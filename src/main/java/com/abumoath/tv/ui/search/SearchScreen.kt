package com.abumoath.tv.ui.search

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.abumoath.tv.R
import com.abumoath.tv.data.prefs.AppSettings
import com.abumoath.tv.util.ApiErrors
import com.abumoath.tv.data.model.SearchResults
import com.abumoath.tv.data.repo.CatalogRepository
import com.abumoath.tv.ui.components.*
import com.abumoath.tv.ui.navigation.Routes
import com.abumoath.tv.ui.theme.Brand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val suggestions: List<String> = emptyList(),
    val results: SearchResults = SearchResults(),
    val searched: Boolean = false,
    val loading: Boolean = false,
    val error: Int? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(private val catalog: CatalogRepository, val settings: AppSettings) : ViewModel() {
    private val _s = MutableStateFlow(SearchState())
    val state = _s.asStateFlow()
    private var job: Job? = null

    /** Debounced search-as-you-type with suggestions. */
    fun onQuery(q: String) {
        _s.update { it.copy(query = q) }
        job?.cancel()
        if (q.isBlank()) { _s.update { SearchState() }; return }
        job = viewModelScope.launch {
            delay(350)
            runCatching { catalog.suggest(q) }.onSuccess { r -> _s.update { it.copy(suggestions = r) } }
            search(q)
        }
    }

    fun search(q: String = _s.value.query) {
        if (q.isBlank()) return
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            try {
                val r = catalog.search(q.trim())
                _s.update { it.copy(results = r, searched = true, loading = false) }
            } catch (e: Exception) { _s.update { it.copy(loading = false, error = ApiErrors.messageFor(e)) } }
        }
    }
}

@Composable
fun SearchScreen(nav: NavHostController, vm: SearchViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }

    // Voice search through the system recognizer (Google Assistant on Android TV), shown only if available.
    val voiceIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (vm.settings.language == "ar") "ar-SA" else "en-US")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.voice_prompt))
    }
    val voiceAvailable = remember { voiceIntent.resolveActivity(context.packageManager) != null }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { vm.onQuery(it) }
        }
    }

    val r = s.results

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 28.dp, bottom = 40.dp)) {
        item {
            Row(Modifier.padding(horizontal = 40.dp), verticalAlignment = Alignment.Bottom) {
                TvTextField(s.query, vm::onQuery, stringResource(R.string.menu_search), Modifier.weight(1f).focusRequester(fr),
                    imeAction = ImeAction.Search, onImeAction = { vm.search() })
                if (voiceAvailable) {
                    Spacer(Modifier.width(12.dp))
                    GoldButton(stringResource(R.string.voice_search), { launcher.launch(voiceIntent) }, icon = R.drawable.ic_mic)
                }
            }
            Text(stringResource(R.string.search_hint), color = Brand.Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 40.dp, vertical = 6.dp))
        }
        if (s.suggestions.isNotEmpty()) item {
            LazyRow(contentPadding = PaddingValues(horizontal = 40.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(s.suggestions) { sug -> Chip(sug, false, { vm.onQuery(sug) }) }
            }
        }
        if (s.error != null) item { Text(stringResource(s.error!!), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(40.dp)) }
        if (s.searched && r.isEmpty && !s.loading) item { Text(stringResource(R.string.search_no_results), color = Brand.Muted, modifier = Modifier.padding(40.dp)) }
        if (r.channels.isNotEmpty()) item {
            ContentRow(stringResource(R.string.result_channels), r.channels, key = { it.id }) { c ->
                ChannelTile(c, onClick = { nav.navigate(Routes.player("channel", c.id)) })
            }
        }
        if (r.titles.isNotEmpty()) item {
            ContentRow(stringResource(R.string.result_movies), r.titles, key = { it.id }) { t ->
                PosterCard(t, onClick = { nav.navigate(Routes.details(t.id)) })
            }
        }
        if (r.countries.isNotEmpty() || r.categories.isNotEmpty()) item {
            Column(Modifier.padding(horizontal = 40.dp, vertical = 10.dp)) {
                Text(stringResource(R.string.result_countries), style = MaterialTheme.typography.titleLarge)
                LazyRow(contentPadding = PaddingValues(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(r.countries) { f -> Chip(f.name, false, { nav.navigate(Routes.liveCountry(f.id)) }) }
                    items(r.categories) { f -> Chip(f.name, false, { nav.navigate(Routes.live(f.id)) }) }
                }
            }
        }
    }
}
