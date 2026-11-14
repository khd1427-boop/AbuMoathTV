package com.abumoath.tv.ui.browse

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import com.abumoath.tv.data.model.Facet
import com.abumoath.tv.data.model.FacetsResponse
import com.abumoath.tv.data.model.TitleCard
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

/** Which facet the user must choose first (browse by country / language / genre). */
enum class PickMode { NONE, COUNTRY, LANGUAGE, GENRE }

data class BrowseState(
    val mode: String,
    val customTitle: String? = null,
    val filters: Map<String, String> = emptyMap(),
    val facets: FacetsResponse = FacetsResponse(),
    val pickMode: PickMode = PickMode.NONE,
    val items: List<TitleCard> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val loading: Boolean = false,
    val endReached: Boolean = false,
    val error: Int? = null
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val catalog: CatalogRepository
) : ViewModel() {
    private val mode: String = handle["mode"] ?: "movies"
    private val _state: MutableStateFlow<BrowseState>
    val state get() = _state.asStateFlow()
    private var job: Job? = null

    init {
        val raw = Uri.decode(handle.get<String>("filters") ?: "")
        val args = raw.split("&").filter { it.contains("=") }.associate { it.substringBefore("=") to it.substringAfter("=") }
        val base = when (mode) {
            "movies" -> mapOf("type" to "movie", "sort" to "new")
            "series" -> mapOf("type" to "series", "sort" to "new")
            "free" -> mapOf("free" to "true", "sort" to "top")
            "top" -> mapOf("sort" to "top")
            "new" -> mapOf("sort" to "new")
            else -> mapOf("sort" to "new")
        } + args.filterKeys { it != "title" }
        val pick = when (mode) {
            "country" -> PickMode.COUNTRY; "language" -> PickMode.LANGUAGE; "genre" -> PickMode.GENRE; else -> PickMode.NONE
        }
        _state = MutableStateFlow(BrowseState(mode = mode, customTitle = args["title"], filters = base, pickMode = if (base.keys.any { it in setOf("country", "language", "genre") }) PickMode.NONE else pick))
        viewModelScope.launch {
            runCatching { catalog.facets(base["type"]) }.onSuccess { f -> _state.update { it.copy(facets = f) } }
        }
        if (_state.value.pickMode == PickMode.NONE) reload()
    }

    fun setFilter(key: String, value: String?) {
        _state.update { s -> s.copy(filters = if (value == null) s.filters - key else s.filters + (key to value), pickMode = PickMode.NONE) }
        reload()
    }

    fun backToPicker(): Boolean {
        val pick = when (mode) { "country" -> PickMode.COUNTRY; "language" -> PickMode.LANGUAGE; "genre" -> PickMode.GENRE; else -> return false }
        if (_state.value.pickMode != PickMode.NONE) return false
        _state.update { it.copy(pickMode = pick, items = emptyList()) }
        return true
    }

    fun reload() {
        job?.cancel()
        _state.update { it.copy(items = emptyList(), page = 0, endReached = false, total = 0, error = null) }
        loadMore()
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || s.endReached) return
        _state.update { it.copy(loading = true) }
        job = viewModelScope.launch {
            try {
                val next = s.page + 1
                val r = catalog.titles(s.filters, next)
                _state.update { it.copy(items = it.items + r.items, page = next, total = r.total, loading = false, endReached = (it.items.size + r.items.size) >= r.total || r.items.isEmpty()) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = ApiErrors.messageFor(e)) }
            }
        }
    }
}

@Composable
fun BrowseScreen(nav: NavHostController, vm: BrowseViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    var openFilter by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = openFilter != null || (s.pickMode == PickMode.NONE && s.mode in setOf("country", "language", "genre"))) {
        if (openFilter != null) openFilter = null else if (!vm.backToPicker()) nav.popBackStack()
    }

    val title = s.customTitle ?: stringResource(
        when (s.mode) {
            "movies" -> R.string.menu_movies; "series" -> R.string.menu_series; "country" -> R.string.menu_by_country
            "language" -> R.string.menu_by_language; "genre" -> R.string.menu_by_genre; "free" -> R.string.menu_free
            "top" -> R.string.menu_top; else -> R.string.menu_new
        }
    )

    if (s.pickMode != PickMode.NONE) {
        val (list, key) = when (s.pickMode) {
            PickMode.COUNTRY -> s.facets.countries to "country"
            PickMode.LANGUAGE -> s.facets.languages to "language"
            else -> s.facets.genres to "genre"
        }
        FacetPicker(title, list) { vm.setFilter(key, it.id) }
        return
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
            Row(Modifier.padding(horizontal = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(16.dp))
                if (s.total > 0) Text(stringResource(R.string.results_count, s.total), color = Brand.Muted)
            }
            FilterBar(s, onSort = { vm.setFilter("sort", it) }, onOpen = { openFilter = it })
            when {
                s.error != null && s.items.isEmpty() -> ErrorView(stringResource(s.error!!), vm::reload)
                s.items.isEmpty() && s.loading -> LoadingView()
                s.items.isEmpty() -> EmptyView()
                else -> TitleGrid(s.items, onEnd = vm::loadMore) { nav.navigate(Routes.details(it.id)) }
            }
        }
        openFilter?.let { key ->
            val sel = s.filters[key]
            val options: List<Facet> = when (key) {
                "country" -> s.facets.countries
                "language" -> s.facets.languages
                "genre" -> s.facets.genres
                "decade" -> s.facets.decades
                "quality" -> listOf(Facet("FHD", "Full HD 1080p"), Facet("HD", "HD 720p"), Facet("SD", "SD"))
                "duration" -> listOf(Facet("short", stringResource(R.string.duration_short)), Facet("medium", stringResource(R.string.duration_medium)), Facet("long", stringResource(R.string.duration_long)))
                else -> emptyList()
            }
            OptionsPanel(options, sel, onPick = { vm.setFilter(key, it); openFilter = null })
        }
    }
}

@Composable
private fun FilterBar(s: BrowseState, onSort: (String) -> Unit, onOpen: (String) -> Unit) {
    val sorts = listOf("new" to R.string.sort_new, "top" to R.string.sort_top, "rating" to R.string.sort_rating, "year" to R.string.sort_year)
    val filters = buildList {
        if (s.facets.countries.isNotEmpty()) add("country" to R.string.filter_country)
        if (s.facets.languages.isNotEmpty()) add("language" to R.string.filter_language)
        if (s.facets.genres.isNotEmpty()) add("genre" to R.string.filter_genre)
        add("quality" to R.string.filter_quality)
        if (s.facets.decades.isNotEmpty()) add("decade" to R.string.filter_decade)
        add("duration" to R.string.filter_duration)
    }
    LazyRow(contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(sorts, key = { "s" + it.first }) { (k, label) -> Chip(stringResource(label), s.filters["sort"] == k, { onSort(k) }) }
        item { Spacer(Modifier.width(20.dp)) }
        items(filters, key = { "f" + it.first }) { (k, label) ->
            val v = s.filters[k]
            val shown = if (v == null) stringResource(label) else {
                val name = (s.facets.countries + s.facets.languages + s.facets.genres + s.facets.decades).firstOrNull { it.id == v }?.name ?: v
                "${stringResource(label)}: $name"
            }
            Chip(shown, v != null, { onOpen(k) })
        }
    }
}

@Composable
fun TitleGrid(items: List<TitleCard>, onEnd: () -> Unit, onClick: (TitleCard) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(start = 40.dp, end = 40.dp, bottom = 40.dp, top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(items, key = { _, t -> t.id }) { i, t ->
            if (i >= items.size - 12) LaunchedEffect(items.size) { onEnd() } // infinite scrolling
            PosterCard(t, onClick = { onClick(t) }, width = 150.dp)
        }
    }
}

@Composable
private fun FacetPicker(title: String, list: List<Facet>, onPick: (Facet) -> Unit) {
    val fr = remember { FocusRequester() }
    Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 40.dp))
        if (list.isEmpty()) { LoadingView(); return }
        LaunchedEffect(list.size) { runCatching { fr.requestFocus() } }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(230.dp),
            contentPadding = PaddingValues(40.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(list.size, key = { list[it].id }) { i ->
                val f = list[i]
                OptionRow(f.name, false, { onPick(f) }, if (i == 0) Modifier.focusRequester(fr) else Modifier, trailing = f.count.toString())
            }
        }
    }
}

/** Side panel listing the options of one filter. */
@Composable
fun OptionsPanel(options: List<Facet>, selected: String?, onPick: (String?) -> Unit) {
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.CenterEnd) {
        LazyColumn(
            Modifier.width(380.dp).fillMaxHeight().background(Brand.Surface, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { OptionRow(stringResource(R.string.all), selected == null, { onPick(null) }, Modifier.focusRequester(fr)) }
            items(options, key = { it.id }) { o -> OptionRow(o.name, o.id == selected, { onPick(o.id) }, trailing = if (o.count > 0) o.count.toString() else null) }
        }
    }
}
