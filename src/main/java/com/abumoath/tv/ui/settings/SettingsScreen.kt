package com.abumoath.tv.ui.settings

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.imageLoader
import com.abumoath.tv.BuildConfig
import com.abumoath.tv.R
import com.abumoath.tv.data.prefs.AppSettings
import com.abumoath.tv.data.prefs.PlaybackPrefs
import com.abumoath.tv.data.repo.CatalogRepository
import com.abumoath.tv.data.repo.LibraryRepository
import com.abumoath.tv.data.source.ContentSync
import com.abumoath.tv.ui.components.OptionRow
import com.abumoath.tv.ui.navigation.Routes
import com.abumoath.tv.ui.theme.Brand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settings: AppSettings,
    private val catalog: CatalogRepository,
    private val library: LibraryRepository,
    val sync: ContentSync
) : ViewModel() {
    val playback = settings.playback

    private val _speed = MutableStateFlow<String?>(null)
    val speed = _speed.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun set(f: (PlaybackPrefs) -> PlaybackPrefs) = settings.updatePlayback(f)

    /** Measures throughput and caps adaptive streaming to ~70 % of it (leaves headroom, fewer stalls). */
    fun runSpeedTest(ctx: Context) {
        _speed.value = ctx.getString(R.string.speed_testing)
        viewModelScope.launch {
            runCatching { catalog.speedTestMbps() }.onSuccess { mbps ->
                val suggested = when { mbps >= 8 -> "Full HD"; mbps >= 4 -> "HD"; else -> "SD" }
                settings.updatePlayback { it.copy(maxBitrate = (mbps * 0.7 * 1_000_000).toInt().coerceAtLeast(400_000)) }
                _speed.value = ctx.getString(R.string.speed_result, String.format(Locale.US, "%.1f", mbps), suggested)
            }.onFailure { _speed.value = ctx.getString(R.string.speed_failed) }
        }
    }

    fun clearHistory(ctx: Context) = viewModelScope.launch {
        runCatching { library.clearHistory() }.onSuccess { _message.value = ctx.getString(R.string.history_cleared) }
    }

    fun updateContent(ctx: Context) = viewModelScope.launch {
        _message.value = ctx.getString(R.string.sync_running)
        val r = sync.sync()
        catalog.invalidate()
        _message.value = if (r is ContentSync.Status.Done) ctx.getString(R.string.sync_done, r.channels, r.movies) else ctx.getString(R.string.error_network)
    }

    fun consumeMessage() { _message.value = null }
}

@Composable
fun SettingsScreen(nav: NavHostController, vm: SettingsViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val p by vm.playback.collectAsStateWithLifecycle()
    val speed by vm.speed.collectAsStateWithLifecycle()
    val msg by vm.message.collectAsStateWithLifecycle()
    LaunchedEffect(msg) { msg?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show(); vm.consumeMessage() } }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 48.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Text(stringResource(R.string.menu_settings), style = MaterialTheme.typography.headlineMedium) }

        section(R.string.settings_language)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("ar" to R.string.lang_ar, "en" to R.string.lang_en).forEach { (code, label) ->
                    Box(Modifier.weight(1f)) {
                        OptionRow(stringResource(label), vm.settings.language == code, {
                            if (vm.settings.language != code) { vm.settings.language = code; (ctx as? Activity)?.recreate() }
                        })
                    }
                }
            }
        }

        section(R.string.settings_quality)
        choices(listOf("auto" to R.string.q_auto, "FHD" to R.string.q_fhd, "HD" to R.string.q_hd, "SD" to R.string.q_sd), p.defaultQuality) { v -> vm.set { it.copy(defaultQuality = v) } }

        section(R.string.settings_subtitle_lang)
        choices(listOf("ara" to R.string.sub_arabic, "eng" to R.string.sub_english, "off" to R.string.sub_off), p.subtitleLang) { v -> vm.set { it.copy(subtitleLang = v) } }

        section(R.string.settings_subtitle_size)
        choices(listOf("0.8" to R.string.size_small, "1.0" to R.string.size_medium, "1.25" to R.string.size_large, "1.5" to R.string.size_xlarge), p.subtitleSize.toString()) { v -> vm.set { it.copy(subtitleSize = v.toFloat()) } }

        section(R.string.settings_subtitle_color)
        choices(listOf("${0xFFFFFFFF.toInt()}" to R.string.color_white, "${0xFFFFEB3B.toInt()}" to R.string.color_yellow, "${0xFF80DEEA.toInt()}" to R.string.color_cyan), p.subtitleColor.toString()) { v -> vm.set { it.copy(subtitleColor = v.toInt()) } }

        section(R.string.settings_subtitle_bg)
        choices(listOf("none" to R.string.bg_none, "semi" to R.string.bg_semi, "black" to R.string.bg_black), p.subtitleBackground) { v -> vm.set { it.copy(subtitleBackground = v) } }

        section(R.string.settings_subtitle_position)
        choices(listOf("0.08" to R.string.pos_bottom, "0.15" to R.string.pos_raised, "0.25" to R.string.pos_high), p.subtitlePosition.toString()) { v -> vm.set { it.copy(subtitlePosition = v.toFloat()) } }

        section(R.string.settings_speed_test)
        item { OptionRow(speed ?: stringResource(R.string.settings_speed_test), false, { vm.runSpeedTest(ctx) }) }

        section(R.string.settings_content)
        item {
            val last = vm.sync.lastSync
            val text = if (last > 0) stringResource(R.string.last_update, java.text.DateFormat.getDateTimeInstance().format(java.util.Date(last))) else ""
            if (text.isNotEmpty()) Text(text, color = Brand.Muted)
        }
        item { OptionRow(stringResource(R.string.update_content_now), false, { vm.updateContent(ctx) }) }

        section(R.string.settings_privacy)
        item { OptionRow(stringResource(R.string.clear_history), false, { vm.clearHistory(ctx) }) }
        item { OptionRow(stringResource(R.string.manage_favorites), false, { nav.navigate(Routes.library("favorite")) }) }
        item {
            OptionRow(stringResource(R.string.clear_cache), false, {
                ctx.imageLoader.memoryCache?.clear(); ctx.imageLoader.diskCache?.clear()
                java.io.File(ctx.cacheDir, "subs").deleteRecursively()
                Toast.makeText(ctx, R.string.cache_cleared, Toast.LENGTH_SHORT).show()
            })
        }

        section(R.string.settings_about)
        item { Text(stringResource(R.string.version, BuildConfig.VERSION_NAME), color = Brand.Text) }
        item {
            Column(Modifier.background(Brand.Surface, RoundedCornerShape(10.dp)).padding(16.dp)) {
                Text(stringResource(R.string.about_text), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.tmdb_attribution), style = MaterialTheme.typography.labelSmall, color = Brand.Muted)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(title: Int) {
    item { Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = Brand.GoldLight, modifier = Modifier.padding(top = 18.dp, bottom = 4.dp)) }
}

private fun androidx.compose.foundation.lazy.LazyListScope.choices(options: List<Pair<String, Int>>, current: String, onPick: (String) -> Unit) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { (v, label) -> Box(Modifier.weight(1f)) { OptionRow(stringResource(label), v == current, { onPick(v) }) } }
        }
    }
}
