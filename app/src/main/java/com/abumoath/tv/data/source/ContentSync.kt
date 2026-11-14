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
        /** Bump when the content rules change: the app then refreshes everything on next start. */
        const val DATA_VERSION = 4

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
    // After a content-rules update the library is rebuilt in the foreground (old items are not shown).
    val hasContent: Boolean get() = prefs.getBoolean("has_content", false) && prefs.getInt("ver", 1) >= DATA_VERSION
    fun isStale() = System.currentTimeMillis() - lastSync > DAY_MS || prefs.getInt("ver", 1) < DATA_VERSION

    /** Runs a full update. Each source is independent: if one fails the other is still updated. */
    suspend fun sync(): Status = mutex.withLock {
        withContext(Dispatchers.IO) {
            var errors = 0
            _status.value = Status.Running(1)
            val ch = runCatching { syncChannels() }.getOrElse { errors++; -1 }
            // Live-channels-only app: the film library is no longer used and is cleared.
            val mv = runCatching { clearMovies(); 0 }.getOrDefault(0)
            val result = if (ch < 0) Status.Failed("network") else {
                prefs.edit().putLong("last", System.currentTimeMillis()).putBoolean("has_content", true).putInt("ver", DATA_VERSION).apply()
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
                    val adult = a["group-title"].orEmpty().contains("xxx", ignoreCase = true) ||
                        ContentFilter.isAdult(name) || ContentFilter.isAdult(a["tvg-id"].orEmpty())
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
                val arName = ChannelAliases.arabicName(first.name)
                val newId = dao.upsert(ChannelEntity(id = id, ext = ext, name = arName ?: first.name,
                    nameNorm = TextTools.normalize(first.name + " " + (arName ?: "")),
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

    private suspend fun clearMovies() {
        val w = db.openHelper.writableDatabase
        w.execSQL("DELETE FROM titles")
        w.execSQL("DELETE FROM progress")
        w.execSQL("DELETE FROM library WHERE itemType = 'title'")
    }

    /** Open-license queries for RECENT films only (year 2000 and later). */
    private val movieQueries = listOf(
        """mediatype:movies AND year:[2000 TO 2100] AND (licenseurl:*creativecommons* OR licenseurl:*publicdomain*) AND (collection:feature_films OR subject:"feature film" OR subject:"full movie" OR subject:"feature")""" to 400,
        """mediatype:movies AND year:[2000 TO 2100] AND (language:arabic OR language:ara) AND (licenseurl:*creativecommons* OR licenseurl:*publicdomain*)""" to 150,
        """mediatype:movies AND year:[2005 TO 2100] AND licenseurl:*creativecommons* AND (subject:animation OR subject:"animated film" OR subject:"open movie")""" to 150
    )
    private val MIN_YEAR = 2000

    private suspend fun syncMovies(): Int {
        val fields = listOf("identifier", "title", "year", "description", "language", "licenseurl", "subject", "runtime", "downloads", "director", "creator")
        val docs = LinkedHashMap<String, JsonObject>()
        var anyOk = false
        for ((query, rows) in movieQueries) {
            val url = buildString {
                append(ARCHIVE_SEARCH).append("?q=").append(java.net.URLEncoder.encode(query, "UTF-8"))
                fields.forEach { append("&fl[]=").append(it) }
                append("&sort[]=downloads+desc&rows=").append(rows).append("&page=1&output=json")
            }
            val arr = runCatching {
                client.newCall(Request.Builder().url(url).build()).execute().use { r ->
                    if (!r.isSuccessful) error("HTTP ${r.code}")
                    Json.parseToJsonElement(r.body!!.string()).jsonObject["response"]?.jsonObject?.get("docs")?.jsonArray
                }
            }.getOrNull() ?: continue
            anyOk = true
            arr.forEach { el -> val o = el.jsonObject; o["identifier"].str()?.let { id -> docs.putIfAbsent(id, o) } }
        }
        if (!anyOk) error("archive unreachable")

        val dao = db.titles()
        val now = System.currentTimeMillis()
        val kept = HashSet<String>()
        db.withTransaction {
            val existing = dao.idMap().associate { it.ext to it.id }
            for ((ext, o) in docs) {
                val license = o["licenseurl"].str()
                if (license == null || !Regex("publicdomain|creativecommons", RegexOption.IGNORE_CASE).containsMatchIn(license)) continue
                val year = o["year"].str()?.take(4)?.toIntOrNull() ?: continue
                if (year < MIN_YEAR) continue                       // recent films only
                val runtime = parseRuntime(o["runtime"].str())
                if (runtime != null && runtime < 10) continue        // skip clips / trailers
                val title = o["title"].str()?.trim().orEmpty().ifBlank { ext }
                // Family filter: skip anything with adult / sexual keywords in title, subjects or description.
                if (ContentFilter.isAdult(title) || ContentFilter.isAdult(ext) ||
                    o["subject"].strList().any { ContentFilter.isAdult(it) } || ContentFilter.isAdult(o["description"].str().orEmpty().take(3000))) continue
                val id = existing[ext] ?: 0L
                val old = if (id != 0L) dao.get(id) else null
                val subjects = o["subject"].strList()
                val genres = subjects.flatMap { sub -> GENRE_WORDS.filter { it.first.containsMatchIn(sub) }.map { it.second } }.distinct().take(4)
                val lang = o["language"].str()?.lowercase()?.trim()?.let { LANG[it] ?: it.takeIf { c -> c.length == 2 } }
                dao.upsert(TitleEntity(
                    id = id, ext = ext, title = title, titleNorm = TextTools.normalize(title), year = year,
                    overview = o["description"].str()?.replace(Regex("<[^>]+>"), " ")?.replace(Regex("\\s+"), " ")?.trim()?.take(1500),
                    language = lang, runtimeMin = runtime,
                    genres = "," + genres.joinToString(",") + ",",
                    directors = (o["director"].strList().ifEmpty { o["creator"].strList() }).take(2).joinToString("، ").ifBlank { null },
                    license = license, popularity = o["downloads"].str()?.toIntOrNull() ?: 0,
                    views = old?.views ?: 0, addedAt = old?.addedAt ?: now
                ))
                kept += ext
            }
            // Remove films that no longer qualify (e.g. the old classics from the first version).
            val remove = existing.filterKeys { it !in kept }.values.toList()
            val w = db.openHelper.writableDatabase
            remove.chunked(400).forEach { chunk ->
                w.execSQL("DELETE FROM titles WHERE id IN (" + chunk.joinToString(",") + ")")
                w.execSQL("DELETE FROM progress WHERE titleId IN (" + chunk.joinToString(",") + ")")
            }
        }
        return kept.size
    }

    private fun parseRuntime(v: String?): Int? {
        if (v == null) return null
        Regex("^(\\d+):(\\d{2}):(\\d{2})").find(v)?.let { val (h, m, _) = it.destructured; return h.toInt() * 60 + m.toInt() }
        Regex("^(\\d+):(\\d{2})$").find(v)?.let { return it.groupValues[1].toInt() }
        return Regex("(\\d+)\\s*min", RegexOption.IGNORE_CASE).find(v)?.groupValues?.get(1)?.toIntOrNull()
    }
}

/**
 * Family-safe filter applied to every channel and film before it enters the library
 * (so it never appears on screens, rows or search results).
 */
object ContentFilter {
    private val words = listOf(
        "porn", "porno", "xxx", "sex", "sexy", "sexual", "sexploitation", "erotic", "erotica", "nude", "nudity", "naked",
        "adult", "adults only", "18+", "+18", "x-rated", "x rated", "hentai", "fetish", "bdsm", "nsfw", "stripper", "striptease",
        "playboy", "penthouse", "hustler", "brazzers", "dorcel", "redlight", "red light", "babes", "blue movie", "hot girls",
        "softcore", "hardcore", "orgy", "lust", "seduction", "stag film", "burlesque", "nudist", "pin-up", "pinup",
        "اباحي", "إباحي", "جنس", "سكس", "للكبار فقط", "عري", "عاري", "اغراء", "إغراء", "+١٨", "١٨+"
    )
    private val regex = Regex(
        words.joinToString("|") { "(^|[^\\p{L}\\p{N}])" + Regex.escape(it) + "($|[^\\p{L}\\p{N}])" },
        RegexOption.IGNORE_CASE
    )

    fun isAdult(text: String?): Boolean = !text.isNullOrBlank() && regex.containsMatchIn(text)
}
