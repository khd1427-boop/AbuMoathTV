@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.abumoath.tv.ui.player

import android.graphics.Color as AColor
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import androidx.navigation.NavHostController
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.abumoath.tv.R
import com.abumoath.tv.data.prefs.PlaybackPrefs
import com.abumoath.tv.ui.components.FocusCard
import com.abumoath.tv.ui.components.GoldButton
import com.abumoath.tv.ui.components.OptionRow
import com.abumoath.tv.ui.navigation.Routes
import com.abumoath.tv.ui.theme.Brand
import com.abumoath.tv.util.Format
import kotlinx.coroutines.delay
import java.util.Locale

private enum class Panel { QUALITY, AUDIO, TEXT, SYNC }

@Composable
fun PlayerScreen(nav: NavHostController, vm: PlayerViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val player = vm.player
    var controlsVisible by remember { mutableStateOf(true) }
    var panel by remember { mutableStateOf<Panel?>(null) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(true) }
    val rootFocus = remember { FocusRequester() }
    val playFocus = remember { FocusRequester() }

    fun poke() { lastInteraction = System.currentTimeMillis(); controlsVisible = true }

    // Clock for the progress bar.
    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition
            duration = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
            isPlaying = player.isPlaying
            delay(if (controlsVisible) 500 else 1500)
        }
    }
    // Auto-hide controls after 5 s of inactivity (only while playing and with no panel open).
    LaunchedEffect(lastInteraction, controlsVisible, panel, isPlaying) {
        if (controlsVisible && panel == null && isPlaying) {
            delay(5000)
            if (System.currentTimeMillis() - lastInteraction >= 4900) { controlsVisible = false; runCatching { rootFocus.requestFocus() } }
        }
    }
    LaunchedEffect(controlsVisible) { if (controlsVisible && panel == null) runCatching { playFocus.requestFocus() } }

    BackHandler {
        when {
            panel != null -> panel = null
            ui.nextCountdown != null -> vm.cancelCountdown()
            controlsVisible && isPlaying -> { controlsVisible = false; runCatching { rootFocus.requestFocus() } }
            else -> nav.popBackStack()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { ev ->
                if (ev.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                lastInteraction = System.currentTimeMillis()
                // Media keys work everywhere.
                when (ev.key) {
                    Key.MediaPlayPause -> { if (player.isPlaying) player.pause() else player.play(); poke(); return@onPreviewKeyEvent true }
                    Key.MediaPlay -> { player.play(); return@onPreviewKeyEvent true }
                    Key.MediaPause -> { player.pause(); poke(); return@onPreviewKeyEvent true }
                    Key.MediaFastForward -> { if (!ui.isLive) player.seekForward(); poke(); return@onPreviewKeyEvent true }
                    Key.MediaRewind -> { if (!ui.isLive) player.seekBack(); poke(); return@onPreviewKeyEvent true }
                    Key.MediaNext -> { vm.playNext(); return@onPreviewKeyEvent true }
                }
                // With controls hidden: arrows seek, OK toggles pause and shows controls.
                if (!controlsVisible && panel == null) {
                    when (ev.key) {
                        Key.DirectionRight -> { if (!ui.isLive) player.seekForward(); return@onPreviewKeyEvent true }
                        Key.DirectionLeft -> { if (!ui.isLive) player.seekBack(); return@onPreviewKeyEvent true }
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> { if (player.isPlaying) player.pause() else player.play(); poke(); return@onPreviewKeyEvent true }
                        Key.DirectionUp, Key.DirectionDown -> { poke(); return@onPreviewKeyEvent true }
                    }
                }
                false
            }
            .focusRequester(rootFocus)
            .focusable()
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(AColor.BLACK)
                    keepScreenOn = true
                    isFocusable = false
                    this.player = player
                    styleSubtitles(subtitleView, vm.subtitlePrefs)
                }
            },
            update = { it.player = player },
            modifier = Modifier.fillMaxSize()
        )

        if (ui.loading && ui.error == null) {
            Box(Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(20.dp)) {
                Text(stringResource(ui.info ?: R.string.player_buffering), color = Brand.Text)
            }
        } else ui.info?.let {
            Box(Modifier.align(Alignment.TopCenter).padding(top = 30.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(10.dp)).padding(14.dp)) {
                Text(stringResource(it), color = Brand.GoldLight)
            }
        }

        ui.error?.let { err ->
            Column(Modifier.align(Alignment.Center).background(Brand.Surface, RoundedCornerShape(16.dp)).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(err), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                val fr = remember { FocusRequester() }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GoldButton(stringResource(R.string.retry), vm::retryAfterError, Modifier.focusRequester(fr))
                    GoldButton(stringResource(R.string.player_back_details), { nav.popBackStack() })
                }
                LaunchedEffect(err) { runCatching { fr.requestFocus() } }
            }
        }

        ui.nextCountdown?.let { n ->
            Column(Modifier.align(Alignment.BottomEnd).padding(48.dp).background(Brand.Surface, RoundedCornerShape(12.dp)).padding(20.dp)) {
                Text(stringResource(R.string.player_next_in, n), color = Brand.GoldLight)
                Spacer(Modifier.height(10.dp))
                val fr = remember { FocusRequester() }
                GoldButton(stringResource(R.string.player_next_episode), vm::playNext, Modifier.focusRequester(fr))
                LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }
            }
        }

        AnimatedVisibility(visible = controlsVisible && ui.error == null, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                // Top: title
                Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent))).padding(horizontal = 48.dp, vertical = 28.dp)) {
                    Text(ui.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    ui.subtitle?.let { Text(it, color = Brand.GoldLight, style = MaterialTheme.typography.titleSmall) }
                }
                // Bottom: progress + buttons
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
                        .padding(horizontal = 48.dp, vertical = 26.dp)
                ) {
                    if (!ui.isLive) SeekBar(position, duration, onSeek = { player.seekTo(it); poke() }, onInteract = { poke() })
                    else Text("● " + stringResource(R.string.live_now), color = Brand.Live, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.onFocusChanged { if (it.hasFocus) poke() }) {
                        if (!ui.isLive) CtrlButton(R.drawable.ic_replay10, "-10") { player.seekBack(); poke() }
                        CtrlButton(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play, "play", Modifier.focusRequester(playFocus)) {
                            if (player.isPlaying) player.pause() else player.play(); poke()
                        }
                        if (!ui.isLive) CtrlButton(R.drawable.ic_forward10, "+10") { player.seekForward(); poke() }
                        if (ui.nextEpisodeId != null) CtrlButton(R.drawable.ic_skip_next, stringResource(R.string.player_next_episode), label = true) { vm.playNext() }
                        Spacer(Modifier.weight(1f))
                        if (ui.qualities.isNotEmpty()) CtrlButton(R.drawable.ic_hd, stringResource(R.string.player_quality), label = true) { panel = Panel.QUALITY }
                        if (ui.audio.isNotEmpty()) CtrlButton(R.drawable.ic_audio, stringResource(R.string.player_audio), label = true) { panel = Panel.AUDIO }
                        if (ui.texts.isNotEmpty()) {
                            CtrlButton(R.drawable.ic_subtitles, stringResource(R.string.player_subtitles), label = true) { panel = Panel.TEXT }
                            CtrlButton(R.drawable.ic_sync, stringResource(R.string.player_sub_sync), label = true) { panel = Panel.SYNC }
                        }
                        if (!ui.isLive && ui.titleId != null) CtrlButton(R.drawable.ic_series, stringResource(R.string.player_back_details), label = true) {
                            val tid = ui.titleId!!
                            nav.navigate(Routes.details(tid)) { popUpTo(Routes.PLAYER) { inclusive = true } }
                        }
                    }
                }
            }
        }

        panel?.let { p ->
            val options = when (p) { Panel.QUALITY -> ui.qualities; Panel.AUDIO -> ui.audio; Panel.TEXT -> ui.texts; Panel.SYNC -> emptyList() }
            val fr = remember(p) { FocusRequester() }
            LaunchedEffect(p) { runCatching { fr.requestFocus() } }
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.CenterEnd) {
                LazyColumn(
                    Modifier.width(360.dp).fillMaxHeight().background(Brand.Surface.copy(alpha = 0.97f)),
                    contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Text(stringResource(when (p) { Panel.QUALITY -> R.string.player_quality; Panel.AUDIO -> R.string.player_audio; Panel.TEXT -> R.string.player_subtitles; Panel.SYNC -> R.string.player_sub_sync }),
                            style = MaterialTheme.typography.titleLarge, color = Brand.GoldLight, modifier = Modifier.padding(bottom = 10.dp))
                    }
                    if (p == Panel.SYNC) {
                        item { Text(stringResource(R.string.sub_offset, String.format(Locale.US, "%+.2f", ui.subOffsetMs / 1000.0)), color = Brand.Text, modifier = Modifier.padding(bottom = 8.dp)) }
                        item { OptionRow(stringResource(R.string.sub_earlier), false, { vm.shiftSubtitles(-250) }, Modifier.focusRequester(fr)) }
                        item { OptionRow(stringResource(R.string.sub_later), false, { vm.shiftSubtitles(250) }) }
                        item { OptionRow(stringResource(R.string.sub_reset), ui.subOffsetMs == 0L, { vm.shiftSubtitles(null) }) }
                    } else {
                        items(options.size) { i ->
                            val o = options[i]
                            OptionRow(o.label, o.selected, { vm.select(o); panel = null }, if (i == 0) Modifier.focusRequester(fr) else Modifier)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CtrlButton(icon: Int, text: String, modifier: Modifier = Modifier, label: Boolean = false, onClick: () -> Unit) {
    FocusCard(onClick = onClick, modifier = modifier.height(52.dp).then(if (label) Modifier else Modifier.width(52.dp)), shape = RoundedCornerShape(26.dp), focusedScale = 1.1f) {
        Row(Modifier.fillMaxHeight().padding(horizontal = if (label) 16.dp else 0.dp).align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), contentDescription = text, modifier = Modifier.size(26.dp), tint = Color.White)
            if (label) { Spacer(Modifier.width(8.dp)); Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

/** Progress bar. Media timelines always run left-to-right, even in the Arabic (RTL) interface. */
@Composable
private fun SeekBar(position: Long, duration: Long, onSeek: (Long) -> Unit, onInteract: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Format.time(position), color = Color.White, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(16.dp))
            val frac = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
            Box(
                Modifier.weight(1f).height(if (focused) 12.dp else 6.dp)
                    .onKeyEvent { ev ->
                        if (ev.type != KeyEventType.KeyDown || duration <= 0) return@onKeyEvent false
                        val step = (duration / 100).coerceIn(5_000, 60_000) // ~1 % per press, 5-60 s
                        when (ev.key) {
                            Key.DirectionRight -> { onSeek((position + step).coerceAtMost(duration)); true }
                            Key.DirectionLeft -> { onSeek((position - step).coerceAtLeast(0)); true }
                            else -> { onInteract(); false }
                        }
                    }
                    .onFocusChanged { focused = it.isFocused }
                    .focusable()
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .then(if (focused) Modifier.border(2.dp, Brand.Gold, RoundedCornerShape(6.dp)) else Modifier)
            ) {
                Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(Brand.Gold, RoundedCornerShape(6.dp)))
            }
            Spacer(Modifier.width(16.dp))
            Text("-" + Format.time((duration - position).coerceAtLeast(0)), color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Applies the user's subtitle preferences (size, colour, background, position). */
private fun styleSubtitles(view: SubtitleView?, p: PlaybackPrefs) {
    view ?: return
    val bg = when (p.subtitleBackground) { "black" -> AColor.BLACK; "semi" -> 0x99000000.toInt(); else -> AColor.TRANSPARENT }
    view.setStyle(CaptionStyleCompat(p.subtitleColor, bg, AColor.TRANSPARENT,
        if (p.subtitleBackground == "none") CaptionStyleCompat.EDGE_TYPE_OUTLINE else CaptionStyleCompat.EDGE_TYPE_NONE, AColor.BLACK, null))
    view.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * p.subtitleSize)
    view.setBottomPaddingFraction(p.subtitlePosition)
    view.setApplyEmbeddedStyles(false)
}
