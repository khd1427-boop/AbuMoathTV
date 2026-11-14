package com.abumoath.tv.data.source

import android.content.Context
import androidx.room.withTransaction
import com.abumoath.tv.data.local.ChannelEntity
import com.abumoath.tv.data.local.ChannelStreamEntity
import com.abumoath.tv.data.local.LocalDb
import com.abumoath.tv.data.local.TitleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Content Aggregation System running directly on the TV (no own server needed):
 *  - Live channels: iptv-org public index of free-to-air streams (channels removed after DMCA reports
 *    and adult channels are not part of this playlist).
 *  - Movies: Internet Archive films marked Public Domain or Creative Commons only.
 * Runs on first launch, then automatically once a day (or on demand from Settings).
 */
@Singleton
class ContentSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: LocalDb,
    @Named("base") private val client: OkHttpClient
) {
    companion object {
        const val CHANNELS_URL = "https://iptv-org.github.io/iptv/index.m3u"
        const val ARCHIVE_SEARCH = "https://archive.org/advancedsearch.php"
        const val ARCHIVE_QUERY = "collection:feature_films AND mediatype:movies AND (licenseurl:*publicdomain* OR licenseurl:*creativecommons*)"
        private const val DAY_MS = 24L * 3600 * 1000

        // iptv-org group-title -> our category ids
        val CATEGORY_MAP = mapOf(
            "general" to "general", "news" to "news", "sports" to "sports", "movies" to "movies", "series" to "series",
            "kids" to "kids", "animation" to "kids", "family" to "general", "documentary" to "documentary",
            "education" to "education", "science" to "education", "music" to "music", "religious" to "religious",
            "culture" to "culture", "classic" to "movies", "entertainment" to "entertainment", "comedy" to "entertainment",
            "lifestyle" to "entertainment", "travel" to "culture", "cooking" to "entertainment", "outdoor" to "entertainment",
            "business" to "news", "weather" to "news", "legislative" to "news", "auto" to "entertainment", "relax" to "entertainment"
        )
        private val GENRE_WORDS = listOf(
            Regex("comed", RegexOption.IGNORE_CASE) to "comedy", Regex("horror", RegexOption.IGNORE_CASE) to "horror",
            Regex("thrill|suspense", RegexOption.IGNORE_CASE) to "thriller", Regex("sci-?fi|science fiction", RegexOption.IGNORE_CASE) to "scifi",
            Regex("western", RegexOption.IGNORE_CASE) to "western", Regex("drama", RegexOption.IGNORE_CASE) to "drama",
            Regex("noir|crime|gangster|detective", RegexOption.IGNORE_CASE) to "crime", Regex("myster", RegexOption.IGNORE_CASE) to "mystery",
            Regex("\\bwar\\b", RegexOption.IGNORE_CASE) to "war", Regex("roman", RegexOption.IGNORE_CASE) to "romance",
            Regex("adventure", RegexOption.IGNORE_CASE) to "adventure", Regex("action", RegexOption.IGNORE_CASE) to "action",
            Regex("documentar", RegexOption.IGNORE_CASE) to "documentary", Regex("animat|cartoon", RegexOption.IGNORE_CASE) to "animation",
            Regex("famil|children", RegexOption.IGNORE_CASE) to "family", Regex("music", RegexOption.IGNORE_CASE) to "music",
            Regex("histor", RegexOption.IGNORE_CASE) to "history", Regex("sport|boxing", RegexOption.IGNORE_CASE) to "sport"
        )
        private val LANG = mapOf("english" to "en", "arabic" to "ar", "french" to "fr", "german" to "de", "italian" to "it",
            "spanish" to "es", "russian" to "ru", "japanese" to "ja", "chinese" to "zh", "turkish" to "tr", "hindi" to "hi", "korean" to "ko",
            "eng" to "en", "ara" to "ar", "fre" to "fr", "fra" to "fr", "ger" to "de", "deu" to "de", "ita" to "it", "spa" to "es", "rus" to "ru",
            "jpn" to "ja", "chi" to "zh", "zho" to "zh", "tur" to "tr", "hin" to "hi", "kor" to "ko", "por" to "pt", "portuguese" to "pt")
    }

    sealed interface Status {
        data object Idle : Status
        data class Running(val step: Int) : Status    // 1 = channels, 2 = movies
        data class Done(val channels: Int, val movies: Int) : Status
        data class Failed(val message: String) : Status
    }

    private val prefs = context.getSharedPreferences("sync", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val _status = MutableStateFlow<Status>(Status.Idle)
    val status = _status.asStateFlow()

    val lastSync: Long get() = prefs.getLong("last", 0L)
    val hasContent: Boolean get() = prefs.getBoolean("has_content", false)
    fun isStale() = System.currentTimeMillis() - lastSync > DAY_MS

    /** Runs a full update. Each source is independent: if one fails the other is still updated. */
    suspend fun sync(): Status = mutex.withLock {
        withContext(Dispatchers.IO) {
            var errors = 0
            _status.value = Status.Running(1)
            val ch = runCatching { syncChannels() }.getOrElse { errors++; -1 }
            _status.value = Status.Running(2)
            val mv = runCatching { syncMovies() }.getOrElse { errors++; -1 }
            val result = if (errors == 2) Status.Failed("network") else {
                prefs.edit().putLong("last", System.currentTimeMillis()).putBoolean("has_content", true).apply()
                Status.Done(maxOf(ch, 0), maxOf(mv, 0))
            }
            _status.value = result
            result
        }
    }

    // ---------------- Live channels ----------------
    private data class Entry(val ext: String, val name: String, val logo: String?, val country: String?, val cats: Set<String>,
                             val url: String, val height: Int, val referrer: String?, val ua: String?)

    private val attrRe = Regex("([a-zA-Z0-9-]+)=\"([^\"]*)\"")
    private val heightRe = Regex("\\((\\d{3,4})p\\)")

    private fun parseM3u(reader: BufferedReader): List<Entry> {
        val out = ArrayList<Entry>(12000)
        var attrs: Map<String, String>? = null
        var name = ""
        var referrer: String? = null
        var ua: String? = null
        reader.lineSequence().forEach { raw ->
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    attrs = attrRe.findAll(line).associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                    name = line.substringAfterLast(',').trim()
                    referrer = attrs?.get("http-referrer"); ua = attrs?.get("http-user-agent")
                }
                line.startsWith("#EXTVLCOPT:") -> {
                    val kv = line.removePrefix("#EXTVLCOPT:")
                    if (kv.startsWith("http-referrer=")) referrer = kv.substringAfter('=')
                    if (kv.startsWith("http-user-agent=")) ua = kv.substringAfter('=')
                }
                line.isNotEmpty() && !line.startsWith("#") && attrs != null -> {
                    val a = attrs!!
                    val adult = a["group-title"].orEmpty().contains("xxx", ignoreCase = true)
                    if (line.startsWith("http") && !adult) {
                        val tvgId = a["tvg-id"].orEmpty()
                        val ext = tvgId.ifBlank { "name:" + name.lowercase() }.substringBefore('@')
                        val country = tvgId.substringBefore('@').substringAfterLast('.', "").uppercase().takeIf { it.length == 2 }
                        val cats = a["group-title"].orEmpty().split(';').mapNotNull { CATEGORY_MAP[it.trim().lowercase()] }.toSet()
                        val height = heightRe.find(name)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                        val clean = name.replace(heightRe, "").replace(Regex("\\[[^]]*]"), "").trim()
                        out += Entry(ext, clean.ifBlank { name }, a["tvg-logo"]?.takeIf { it.startsWith("http") }, country,
                            cats.ifEmpty { setOf("general") }, line, height, referrer, ua)
                    }
                    attrs = null; referrer = null; ua = null
                }
            }
        }
        return out
    }

    private suspend fun syncChannels(): Int {
        val entries = client.newCall(Request.Builder().url(CHANNELS_URL).build()).execute().use { r ->
            if (!r.isSuccessful) error("HTTP ${r.code}")
            parseM3u(r.body!!.charStream().buffered())
        }
        if (entries.isEmpty()) error("empty playlist")
        val groups = entries.groupBy { it.ext }
        val dao = db.channels()
        val now = System.currentTimeMillis()
        db.withTransaction {
            val oldByExt = dao.all().associateBy { it.ext }
            val existing = oldByExt.mapValues { it.value.id }
            for ((ext, list) in groups) {
                val first = list.first()
                val old = oldByExt[ext]
                val id = old?.id ?: 0L
                val newId = dao.upsert(ChannelEntity(id = id, ext = ext, name = first.name, nameNorm = TextTools.normalize(first.name),
                    logo = list.firstNotNullOfOrNull { it.logo }, country = first.country,
                    categories = "," + list.flatMap { it.cats }.toSet().joinToString(",") + ",",
                    bestHeight = list.maxOf { it.height }, views = old?.views ?: 0, addedAt = old?.addedAt ?: now))
                dao.deleteStreams(newId)
                dao.insertStreams(list.distinctBy { it.url }.map { ChannelStreamEntity(channelId = newId, url = it.url, height = it.height, referrer = it.referrer, userAgent = it.ua) })
            }
            // Channels that disappeared upstream (closed, removed after a rights complaint...) are removed here too.
            val gone = existing.filterKeys { it !in groups }.values.toList()
            gone.chunked(500).forEach { dao.deleteStreamsOf(it); dao.deleteChannels(it) }
        }
        return groups.size
    }

    // ---------------- Movies (Internet Archive) ----------------
    private fun JsonElement?.str(): String? = when (this) {
        null, JsonNull -> null
        is JsonArray -> firstOrNull()?.str()
        is JsonPrimitive -> contentOrNull
        else -> null
    }
    private fun JsonElement?.strList(): List<String> = when (this) {
        is JsonArray -> mapNotNull { it.str() }
        is JsonPrimitive -> contentOrNull?.split(';', ',')?.map { it.trim() } ?: emptyList()
        else -> emptyList()
    }

    private suspend fun syncMovies(): Int {
        val fields = listOf("identifier", "title", "year", "description", "language", "licenseurl", "subject", "runtime", "downloads", "director", "creator")
        val url = buildString {
            append(ARCHIVE_SEARCH).append("?q=").append(java.net.URLEncoder.encode(ARCHIVE_QUERY, "UTF-8"))
            fields.forEach { append("&fl[]=").append(it) }
            append("&sort[]=downloads+desc&rows=600&page=1&output=json")
        }
        val docs = client.newCall(Request.Builder().url(url).build()).execute().use { r ->
            if (!r.isSuccessful) error("HTTP ${r.code}")
            Json.parseToJsonElement(r.body!!.string()).jsonObject["response"]?.jsonObject?.get("docs")?.jsonArray ?: JsonArray(emptyList())
        }
        val dao = db.titles()
        val now = System.currentTimeMillis()
        var count = 0
        db.withTransaction {
            val existing = dao.idMap().associate { it.ext to it.id }
            docs.forEachIndexed { index, el ->
                val o = el.jsonObject
                val ext = o["identifier"].str() ?: return@forEachIndexed
                val license = o["licenseurl"].str()
                if (license == null || !Regex("publicdomain|creativecommons", RegexOption.IGNORE_CASE).containsMatchIn(license)) return@forEachIndexed
                val title = o["title"].str()?.trim().orEmpty().ifBlank { ext }
                val id = existing[ext] ?: 0L
                val old = if (id != 0L) dao.get(id) else null
                val subjects = o["subject"].strList()
                val genres = subjects.flatMap { s -> GENRE_WORDS.filter { it.first.containsMatchIn(s) }.map { it.second } }.distinct().take(4)
                val lang = o["language"].str()?.lowercase()?.trim()?.let { LANG[it] ?: it.takeIf { c -> c.length == 2 } }
                dao.upsert(TitleEntity(
                    id = id, ext = ext, title = title, titleNorm = TextTools.normalize(title),
                    year = o["year"].str()?.take(4)?.toIntOrNull(),
                    overview = o["description"].str()?.replace(Regex("<[^>]+>"), " ")?.replace(Regex("\\s+"), " ")?.trim()?.take(1500),
                    language = lang, runtimeMin = parseRuntime(o["runtime"].str()),
                    genres = "," + genres.joinToString(",") + ",",
                    directors = (o["director"].strList().ifEmpty { o["creator"].strList() }).take(2).joinToString("، ").ifBlank { null },
                    license = license, popularity = docs.size - index,
                    views = old?.views ?: 0, addedAt = old?.addedAt ?: now
                ))
                count++
            }
        }
        return count
    }

    private fun parseRuntime(v: String?): Int? {
        if (v == null) return null
        Regex("^(\\d+):(\\d{2}):(\\d{2})").find(v)?.let { val (h, m, _) = it.destructured; return h.toInt() * 60 + m.toInt() }
        Regex("^(\\d+):(\\d{2})$").find(v)?.let { return it.groupValues[1].toInt() }
        return Regex("(\\d+)\\s*min", RegexOption.IGNORE_CASE).find(v)?.groupValues?.get(1)?.toIntOrNull()
    }
}
