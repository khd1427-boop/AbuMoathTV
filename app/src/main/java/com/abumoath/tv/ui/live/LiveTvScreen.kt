package com.abumoath.tv.ui.live

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
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
import com.abumoath.tv.data.model.FacetsResponse
import com.abumoath.tv.data.repo.CatalogRepository
import com.abumoath.tv.ui.components.*
import com.abumoath.tv.ui.navigation.Routes
import com.abumoath.tv.ui.theme.Brand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Countries from the product spec, shown first (in this order) when they have channels. */
private val PRIORITY = listOf("SA", "AE", "KW", "QA", "BH", "OM", "EG", "IQ", "JO", "PS", "SY", "LB", "MA", "DZ", "TN", "SD", "YE",
    "TR", "IN", "US", "GB", "FR", "DE", "IT", "ES", "KR", "JP", "CN")

data class LiveState(
    val facets: FacetsResponse = FacetsResponse(),
    val country: String? = null,
    val category: String? = null,
    val items: List<ChannelCard> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val loading: Boolean = false,
    val end: Boolean = false,
    val error: Int? = null
)

@HiltViewModel
class LiveViewModel @Inject constructor(handle: SavedStateHandle, private val catalog: CatalogRepository) : ViewModel() {
    private val _s = MutableStateFlow(LiveState(category = handle.get<String>("category")?.takeIf { it.isNotBlank() }, country = handle.get<String>("country")?.takeIf { it.isNotBlank() }))
    val state = _s.asStateFlow()
    private var job: Job? = null

    init {
        viewModelScope.launch {
            runCatching { catalog.facets("channel") }.onSuccess { f ->
                val sorted = f.countries.sortedWith(compareBy({ PRIORITY.indexOf(it.id).let { i -> if (i < 0) 999 else i } }, { -it.count }))
                _s.update { it.copy(facets = f.copy(countries = sorted)) }
            }
        }
        reload()
    }

    fun select(country: String? = _s.value.country, category: String? = _s.value.category) {
        _s.update { it.copy(country = country, category = category) }
        reload()
    }

    fun reload() {
        job?.cancel()
        _s.update { it.copy(items = emptyList(), page = 0, end = false, loading = false, error = null) }
        more()
    }

    fun more() {
        val s = _s.value
        if (s.loading || s.end) return
        _s.update { it.copy(loading = true) }
        job = viewModelScope.launch {
            try {
                val f = buildMap { s.country?.let { put("country", it) }; s.category?.let { put("category", it) } }
                val r = catalog.channels(f, s.page + 1)
                _s.update { it.copy(items = it.items + r.items, page = s.page + 1, total = r.total, loading = false, end = it.items.size + r.items.size >= r.total || r.items.isEmpty()) }
            } catch (e: Exception) { _s.update { it.copy(loading = false, error = ApiErrors.messageFor(e)) } }
        }
    }
}

@Composable
fun LiveTvScreen(nav: NavHostController, vm: LiveViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    Row(Modifier.fillMaxSize()) {
        // Country list
        LazyColumn(
            Modifier.width(250.dp).fillMaxHeight().background(Brand.Surface.copy(alpha = 0.6f)),
            contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { Text(stringResource(R.string.live_countries), style = MaterialTheme.typography.titleMedium, color = Brand.GoldLight, modifier = Modifier.padding(6.dp)) }
            item { OptionRow("🌍  " + stringResource(R.string.all), s.country == null, { vm.select(country = null) }) }
            items(s.facets.countries, key = { it.id }) { c -> OptionRow(com.abumoath.tv.data.repo.flagOf(c.id) + "  " + c.name, s.country == c.id, { vm.select(country = c.id) }, trailing = c.count.toString()) }
        }
        Column(Modifier.weight(1f).fillMaxHeight().padding(top = 20.dp)) {
            Row(Modifier.padding(horizontal = 32.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(s.country?.let { com.abumoath.tv.data.repo.flagOf(it) + "  " + (s.facets.countries.firstOrNull { f -> f.id == it }?.name ?: it) }
                    ?: stringResource(R.string.menu_live), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(16.dp))
                if (s.total > 0) Text(stringResource(R.string.channels_count, s.total), color = Brand.Muted)
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Chip(stringResource(R.string.all), s.category == null, { vm.select(category = null) }) }
                items(s.facets.categories, key = { it.id }) { c -> Chip(c.name, s.category == c.id, { vm.select(category = c.id) }) }
            }
            when {
                s.error != null && s.items.isEmpty() -> ErrorView(stringResource(s.error!!), vm::reload)
                s.items.isEmpty() && (s.loading || s.page == 0) -> LoadingView()
                s.items.isEmpty() -> EmptyView()
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(190.dp),
                    contentPadding = PaddingValues(start = 32.dp, end = 32.dp, bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    itemsIndexed(s.items, key = { _, c -> c.id }) { i, c ->
                        if (i >= s.items.size - 12) LaunchedEffect(s.items.size) { vm.more() }
                        ChannelTile(c, onClick = { nav.navigate(Routes.player("channel", c.id)) }, width = 190.dp)
                    }
                }
            }
        }
    }
}
