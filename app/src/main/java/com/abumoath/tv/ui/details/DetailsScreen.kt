package com.abumoath.tv.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.abumoath.tv.R
import com.abumoath.tv.data.model.TitleDetails
import com.abumoath.tv.data.repo.CatalogRepository
import com.abumoath.tv.data.repo.LibraryRepository
import com.abumoath.tv.ui.components.*
import com.abumoath.tv.ui.navigation.Routes
import com.abumoath.tv.ui.theme.Brand
import com.abumoath.tv.util.ApiErrors
import com.abumoath.tv.util.Format
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

sealed interface DetailsUi {
    data object Loading : DetailsUi
    data class Ready(val d: TitleDetails) : DetailsUi
    data class Error(val msg: Int) : DetailsUi
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val catalog: CatalogRepository,
    private val library: LibraryRepository
) : ViewModel() {
    val id: Long = handle["id"] ?: 0L
    private val _ui = MutableStateFlow<DetailsUi>(DetailsUi.Loading)
    val ui = _ui.asStateFlow()
    val isFavorite = library.observe("favorite", "title", id).stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val inWatchlist = library.observe("watchlist", "title", id).stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun load() {
        viewModelScope.launch {
            try { _ui.value = DetailsUi.Ready(catalog.title(id)) }
            catch (e: Exception) { if (_ui.value !is DetailsUi.Ready) _ui.value = DetailsUi.Error(ApiErrors.messageFor(e)) }
        }
    }

    fun toggle(list: String, current: Boolean) = viewModelScope.launch { library.setInList(list, "title", id, !current) }
}

@Composable
fun DetailsScreen(nav: NavHostController, vm: DetailsViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.load() } // reload on return from the player (fresh progress)
    when (val s = ui) {
        DetailsUi.Loading -> LoadingView()
        is DetailsUi.Error -> ErrorView(stringResource(s.msg), vm::load)
        is DetailsUi.Ready -> DetailsContent(s.d, vm, nav)
    }
}

@Composable
private fun DetailsContent(d: TitleDetails, vm: DetailsViewModel, nav: NavHostController) {
    val fav by vm.isFavorite.collectAsStateWithLifecycle()
    val watch by vm.inWatchlist.collectAsStateWithLifecycle()
    val playFocus = remember { FocusRequester() }
    LaunchedEffect(d.id) { runCatching { playFocus.requestFocus() } }

    Box(Modifier.fillMaxSize().background(Brand.Black)) {
        AsyncImage(model = d.posterUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), alpha = 0.35f)
        Box(Modifier.fillMaxSize().background(Brand.ScrimStart))
        Row(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 48.dp)) {
            AsyncImage(model = d.posterUrl, contentDescription = d.displayTitle, contentScale = ContentScale.Crop,
                modifier = Modifier.width(220.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(12.dp)).background(Brand.Surface2))
            Spacer(Modifier.width(36.dp))
            Column(Modifier.widthIn(max = 780.dp).verticalScroll(rememberScrollState())) {
                Text(d.displayTitle, style = MaterialTheme.typography.displaySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val parts = listOfNotNull(d.year?.toString(), d.languageName, d.runtimeMin?.let { stringResource(R.string.duration_min, it) })
                    Text(parts.joinToString("  •  "), color = Brand.Muted)
                    Spacer(Modifier.width(12.dp))
                    QualityBadge(d.quality)
                }
                if (d.genres.isNotEmpty()) Text(d.genres.joinToString("، ") { it.name }, color = Brand.GoldLight, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(14.dp))
                if (d.overview.isNotBlank()) Text(d.overview, style = MaterialTheme.typography.bodyLarge, maxLines = 6, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(22.dp))

                val p = d.progress
                val canPlay = d.streams.isNotEmpty()
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (canPlay && p != null && !p.completed && p.positionMs > 30_000) {
                        GoldButton(stringResource(R.string.resume), { nav.navigate(Routes.player("title", d.id, p.positionMs)) }, Modifier.focusRequester(playFocus), icon = R.drawable.ic_play)
                        GoldButton(stringResource(R.string.start_over), { nav.navigate(Routes.player("title", d.id, 0)) })
                    } else if (canPlay) {
                        GoldButton(stringResource(R.string.watch_now), { nav.navigate(Routes.player("title", d.id, 0)) }, Modifier.focusRequester(playFocus), icon = R.drawable.ic_play)
                    }
                    GoldButton(stringResource(if (fav) R.string.remove_favorite else R.string.add_favorite), { vm.toggle("favorite", fav) },
                        if (canPlay) Modifier else Modifier.focusRequester(playFocus))
                    GoldButton(stringResource(if (watch) R.string.remove_watchlist else R.string.add_watchlist), { vm.toggle("watchlist", watch) })
                }
                if (!canPlay) Text(stringResource(R.string.no_streams), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
                Spacer(Modifier.height(22.dp))

                @Composable
                fun line(label: Int, value: String) {
                    if (value.isBlank()) return
                    Row(Modifier.padding(vertical = 3.dp)) {
                        Text(stringResource(label) + ": ", color = Brand.Muted)
                        Text(value, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                line(R.string.director, d.directors.joinToString("، "))
                line(R.string.quality_available, d.qualities.mapNotNull { Format.qualityLabel(it) }.joinToString(" / "))
                // Only subtitles that really exist are listed; nothing is claimed when there are none.
                if (d.subtitles.isEmpty()) Text(stringResource(R.string.no_subtitles), color = Brand.Muted, style = MaterialTheme.typography.bodySmall)
                else line(R.string.subtitles_available, d.subtitles.joinToString("، ") { it.label ?: Locale(it.language).displayLanguage })
                d.license?.let { Text(stringResource(R.string.license_label, licenseName(it)), color = Brand.Muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp)) }
                d.sourcePage?.let { Text(it, color = Brand.Muted, style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

private fun licenseName(url: String) = when {
    url.contains("publicdomain", true) -> "Public Domain"
    url.contains("creativecommons", true) -> "Creative Commons (" + url.substringAfter("licenses/", "").trim('/').uppercase() + ")"
    else -> url
}
