package com.abumoath.tv.data.repo

import com.abumoath.tv.data.local.*
import com.abumoath.tv.data.model.*
import com.abumoath.tv.data.prefs.AppSettings
import com.abumoath.tv.data.source.ArchiveResolver
import com.abumoath.tv.data.source.TextTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Countries treated as "Arabic" for the home rows. */
val ARAB_COUNTRIES = listOf("SA", "AE", "KW", "QA", "BH", "OM", "EG", "IQ", "JO", "PS", "SY", "LB", "MA", "DZ", "TN", "SD", "YE", "LY", "MR")

/** Channel category names (ids match ContentSync.CATEGORY_MAP values). */
val CATEGORY_NAMES = mapOf(
    "general" to ("قنوات عامة" to "General"), "news" to ("قنوات إخبارية" to "News"), "sports" to ("قنوات رياضية" to "Sports"),
    "movies" to ("قنوات أفلام" to "Movies"), "series" to ("قنوات مسلسلات" to "Series"), "kids" to ("قنوات أطفال" to "Kids"),
    "documentary" to ("قنوات وثائقية" to "Documentary"), "education" to ("قنوات تعليمية" to "Education"), "music" to ("قنوات موسيقية" to "Music"),
    "religious" to ("قنوات دينية" to "Religious"), "culture" to ("قنوات ثقافية" to "Culture"), "entertainment" to ("قنوات ترفيهية" to "Entertainment")
)

val GENRE_NAMES = mapOf(
    "action" to ("أكشن" to "Action"), "comedy" to ("كوميديا" to "Comedy"), "drama" to ("دراما" to "Drama"), "horror" to ("رعب" to "Horror"),
    "thriller" to ("إثارة" to "Thriller"), "scifi" to ("خيال علمي" to "Sci-Fi"), "adventure" to ("مغامرات" to "Adventure"),
    "romance" to ("رومانسية" to "Romance"), "history" to ("تاريخية" to "History"), "documentary" to ("وثائقية" to "Documentary"),
    "family" to ("عائلية" to "Family"), "animation" to ("رسوم متحركة" to "Animation"), "crime" to ("جريمة" to "Crime"), "war" to ("حرب" to "War"),
    "sport" to ("رياضية" to "Sport"), "mystery" to ("غموض" to "Mystery"), "western" to ("غرب أمريكي" to "Western"), "music" to ("موسيقى" to "Music")
)

/** Country flag emoji from an ISO code ("SA" -> 🇸🇦). */
fun flagOf(code: String?): String {
    if (code == null || code.length != 2) return "🌍"
    return code.uppercase().map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")
}

private fun qLabel(h: Int) = when { h >= 1000 -> "FHD"; h >= 700 -> "HD"; h > 0 -> "SD"; else -> null }

@Singleton
class CatalogRepository @Inject constructor(
    private val db: LocalDb,
    private val settings: AppSettings,
    private val resolver: ArchiveResolver,
    @Named("base") private val client: OkHttpClient
) {
    private val ar get() = settings.language != "en"
    private fun n(p: Pair<String, String>?) = p?.let { if (ar) it.first else it.second }

    private fun TitleEntity.card(progress: ProgressDto? = null) = TitleCard(
        id = id, type = type, title = title, year = year, overview = overview,
        posterUrl = "https://archive.org/services/img/$ext", backdropUrl = null, progress = progress
    )

    private fun ChannelEntity.card() = ChannelCard(id, name, logo, country, categories.trim(',').split(',').filter { it.isNotEmpty() }, qLabel(bestHeight))

    // ---------------- Titles (filtered in memory: a few hundred films) ----------------
    @Volatile private var titleCache: List<TitleEntity>? = null
    fun invalidate() { titleCache = null; searchCache = null }
    private suspend fun allTitles(): List<TitleEntity> = titleCache ?: db.titles().all().also { titleCache = it }

    private fun matches(t: TitleEntity, f: Map<String, String>): Boolean {
        f["language"]?.let { if (t.language != it) return false }
        f["genre"]?.let { if (!t.genres.contains(",$it,")) return false }
        f["decade"]?.toIntOrNull()?.let { d -> if (t.year == null || t.year < d || t.year > d + 9) return false }
        when (f["duration"]) {
            "short" -> if ((t.runtimeMin ?: 0) == 0 || t.runtimeMin!! >= 90) return false
            "medium" -> if (t.runtimeMin == null || t.runtimeMin !in 90..120) return false
            "long" -> if ((t.runtimeMin ?: 0) <= 120) return false
        }
        when (f["region"]) {
            "arab" -> if (t.language != "ar") return false
            "foreign" -> if (t.language == "ar") return false
            "turkish" -> if (t.language != "tr") return false
            "indian" -> if (t.language != "hi") return false
            "korean" -> if (t.language != "ko") return false
            "japanese" -> if (t.language != "ja") return false
            "europe" -> if (t.language !in setOf("fr", "de", "it", "es", "ru", "pt", "nl", "sv", "pl")) return false
        }
        return true
    }

    suspend fun titles(filters: Map<String, String>, page: Int): PageResponse<TitleCard> {
        val list = allTitles().filter { matches(it, filters) }.let { l ->
            when (filters["sort"]) {
                "top" -> l.sortedWith(compareByDescending<TitleEntity> { it.views }.thenByDescending { it.popularity })
                "year" -> l.sortedByDescending { it.year ?: 0 }
                "rating" -> l.sortedByDescending { it.popularity }
                else -> l.sortedWith(compareByDescending<TitleEntity> { it.year ?: 0 }.thenByDescending { it.popularity })
            }
        }
        val size = 30
        return PageResponse(page, size, list.size, list.drop((page - 1) * size).take(size).map { it.card() })
    }

    suspend fun facets(type: String?): FacetsResponse {
        if (type == "channel") {
            val countries = db.channels().countryFacets().mapNotNull { r -> r.k?.let { Facet(it, TextTools.countryName(it, settings.language), r.n) } }
            val catCounts = HashMap<String, Int>()
            db.channels().allCategories().forEach { r -> r.k?.trim(',')?.split(',')?.filter { it.isNotEmpty() }?.forEach { catCounts[it] = (catCounts[it] ?: 0) + 1 } }
            return FacetsResponse(countries = countries.sortedByDescending { it.count },
                categories = CATEGORY_NAMES.keys.mapNotNull { k -> catCounts[k]?.let { Facet(k, n(CATEGORY_NAMES[k])!!, it) } })
        }
        val all = allTitles()
        val langs = all.mapNotNull { it.language }.groupingBy { it }.eachCount()
            .map { (k, c) -> Facet(k, TextTools.languageName(k, settings.language), c) }.sortedByDescending { it.count }
        val genres = all.flatMap { it.genres.trim(',').split(',').filter { g -> g.isNotEmpty() } }.groupingBy { it }.eachCount()
            .mapNotNull { (k, c) -> GENRE_NAMES[k]?.let { Facet(k, n(it)!!, c) } }.sortedByDescending { it.count }
        val decades = all.mapNotNull { it.year?.let { y -> y / 10 * 10 } }.groupingBy { it }.eachCount()
            .map { (d, c) -> Facet(d.toString(), "${d}s", c) }.sortedByDescending { it.id }
        return FacetsResponse(languages = langs, genres = genres, decades = decades)
    }

    suspend fun title(id: Long): TitleDetails {
        val t = db.titles().get(id) ?: error("not_found")
        val p = db.progress().get("title", id)
        val resolved = runCatching { resolver.resolve(t.ext) }.getOrNull()
        val streams = resolved?.streams ?: emptyList()
        return TitleDetails(
            id = t.id, title = t.title, year = t.year, language = t.language,
            languageName = t.language?.let { TextTools.languageName(it, settings.language) },
            posterUrl = "https://archive.org/services/img/${t.ext}", overview = t.overview ?: "", runtimeMin = t.runtimeMin,
            genres = t.genres.trim(',').split(',').mapNotNull { g -> GENRE_NAMES[g]?.let { NamedDto(g, n(it)!!) } },
            directors = t.directors?.split("، ")?.filter { it.isNotBlank() } ?: emptyList(),
            license = t.license, sourcePage = "https://archive.org/details/${t.ext}",
            progress = p?.let { ProgressDto("title", id, it.positionMs, it.durationMs, it.completed) },
            streams = streams, subtitles = resolved?.subtitles ?: emptyList(),
            qualities = streams.mapNotNull { it.quality }.distinct(),
            quality = streams.firstOrNull()?.quality
        )
    }

    // ---------------- Channels ----------------
    suspend fun channels(filters: Map<String, String>, page: Int): PageResponse<ChannelCard> {
        val size = 60
        val c = filters["country"]; val cat = filters["category"]
        val items = db.channels().list(c, cat, size, (page - 1) * size).map { it.card() }
        return PageResponse(page, size, db.channels().count(c, cat), items)
    }

    /** Live channels for the category pages. region: "arab" | "foreign" | null; country: ISO code or null. */
    suspend fun channelsFor(category: String?, region: String?, country: String? = null, limit: Int = 30): List<ChannelCard> {
        val list = when {
            country != null -> db.channels().list(country, category, limit, 0)
            region == "arab" -> db.channels().inCountries(ARAB_COUNTRIES, category, limit)
            else -> db.channels().list(null, category, limit * 4, 0).filter { region != "foreign" || it.country !in ARAB_COUNTRIES }.take(limit)
        }
        return list.map { it.card() }
    }

    suspend fun channel(id: Long): ChannelDetails {
        val c = db.channels().get(id) ?: error("not_found")
        val streams = db.channels().streams(id).map {
            StreamDto(it.id, it.url, if (it.url.substringBefore('?').endsWith(".mpd")) "dash" else "hls", qLabel(it.height), it.height, it.referrer, it.userAgent)
        }
        return ChannelDetails(c.id, c.name, c.logo, c.country, streams)
    }

    suspend fun reportStart(kind: String, id: Long) = runCatching {
        if (kind == "channel") db.channels().addView(id) else db.titles().addView(id)
    }
    suspend fun reportStreamResult(kind: String, streamId: Long, ok: Boolean) = runCatching {
        if (kind != "channel") return@runCatching
        if (ok) db.channels().markOk(streamId) else db.channels().markFailed(streamId)
    }

    // ---------------- Home (live channels only) ----------------
    /** Countries shown as their own rows on the home screen (in this order, when they have channels). */
    private val HOME_COUNTRIES = listOf("SA", "AE", "KW", "QA", "BH", "OM", "EG", "IQ", "JO", "LB", "SY", "PS", "MA", "DZ", "TN", "LY", "YE", "SD", "TR")

    suspend fun home(): HomeResponse {
        val rows = mutableListOf<HomeRow>()
        val used = HashSet<Long>()
        fun C(id: String, a: String, e: String, items: List<ChannelEntity>, browse: Map<String, String>?, unique: Boolean = false) {
            val list = if (unique) items.filter { used.add(it.id) } else items
            if (list.isNotEmpty() && rows.none { it.id == id }) rows += HomeRow(id, if (ar) a else e, "channels", channels = list.map { it.card() }, browse = browse)
        }
        val chDao = db.channels()

        // Hero: the main Arabic news channels (Al Jazeera first).
        val newsIds = chDao.searchIndex().mapNotNull { r -> com.abumoath.tv.data.source.ChannelAliases.newsRank(r.name)?.let { r.id to it } }
            .sortedBy { it.second }.map { it.first }.take(20)
        val newsMap = chDao.getMany(newsIds).associateBy { it.id }
        val news = newsIds.mapNotNull { newsMap[it] }

        // Your channels first
        val favIds = db.library().list("favorite").filter { it.itemType == "channel" }.map { it.itemId }
        val favMap = chDao.getMany(favIds).associateBy { it.id }
        C("fav", "قنواتي المفضلة", "My favorites", favIds.mapNotNull { favMap[it] }, null)
        C("recent", "الأكثر مشاهدة عندك", "Your most watched", chDao.mostWatched(20), null)

        C("news_ar", "القنوات الإخبارية العربية", "Arabic news", news, mapOf("category" to "news"))
        C("sports", "القنوات الرياضية", "Sports", chDao.list(null, "sports", 30, 0), mapOf("category" to "sports"))
        C("religious", "قنوات القرآن الكريم والقنوات الدينية", "Quran & religious", chDao.inCountries(ARAB_COUNTRIES, "religious", 30), mapOf("category" to "religious"))
        C("kids", "قنوات الأطفال", "Kids", chDao.list(null, "kids", 30, 0), mapOf("category" to "kids"))

        // One row per Arab country (flag + name)
        for (code in HOME_COUNTRIES) {
            val items = chDao.list(code, null, 25, 0)
            if (items.isEmpty()) continue
            val flag = flagOf(code)
            C("c_$code", "$flag  " + TextTools.countryName(code, "ar"), "$flag  " + TextTools.countryName(code, "en"), items, mapOf("country" to code))
        }

        C("docs", "قنوات وثائقية", "Documentary", chDao.list(null, "documentary", 30, 0), mapOf("category" to "documentary"))
        C("movies_ch", "قنوات الأفلام", "Movie channels", chDao.list(null, "movies", 30, 0), mapOf("category" to "movies"))
        C("series_ch", "قنوات المسلسلات", "Series channels", chDao.list(null, "series", 30, 0), mapOf("category" to "series"))
        C("music", "قنوات موسيقية", "Music", chDao.list(null, "music", 30, 0), mapOf("category" to "music"))
        C("culture", "قنوات ثقافية وتعليمية", "Culture & education", chDao.list(null, "culture", 15, 0) + chDao.list(null, "education", 15, 0), mapOf("category" to "culture"))
        C("world_news", "أخبار عالمية", "World news", chDao.list(null, "news", 40, 0).filter { it.country !in ARAB_COUNTRIES }.take(30), mapOf("category" to "news"))
        C("entertainment", "قنوات ترفيهية", "Entertainment", chDao.list(null, "entertainment", 30, 0), mapOf("category" to "entertainment"))

        return HomeResponse(rows = rows, heroChannels = news.take(6).map { it.card() })
    }

    /** Channels around the current one (same country), used for channel up/down zapping in the player. */
    suspend fun channelNeighbors(id: Long): List<Long> {
        val c = db.channels().get(id) ?: return emptyList()
        return db.channels().list(c.country, null, 400, 0).map { it.id }
    }

    suspend fun continueWatching(): List<TitleCard> {
        val prog = db.progress().recent().filter { !it.completed }.take(20)
        if (prog.isEmpty()) return emptyList()
        val map = db.titles().getMany(prog.map { it.titleId }).associateBy { it.id }
        return prog.mapNotNull { p -> map[p.titleId]?.card(ProgressDto("title", p.itemId, p.positionMs, p.durationMs, p.completed, p.updatedAt)) }
    }

    // ---------------- Search ----------------
    private data class Idx(val kind: String, val id: Long, val norm: String)
    @Volatile private var searchCache: List<Idx>? = null

    suspend fun search(q: String): SearchResults = withContext(Dispatchers.Default) {
        val qn = TextTools.normalize(q)
        if (qn.isEmpty()) return@withContext SearchResults()
        val qt = TextTools.tokens(q)
        val idx = searchCache ?: buildList {
            allTitles().forEach { add(Idx("t", it.id, it.titleNorm)) }
            db.channels().searchIndex().forEach { add(Idx("c", it.id, it.nameNorm)) }
        }.also { searchCache = it }
        val scored = idx.mapNotNull { e -> TextTools.score(qn, qt, e.norm).takeIf { it > 0 }?.let { e to it } }.sortedByDescending { it.second }.take(80)
        val titles = scored.filter { it.first.kind == "t" }.take(30).map { it.first.id }
        val chans = scored.filter { it.first.kind == "c" }.take(40).map { it.first.id }
        val tMap = db.titles().getMany(titles).associateBy { it.id }
        val cMap = db.channels().getMany(chans).associateBy { it.id }
        // Countries & categories by name (e.g. "السعودية" or "رياضة")
        val countries = db.channels().countryFacets().mapNotNull { r ->
            val code = r.k ?: return@mapNotNull null
            val names = listOf(TextTools.countryName(code, "ar"), TextTools.countryName(code, "en"))
            if (names.any { TextTools.score(qn, qt, TextTools.normalize(it)) > 0 }) Facet(code, TextTools.countryName(code, settings.language), r.n) else null
        }
        val cats = CATEGORY_NAMES.filter { (_, p) -> TextTools.score(qn, qt, TextTools.normalize(p.first)) > 0 || TextTools.score(qn, qt, TextTools.normalize(p.second)) > 0 }
            .map { (k, p) -> Facet(k, n(p)!!) }
        SearchResults(titles.mapNotNull { tMap[it]?.card() }, chans.mapNotNull { cMap[it]?.card() }, countries, cats)
    }

    suspend fun suggest(q: String): List<String> {
        val r = search(q)
        return (r.titles.map { it.displayTitle } + r.channels.map { it.name }).distinct().take(8)
    }

    /** Measures throughput with a 2 MB download from a public speed-test endpoint. Nothing is stored. */
    suspend fun speedTestMbps(): Double = withContext(Dispatchers.IO) {
        val start = System.nanoTime()
        var bytes = 0L
        client.newCall(Request.Builder().url("https://speed.cloudflare.com/__down?bytes=2000000").build()).execute().use { r ->
            val input = r.body!!.byteStream(); val buf = ByteArray(32 * 1024)
            while (true) { val k = input.read(buf); if (k < 0) break; bytes += k }
        }
        (bytes * 8 / 1e6) / ((System.nanoTime() - start) / 1e9).coerceAtLeast(0.05)
    }
}

@Singleton
class LibraryRepository @Inject constructor(private val db: LocalDb, private val catalog: CatalogRepository) {
    fun observe(list: String, itemType: String, id: Long): Flow<Boolean> = db.library().observeContains(list, itemType, id)

    suspend fun setInList(list: String, itemType: String, id: Long, add: Boolean) {
        if (add) db.library().add(LibraryEntity(list, itemType, id, System.currentTimeMillis())) else db.library().remove(list, itemType, id)
    }

    /** Favorites / watchlist entries (titles and channels). */
    suspend fun list(list: String): Pair<List<TitleCard>, List<ChannelCard>> {
        val entries = db.library().list(list)
        val titles = db.titles().getMany(entries.filter { it.itemType == "title" }.map { it.itemId }).associateBy { it.id }
        val chans = db.channels().getMany(entries.filter { it.itemType == "channel" }.map { it.itemId }).associateBy { it.id }
        val t = entries.mapNotNull { e -> if (e.itemType == "title") titles[e.itemId]?.let { TitleCard(it.id, title = it.title, year = it.year, posterUrl = "https://archive.org/services/img/${it.ext}") } else null }
        val c = entries.mapNotNull { e -> if (e.itemType == "channel") chans[e.itemId]?.let { ChannelCard(it.id, it.name, it.logo, it.country) } else null }
        return t to c
    }

    suspend fun history(): List<TitleCard> {
        val prog = db.progress().recent()
        val map = db.titles().getMany(prog.map { it.titleId }).associateBy { it.id }
        return prog.mapNotNull { p -> map[p.titleId]?.let { TitleCard(it.id, title = it.title, year = it.year, posterUrl = "https://archive.org/services/img/${it.ext}",
            progress = ProgressDto("title", p.itemId, p.positionMs, p.durationMs, p.completed, p.updatedAt)) } }
    }

    suspend fun localProgress(itemType: String, id: Long) = db.progress().get(itemType, id)

    suspend fun saveProgress(itemType: String, id: Long, titleId: Long, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        db.progress().upsert(ProgressEntity(itemType, id, titleId, positionMs, durationMs, positionMs.toDouble() / durationMs > 0.93, System.currentTimeMillis()))
    }

    suspend fun isIn(list: String, itemType: String, id: Long): Boolean = db.library().observeContains(list, itemType, id).first()

    suspend fun deleteHistory(card: TitleCard) = db.progress().deleteForTitle(card.id)
    suspend fun clearHistory() = db.progress().clear()
}
