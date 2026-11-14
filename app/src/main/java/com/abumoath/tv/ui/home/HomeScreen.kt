package com.abumoath.tv.ui.home

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.abumoath.tv.R
import com.abumoath.tv.util.ApiErrors
import com.abumoath.tv.data.model.HomeResponse
import com.abumoath.tv.data.model.TitleCard
import com.abumoath.tv.data.repo.CatalogRepository
import com.abumoath.tv.ui.components.*
import com.abumoath.tv.ui.navigation.Routes
import com.abumoath.tv.ui.theme.Brand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUi {
    data object Loading : HomeUi
    data class Ready(val data: HomeResponse, val offline: Boolean) : HomeUi
    data class Error(val message: Int) : HomeUi
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catalog: CatalogRepository
) : ViewModel() {
    private val _ui = MutableStateFlow<HomeUi>(HomeUi.Loading)
    val ui = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            try {
                _ui.value = HomeUi.Ready(catalog.home(), offline = false)
            } catch (e: Exception) {
                if (_ui.value !is HomeUi.Ready) _ui.value = HomeUi.Error(ApiErrors.messageFor(e))
            }
        }
    }
}

@Composable
fun HomeScreen(nav: NavHostController, vm: HomeViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var lastBack by remember { mutableLongStateOf(0L) }
    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBack < 2000) (context as? Activity)?.finish()
        else { lastBack = now; Toast.makeText(context, R.string.press_back_again, Toast.LENGTH_SHORT).show() }
    }
    // Refresh "continue watching" when coming back from the player.
    LaunchedEffect(Unit) { vm.load() }

    when (val s = ui) {
        HomeUi.Loading -> LoadingView()
        is HomeUi.Error -> ErrorView(stringResource(s.message), vm::load)
        is HomeUi.Ready -> HomeContent(s.data, nav)
    }
}

@Composable
private fun HomeContent(data: HomeResponse, nav: NavHostController) {
    val heroFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { heroFocus.requestFocus() } }
    if (data.hero.isEmpty() && data.rows.isEmpty() && data.heroChannels.isEmpty()) { EmptyView(); return }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
        if (data.heroChannels.isNotEmpty()) item(key = "chero") { ChannelHero(data.heroChannels, nav, heroFocus) }
        else if (data.hero.isNotEmpty()) item(key = "hero") { HeroBanner(data.hero, nav, heroFocus) }
        items(data.rows, key = { it.id }) { row ->
            when (row.kind) {
                "channels" -> {
                    ContentRow(row.title, row.channels, key = { it.id },
                        onSeeAll = {
                            val b = row.browse
                            nav.navigate(if (b?.get("country") != null) Routes.liveCountry(b.getValue("country")) else Routes.live(b?.get("category")))
                        }) { ch ->
                        ChannelTile(ch, onClick = { nav.navigate(Routes.player("channel", ch.id)) })
                    }
                }
                "continue" -> {
                    ContentRow(row.title, row.titles, key = { it.id }) { t ->
                        val p = t.progress
                        val sub = p?.episode?.let { stringResource(R.string.episode_label, it.season, it.number) }
                        WideCard(t.displayTitle, sub, t.backdropUrl ?: t.posterUrl, p?.fraction, onClick = {
                            if (p != null) nav.navigate(Routes.player("title", p.itemId, p.positionMs))
                            else nav.navigate(Routes.details(t.id))
                        })
                    }
                }
                else -> {
                    val filters = row.browse ?: emptyMap()
                    ContentRow(row.title, row.titles, key = { it.id },
                        onSeeAll = if (row.browse != null) ({ nav.navigate(Routes.browse("custom", filters + ("title" to row.title))) }) else null) { t ->
                        PosterCard(t, onClick = { nav.navigate(Routes.details(t.id)) })
                    }
                }
            }
        }
    }
}

/** Large cinematic banner; rotates through featured titles while the user is not interacting with it. */
@Composable
private fun HeroBanner(items: List<TitleCard>, nav: NavHostController, focus: FocusRequester) {
    var index by remember { mutableIntStateOf(0) }
    var hasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(hasFocus, items.size) {
        while (!hasFocus && items.size > 1) { delay(8000); index = (index + 1) % items.size }
    }
    val t = items[index.coerceIn(0, items.lastIndex)]
    Box(Modifier.fillMaxWidth().height(400.dp)) {
        AsyncImage(model = t.backdropUrl ?: t.posterUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brand.ScrimStart))
        Box(Modifier.fillMaxSize().background(Brand.ScrimBottom))
        Column(Modifier.align(Alignment.BottomStart).padding(start = 48.dp, bottom = 28.dp).widthIn(max = 620.dp)) {
            Text(t.displayTitle, style = MaterialTheme.typography.displaySmall, color = Brand.Text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                t.year?.let { Text("$it", color = Brand.Muted); Spacer(Modifier.width(12.dp)) }
                t.rating?.let { Text("★ $it", color = Brand.GoldLight); Spacer(Modifier.width(12.dp)) }
                QualityBadge(t.quality)
            }
            if (!t.overview.isNullOrBlank()) Text(t.overview, maxLines = 3, overflow = TextOverflow.Ellipsis, color = Brand.Text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.onFocusChanged { hasFocus = it.hasFocus }) {
                GoldButton(stringResource(R.string.watch_now), { nav.navigate(Routes.player("title", t.id)) },
                    Modifier.focusRequester(focus), icon = R.drawable.ic_play)
                Spacer(Modifier.width(14.dp))
                GoldButton(stringResource(R.string.details), { nav.navigate(Routes.details(t.id)) })
            }
        }
    }
}

/** Big live banner for the main news channels: logo, LIVE badge, country; rotates while not focused. */
@Composable
private fun ChannelHero(items: List<com.abumoath.tv.data.model.ChannelCard>, nav: NavHostController, focus: FocusRequester) {
    var index by remember { mutableIntStateOf(0) }
    var hasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(hasFocus, items.size) {
        while (!hasFocus && items.size > 1) { delay(7000); index = (index + 1) % items.size }
    }
    val c = items[index.coerceIn(0, items.lastIndex)]
    Box(
        Modifier.fillMaxWidth().height(330.dp)
            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(androidx.compose.ui.graphics.Color(0xFF1B2547), Brand.Black, androidx.compose.ui.graphics.Color(0xFF2A1F05))))
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 30.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(340.dp, 200.dp)
                    .background(androidx.compose.ui.graphics.Color(0x22FFFFFF), androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                    .padding(26.dp),
                contentAlignment = Alignment.Center
            ) {
                if (c.logoUrl != null) AsyncImage(model = c.logoUrl, contentDescription = c.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                else Text(c.name, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(40.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(Brand.Live, androidx.compose.foundation.shape.RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 3.dp)) {
                        Text("● " + stringResource(R.string.live_badge), color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(com.abumoath.tv.data.repo.flagOf(c.country) + "  " + (c.country?.let { com.abumoath.tv.data.source.TextTools.countryName(it, java.util.Locale.getDefault().language) } ?: ""), color = Brand.Muted)
                }
                Spacer(Modifier.height(12.dp))
                Text(c.name, style = MaterialTheme.typography.displaySmall, color = Brand.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(20.dp))
                Row(Modifier.onFocusChanged { hasFocus = it.hasFocus }) {
                    GoldButton(stringResource(R.string.watch_now), { nav.navigate(Routes.player("channel", c.id)) },
                        Modifier.focusRequester(focus), icon = R.drawable.ic_play)
                    Spacer(Modifier.width(14.dp))
                    GoldButton(stringResource(R.string.menu_live), { nav.navigate(Routes.live()) }, icon = R.drawable.ic_live_tv)
                }
                // Dots
                Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.indices.forEach { i ->
                        Box(Modifier.size(if (i == index) 22.dp else 8.dp, 8.dp)
                            .background(if (i == index) Brand.Gold else Brand.Line, androidx.compose.foundation.shape.RoundedCornerShape(4.dp)))
                    }
                }
            }
        }
    }
}
