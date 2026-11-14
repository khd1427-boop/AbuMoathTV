package com.abumoath.tv.ui.navigation

import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.*
import com.abumoath.tv.R
import com.abumoath.tv.ui.browse.BrowseScreen
import com.abumoath.tv.ui.details.DetailsScreen
import com.abumoath.tv.ui.home.HomeScreen
import com.abumoath.tv.ui.library.LibraryScreen
import com.abumoath.tv.ui.live.LiveTvScreen
import com.abumoath.tv.ui.player.PlayerScreen
import com.abumoath.tv.ui.portal.PortalScreen
import com.abumoath.tv.ui.search.SearchScreen
import com.abumoath.tv.ui.settings.SettingsScreen
import com.abumoath.tv.ui.splash.SplashScreen
import com.abumoath.tv.ui.theme.Brand

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val BROWSE = "browse?mode={mode}&filters={filters}"
    const val LIVE = "live?category={category}&country={country}"
    const val DETAILS = "details/{id}"
    const val PLAYER = "player/{kind}/{id}?resume={resume}"
    const val SEARCH = "search"
    const val PORTAL = "portal/{mode}"
    fun portal(mode: String) = "portal/$mode"
    const val LIBRARY = "library/{tab}"
    const val SETTINGS = "settings"

    /** mode: movies | series | country | language | genre | free | top | new | custom; filters: "k=v&k2=v2". */
    fun browse(mode: String, filters: Map<String, String> = emptyMap()) =
        "browse?mode=$mode&filters=" + Uri.encode(filters.entries.joinToString("&") { "${it.key}=${it.value}" })
    fun live(category: String? = null) = "live?category=${category ?: ""}&country="
    fun liveCountry(country: String) = "live?category=&country=$country"
    fun details(id: Long) = "details/$id"
    fun player(kind: String, id: Long, resumeMs: Long = -1) = "player/$kind/$id?resume=$resumeMs"
    fun library(tab: String) = "library/$tab"
}

enum class MenuItem(val route: () -> String, val label: Int, val icon: Int, val category: String? = null) {
    Search({ Routes.SEARCH }, R.string.menu_search, R.drawable.ic_mic),
    Home({ Routes.HOME }, R.string.menu_home, R.drawable.ic_home),
    All({ Routes.live() }, R.string.menu_all_channels, R.drawable.ic_live_tv),
    News({ Routes.live("news") }, R.string.cat_news, R.drawable.ic_new, "news"),
    Sports({ Routes.live("sports") }, R.string.cat_sports, R.drawable.ic_hd, "sports"),
    Religious({ Routes.live("religious") }, R.string.cat_religious, R.drawable.ic_anime, "religious"),
    Kids({ Routes.live("kids") }, R.string.cat_kids, R.drawable.ic_free, "kids"),
    Documentary({ Routes.live("documentary") }, R.string.cat_documentary, R.drawable.ic_globe, "documentary"),
    MoviesCh({ Routes.live("movies") }, R.string.cat_movies, R.drawable.ic_movie, "movies"),
    SeriesCh({ Routes.live("series") }, R.string.cat_series, R.drawable.ic_series, "series"),
    Music({ Routes.live("music") }, R.string.cat_music, R.drawable.ic_audio, "music"),
    Favorites({ Routes.library("favorite") }, R.string.menu_favorites, R.drawable.ic_category),
    Settings({ Routes.SETTINGS }, R.string.menu_settings, R.drawable.ic_sync)
}

/**
 * Screen frame with the side navigation rail. The rail is collapsed (icons only) and expands with
 * labels when it receives focus. In Arabic it sits on the right automatically (RTL).
 */
@Composable
fun MainFrame(nav: NavHostController, selected: MenuItem?, content: @Composable () -> Unit) {
    var railFocused by remember { mutableStateOf(false) }
    val width by animateDpAsState(if (railFocused) 250.dp else 76.dp, label = "rail")
    Row(Modifier.fillMaxSize().background(Brand.Black)) {
        Column(
            Modifier
                .width(width)
                .fillMaxHeight()
                .background(if (railFocused) Brand.Surface else Color.Transparent)
                .onFocusChanged { railFocused = it.hasFocus }
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp, horizontal = 10.dp)
        ) {
            Image(painterResource(R.drawable.brand_logo), contentDescription = null, modifier = Modifier.size(54.dp).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(14.dp))
            MenuItem.entries.forEach { item ->
                val isSel = item == selected
                Surface(
                    onClick = {
                        if (!isSel) nav.navigate(item.route()) {
                            popUpTo(Routes.HOME) { inclusive = item == MenuItem.Home }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp).padding(vertical = 2.dp),
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSel) Brand.Gold.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isSel) Brand.GoldLight else Brand.Muted,
                        focusedContainerColor = Brand.Gold,
                        focusedContentColor = Color.Black
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(item.icon), contentDescription = stringResource(item.label), modifier = Modifier.size(22.dp))
                        if (railFocused) {
                            Spacer(Modifier.width(14.dp))
                            Text(stringResource(item.label), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(0.dp))) { content() }
    }
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = { nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } })
        }
        composable(Routes.HOME) {
            MainFrame(nav, MenuItem.Home) { HomeScreen(nav) }
        }
        composable(
            Routes.BROWSE,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType; defaultValue = "movies" },
                navArgument("filters") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            val mode = entry.arguments?.getString("mode") ?: "movies"
            val sel = when (mode) {
                else -> null
            }
            MainFrame(nav, sel) { BrowseScreen(nav) }
        }
        composable(Routes.LIVE, arguments = listOf(navArgument("category") { type = NavType.StringType; defaultValue = "" }, navArgument("country") { type = NavType.StringType; defaultValue = "" })) {
            val cat = it.arguments?.getString("category").orEmpty()
            MainFrame(nav, MenuItem.entries.firstOrNull { m -> m.category == cat && cat.isNotEmpty() } ?: MenuItem.All) { LiveTvScreen(nav) }
        }
        composable(Routes.DETAILS, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            DetailsScreen(nav)
        }
        composable(
            Routes.PLAYER,
            arguments = listOf(
                navArgument("kind") { type = NavType.StringType },
                navArgument("id") { type = NavType.LongType },
                navArgument("resume") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { PlayerScreen(nav) }
        composable(Routes.PORTAL, arguments = listOf(navArgument("mode") { type = NavType.StringType })) { entry ->
            val mode = entry.arguments?.getString("mode") ?: "arab_series"
            MainFrame(nav, null) { PortalScreen(nav) }
        }
        composable(Routes.SEARCH) { MainFrame(nav, MenuItem.Search) { SearchScreen(nav) } }
        composable(Routes.LIBRARY, arguments = listOf(navArgument("tab") { type = NavType.StringType })) { entry ->
            val tab = entry.arguments?.getString("tab") ?: "favorite"
            MainFrame(nav, MenuItem.Favorites) { LibraryScreen(nav) }
        }
        composable(Routes.SETTINGS) {
            MainFrame(nav, MenuItem.Settings) {
                SettingsScreen(nav)
            }
        }
    }
}
