package com.abumoath.tv.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.abumoath.tv.util.ApiErrors
import com.abumoath.tv.data.model.ChannelCard
import com.abumoath.tv.data.model.TitleCard
import com.abumoath.tv.data.repo.LibraryRepository
import com.abumoath.tv.ui.components.*
import com.abumoath.tv.ui.navigation.Routes
import com.abumoath.tv.ui.theme.Brand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** An entry is either a title (movie/series) or a channel. */
data class LibEntry(val title: TitleCard? = null, val channel: ChannelCard? = null)

data class LibraryState(val tab: String, val items: List<LibEntry> = emptyList(), val loading: Boolean = true, val error: Int? = null, val confirmClear: Boolean = false)

@HiltViewModel
class LibraryViewModel @Inject constructor(handle: SavedStateHandle, private val repo: LibraryRepository) : ViewModel() {
    private val _s = MutableStateFlow(LibraryState(tab = handle["tab"] ?: "favorite"))
    val state = _s.asStateFlow()

    fun setTab(tab: String) { _s.update { it.copy(tab = tab, confirmClear = false) }; load() }

    fun load() {
        _s.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val tab = _s.value.tab
                val items = if (tab == "history") repo.history().map { LibEntry(title = it) }
                else repo.list(tab).let { (t, c) -> c.map { LibEntry(channel = it) } + t.map { LibEntry(title = it) } }
                _s.update { it.copy(items = items, loading = false) }
            } catch (e: Exception) { _s.update { it.copy(loading = false, error = ApiErrors.messageFor(e)) } }
        }
    }

    fun remove(e: LibEntry) = viewModelScope.launch {
        runCatching {
            val tab = _s.value.tab
            when {
                tab == "history" && e.title != null -> repo.deleteHistory(e.title)
                e.channel != null -> repo.setInList(tab, "channel", e.channel.id, false)
                e.title != null -> repo.setInList(tab, "title", e.title.id, false)
            }
        }
        load()
    }

    fun askClear(v: Boolean) = _s.update { it.copy(confirmClear = v) }
    fun clearHistory() = viewModelScope.launch { runCatching { repo.clearHistory() }; _s.update { it.copy(confirmClear = false) }; load() }
}

@Composable
fun LibraryScreen(nav: NavHostController, vm: LibraryViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    var editMode by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
        Row(Modifier.padding(horizontal = 40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Chip(stringResource(R.string.menu_favorites), s.tab == "favorite", { vm.setTab("favorite") })
            Chip(stringResource(R.string.menu_watchlist), s.tab == "watchlist", { vm.setTab("watchlist") })
            Chip(stringResource(R.string.menu_history), s.tab == "history", { vm.setTab("history") })
            Spacer(Modifier.width(30.dp))
            if (s.items.isNotEmpty()) Chip(stringResource(R.string.delete), editMode, { editMode = !editMode })
            if (s.tab == "history" && s.items.isNotEmpty()) Chip(stringResource(R.string.clear_all), s.confirmClear, { vm.askClear(true) })
        }
        if (s.confirmClear) {
            Row(Modifier.padding(horizontal = 40.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.confirm_clear_history), color = Brand.Text)
                Spacer(Modifier.width(16.dp))
                GoldButton(stringResource(R.string.ok), { vm.clearHistory() })
                Spacer(Modifier.width(10.dp))
                GoldButton(stringResource(R.string.cancel), { vm.askClear(false) })
            }
        }
        if (editMode) Text(stringResource(R.string.remove_from_history), color = Brand.GoldLight, modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall)
        when {
            s.loading && s.items.isEmpty() -> LoadingView()
            s.error != null -> ErrorView(stringResource(s.error!!), vm::load)
            s.items.isEmpty() -> EmptyView()
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(40.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                items(s.items, key = { (it.title?.id ?: -(it.channel?.id ?: 0L)).toString() + it.title?.progress?.itemId }) { e ->
                    when {
                        e.channel != null -> ChannelTile(e.channel, onClick = {
                            if (editMode) vm.remove(e) else nav.navigate(Routes.player("channel", e.channel.id))
                        }, width = 160.dp)
                        e.title != null -> {
                            val t = e.title
                            PosterCard(t, onClick = {
                                val p = t.progress
                                when {
                                    editMode -> vm.remove(e)
                                    s.tab == "history" && p != null && !p.completed -> nav.navigate(Routes.player("title", p.itemId, p.positionMs))
                                    else -> nav.navigate(Routes.details(t.id))
                                }
                            }, width = 160.dp)
                        }
                    }
                }
            }
        }
    }
}
