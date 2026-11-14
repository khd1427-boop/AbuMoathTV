package com.abumoath.tv.ui.portal

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.abumoath.tv.R
import com.abumoath.tv.data.model.ChannelCard
import com.abumoath.tv.data.model.TitleCard
import com.abumoath.tv.data.repo.CatalogRepository
import com.abumoath.tv.ui.components.*
import com.abumoath.tv.ui.navigation.Routes
import com.abumoath.tv.ui.theme.Brand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Category pages of the side menu: Arabic / foreign / Korean / anime films and Arabic / foreign / anime series.
 * Each page shows, in this order:
 *  1. the films of the app library that match (recent, openly licensed),
 *  2. free live channels of that kind,
 *  3. shortcuts to the official apps installed on the TV (where recent licensed titles are available),
 *  4. a search that opens YouTube (official channels publish full episodes / films).
 */
data class CategoryConfig(
    val title: Int,
    val films: Map<String, String>?,     // library filter, null = no films (series pages)
    val channelCategory: String?,
    val channelRegion: String?,          // arab | foreign | null
    val channelCountry: String?,
    val channelsTitle: Int,
    val youtubeSuffix: String,
    val searchHint: Int,
    val appKeywords: List<String>,
    val storeQuery: String
)

private val SERIES_APPS = listOf("shahid", "شاهد", "youtube", "netflix", "osn", "starz", "tod", "weyyak", "prime video", "mbc", "watch it", "awaan", "أوان")
private val ANIME_APPS = listOf("youtube", "crunchyroll", "anime", "أنمي", "spacetoon", "سبيستون", "netflix", "shahid", "شاهد")
private val KOREAN_APPS = listOf("youtube", "netflix", "viki", "kocowa", "shahid", "شاهد", "prime video")

val CATEGORIES = mapOf(
    "arab_movies" to CategoryConfig(R.string.menu_arab_movies, mapOf("region" to "arab", "sort" to "new"), "movies", "arab", null,
        R.string.cat_live_movies, " فيلم كامل", R.string.portal_search_movie, SERIES_APPS, "أفلام عربية"),
    "foreign_movies" to CategoryConfig(R.string.menu_foreign_movies, mapOf("region" to "foreign", "sort" to "new"), "movies", "foreign", null,
        R.string.cat_live_movies, " فيلم مترجم عربي", R.string.portal_search_movie, SERIES_APPS, "movies"),
    "korean_movies" to CategoryConfig(R.string.menu_korean_movies, mapOf("region" to "korean", "sort" to "new"), null, null, "KR",
        R.string.cat_live_korean, " فيلم كوري مترجم عربي", R.string.portal_search_movie, KOREAN_APPS, "korean drama"),
    "anime_movies" to CategoryConfig(R.string.menu_anime_movies, mapOf("genre" to "animation", "sort" to "new"), "kids", null, null,
        R.string.anime_live, " فيلم انمي مترجم عربي", R.string.portal_search_anime, ANIME_APPS, "anime"),
    "arab_series" to CategoryConfig(R.string.menu_arab_series, null, "series", "arab", null,
        R.string.series_live, " الحلقة", R.string.portal_search_series, SERIES_APPS, "مسلسلات"),
    "foreign_series" to CategoryConfig(R.string.menu_foreign_series, null, "series", "foreign", null,
        R.string.series_live, " مسلسل مترجم عربي الحلقة", R.string.portal_search_series, SERIES_APPS, "series"),
    "anime_series" to CategoryConfig(R.string.menu_anime_series, null, "kids", null, null,
        R.string.anime_live, " انمي مترجم عربي الحلقة", R.string.portal_search_anime, ANIME_APPS, "anime")
)

data class PortalData(val loaded: Boolean = false, val channels: List<ChannelCard> = emptyList(), val films: List<TitleCard> = emptyList())

@HiltViewModel
class PortalViewModel @Inject constructor(handle: SavedStateHandle, private val catalog: CatalogRepository) : ViewModel() {
    val mode: String = handle["mode"] ?: "arab_series"
    val config: CategoryConfig = CATEGORIES[mode] ?: CATEGORIES.getValue("arab_series")
    private val _data = MutableStateFlow(PortalData())
    val data = _data.asStateFlow()

    init {
        viewModelScope.launch {
            val films = config.films?.let { f -> runCatching { catalog.titles(f, 1).items }.getOrDefault(emptyList()) } ?: emptyList()
            val ch = runCatching { catalog.channelsFor(config.channelCategory, config.channelRegion, config.channelCountry) }.getOrDefault(emptyList())
            _data.value = PortalData(true, ch, films)
        }
    }
}

private data class TvApp(val label: String, val pkg: String)

/** Installed TV apps (leanback launcher) whose name matches the keywords. */
private fun installedApps(ctx: Context, keywords: List<String>): List<TvApp> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
    return runCatching {
        pm.queryIntentActivities(intent, 0).map { TvApp(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
    }.getOrDefault(emptyList())
        .filter { app -> app.pkg != ctx.packageName && keywords.any { k -> app.label.contains(k, ignoreCase = true) || app.pkg.contains(k.replace(" ", ""), ignoreCase = true) } }
        .distinctBy { it.pkg }
        .sortedBy { it.label }
}

private fun launch(ctx: Context, pkg: String) {
    val pm: PackageManager = ctx.packageManager
    val i = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg)
    if (i != null) ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun openStore(ctx: Context, query: String) {
    val tries = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + Uri.encode(query))),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=" + Uri.encode(query) + "&c=apps"))
    )
    for (t in tries) if (runCatching { ctx.startActivity(t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess) return
}

/** Opens the YouTube TV app with a search (several known ways; the first that works is used). */
private fun youtubeSearch(ctx: Context, query: String): Boolean {
    val url = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))
    val tries = listOf(
        Intent(Intent.ACTION_SEARCH).setPackage("com.google.android.youtube.tv").putExtra(SearchManager.QUERY, query),
        Intent(Intent.ACTION_VIEW, url).setPackage("com.google.android.youtube.tv"),
        Intent(Intent.ACTION_VIEW, url)
    )
    for (t in tries) if (runCatching { ctx.startActivity(t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess) return true
    return false
}

@Composable
fun PortalScreen(nav: NavHostController, vm: PortalViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val data by vm.data.collectAsStateWithLifecycle()
    val cfg = vm.config
    val apps = remember(vm.mode) { installedApps(ctx, cfg.appKeywords) }
    var query by remember { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    fun search() {
        if (query.isBlank()) Toast.makeText(ctx, R.string.portal_type_name, Toast.LENGTH_SHORT).show()
        else if (!youtubeSearch(ctx, query.trim() + cfg.youtubeSuffix)) Toast.makeText(ctx, R.string.youtube_missing, Toast.LENGTH_LONG).show()
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 28.dp, bottom = 40.dp)) {
        item {
            Text(stringResource(cfg.title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 40.dp))
        }
        // 1) Films from the app library
        if (data.films.isNotEmpty()) item {
            Spacer(Modifier.height(16.dp))
            ContentRow(stringResource(R.string.cat_in_app), data.films, key = { it.id },
                onSeeAll = { cfg.films?.let { f -> nav.navigate(Routes.browse("custom", f + ("title" to ctx.getString(cfg.title)))) } }) { t ->
                PosterCard(t, onClick = { nav.navigate(Routes.details(t.id)) })
            }
        }
        // 2) Free live channels of this kind
        if (data.channels.isNotEmpty()) item {
            ContentRow(stringResource(cfg.channelsTitle), data.channels, key = { it.id },
                onSeeAll = { nav.navigate(if (cfg.channelCountry != null) Routes.liveCountry(cfg.channelCountry) else Routes.live(cfg.channelCategory)) }) { c ->
                ChannelTile(c, onClick = { nav.navigate(Routes.player("channel", c.id)) })
            }
        }
        // 3) Official apps on this TV
        item {
            Column(Modifier.padding(start = 40.dp, end = 40.dp, top = 12.dp)) {
                Text(stringResource(R.string.portal_apps), style = MaterialTheme.typography.titleLarge, color = Brand.GoldLight)
                Text(stringResource(R.string.portal_note), color = Brand.Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                LazyRow(contentPadding = PaddingValues(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(apps, key = { it.pkg }) { app -> GoldButton(app.label, { launch(ctx, app.pkg) }) }
                    if (apps.none { it.label.contains("shahid", true) || it.label.contains("شاهد") }) {
                        item { GoldButton(stringResource(R.string.install_shahid), { openStore(ctx, "Shahid") }) }
                    }
                    item { GoldButton(stringResource(R.string.open_store), { openStore(ctx, cfg.storeQuery) }) }
                }
            }
        }
        // 4) Search on YouTube
        item {
            Row(Modifier.padding(horizontal = 40.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                TvTextField(query, { query = it }, stringResource(cfg.searchHint), Modifier.weight(1f).focusRequester(searchFocus),
                    imeAction = ImeAction.Search, onImeAction = { search() })
                Spacer(Modifier.width(12.dp))
                GoldButton(stringResource(R.string.search_youtube), { search() })
            }
            Text(stringResource(R.string.portal_official_tip), color = Brand.Muted, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 40.dp))
        }
    }
    // Focus the first useful element once data is loaded.
    LaunchedEffect(data.loaded) { if (data.loaded && data.films.isEmpty() && data.channels.isEmpty()) runCatching { searchFocus.requestFocus() } }
}
