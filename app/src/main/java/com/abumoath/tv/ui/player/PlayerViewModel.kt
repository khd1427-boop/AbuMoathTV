@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.abumoath.tv.ui.player

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.abumoath.tv.R
import com.abumoath.tv.data.prefs.AppSettings
import com.abumoath.tv.data.prefs.PlaybackPrefs
import com.abumoath.tv.util.ApiErrors
import com.abumoath.tv.data.model.StreamDto
import com.abumoath.tv.data.model.SubtitleDto
import com.abumoath.tv.data.repo.CatalogRepository
import com.abumoath.tv.data.repo.LibraryRepository
import com.abumoath.tv.player.PlayerFactory
import com.abumoath.tv.data.source.ArchiveResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

data class TrackOption(val key: String, val label: String, val selected: Boolean)

data class PlayerUi(
    val loading: Boolean = true,
    val title: String = "",
    val subtitle: String? = null,
    val backdrop: String? = null,
    val isLive: Boolean = false,
    val titleId: Long? = null,
    val nextEpisodeId: Long? = null,
    val qualities: List<TrackOption> = emptyList(),
    val audio: List<TrackOption> = emptyList(),
    val texts: List<TrackOption> = emptyList(),
    val subOffsetMs: Long = 0,
    val info: Int? = null,          // transient message (reconnecting / switching source)
    val error: Int? = null,         // fatal message
    val nextCountdown: Int? = null,
    val isFavorite: Boolean = false,
    val channelNumber: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    handle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val catalog: CatalogRepository,
    private val library: LibraryRepository,
    private val resolver: ArchiveResolver,
    settings: AppSettings,
    @Named("video") private val videoClient: OkHttpClient
) : ViewModel() {

    private var kind: String = handle["kind"] ?: "title"   // title | episode | channel | auto
    private var itemId: Long = handle["id"] ?: 0L
    private var resumeMs: Long = handle["resume"] ?: -1L
    private val prefs: PlaybackPrefs = settings.playback.value
    val subtitlePrefs get() = prefs

    val player: ExoPlayer
    private val trackSelector: DefaultTrackSelector

    private val _ui = MutableStateFlow(PlayerUi())
    val ui = _ui.asStateFlow()

    private var streams: List<StreamDto> = emptyList()
    private var subtitles: List<SubtitleDto> = emptyList()
    private var activeSubtitles: List<SubtitleDto> = emptyList()
        get() = if (field.isEmpty()) subtitles else field
    private var streamIndex = 0
    private var zapList: List<Long> = emptyList()
    private var networkRetries = 0
    private var progressJob: Job? = null
    private var countdownJob: Job? = null
    private val saveScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val netCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Internet is back: resume a stream that failed because of the outage.
            viewModelScope.launch(Dispatchers.Main) { if (player.playerError != null) { networkRetries = 0; retrySame() } }
        }
    }

    private val listener = object : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) = refreshTracks(tracks)
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_READY -> {
                    networkRetries = 0
                    _ui.update { it.copy(loading = false, info = null, error = null) }
                    streams.getOrNull(streamIndex)?.let { st -> viewModelScope.launch { catalog.reportStreamResult(kind, st.id, ok = true) } }
                }
                Player.STATE_BUFFERING -> _ui.update { it.copy(loading = true) }
                Player.STATE_ENDED -> onEnded()
                else -> Unit
            }
        }
        override fun onIsPlayingChanged(isPlaying: Boolean) { if (!isPlaying) saveProgress() }
        override fun onPlayerError(error: PlaybackException) = handleError(error)
    }

    init {
        val (p, ts) = PlayerFactory.create(context, prefs)
        player = p; trackSelector = ts
        player.addListener(listener)
        runCatching { connectivity.registerDefaultNetworkCallback(netCallback) }
        load()
    }

    private fun load() {
        _ui.update { PlayerUi(loading = true) }
        viewModelScope.launch {
            try {
                when (kind) {
                    "channel" -> {
                        val c = catalog.channel(itemId)
                        streams = c.streams; subtitles = emptyList(); resumeMs = 0
                        if (zapList.isEmpty()) zapList = runCatching { catalog.channelNeighbors(itemId) }.getOrDefault(emptyList())
                        val pos = zapList.indexOf(itemId)
                        val fav = runCatching { library.isIn("favorite", "channel", itemId) }.getOrDefault(false)
                        _ui.update { it.copy(title = c.name, isLive = true, isFavorite = fav,
                            subtitle = com.abumoath.tv.data.repo.flagOf(c.country) + "  " + context.getString(R.string.live_now),
                            channelNumber = if (pos >= 0) "${pos + 1} / ${zapList.size}" else null) }
                    }
                    else -> {
                        kind = "title"
                        val d = catalog.title(itemId)
                        streams = d.streams; subtitles = d.subtitles
                        if (resumeMs < 0) resumeMs = d.progress?.takeIf { !it.completed }?.positionMs ?: 0
                        _ui.update { it.copy(title = d.displayTitle, backdrop = d.posterUrl, titleId = d.id) }
                    }
                }
                streams = orderByPreference(streams)
                if (streams.isEmpty()) { _ui.update { it.copy(loading = false, error = if (kind == "channel") R.string.channel_unavailable else R.string.no_streams) }; return@launch }
                streamIndex = 0
                startStream(resumeMs)
                catalog.reportStart(kind, itemId)
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = ApiErrors.messageFor(e)) }
            }
        }
    }

    /** Puts the user's preferred quality first when the alternatives are separate files (e.g. MP4 renditions). */
    private fun orderByPreference(list: List<StreamDto>): List<StreamDto> {
        val pref = prefs.defaultQuality
        if (pref == "auto") return list
        return list.sortedByDescending { if (it.quality == pref) 1 else 0 }
    }

    private fun startStream(positionMs: Long) {
        val s = streams.getOrNull(streamIndex) ?: return
        val source = PlayerFactory.mediaSource(videoClient, s, activeSubtitles, prefs, _ui.value.title)
        player.setMediaSource(source, positionMs.coerceAtLeast(0))
        player.prepare()
        player.playWhenReady = true
        startProgressLoop()
    }

    private fun retrySame() {
        val pos = if (_ui.value.isLive) C.TIME_UNSET else player.currentPosition
        player.prepare()
        if (pos != C.TIME_UNSET) player.seekTo(pos)
        player.playWhenReady = true
    }

    private fun handleError(e: PlaybackException) {
        val s = streams.getOrNull(streamIndex)
        if (e.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) { player.seekToDefaultPosition(); player.prepare(); return }
        val isNetwork = e.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
            e.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ||
            e.errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED
        if (isNetwork && networkRetries < 3) {
            networkRetries++
            _ui.update { it.copy(info = R.string.player_reconnecting) }
            viewModelScope.launch { delay(2000L * networkRetries); retrySame() }
            return
        }
        s?.let { viewModelScope.launch { catalog.reportStreamResult(kind, it.id, ok = false) } }
        if (streamIndex < streams.lastIndex) {
            // Fallback to the next (legal) source of the same title/channel.
            val pos = if (_ui.value.isLive) 0 else player.currentPosition
            streamIndex++; networkRetries = 0
            _ui.update { it.copy(info = R.string.player_switching_source) }
            startStream(pos)
            return
        }
        val isDecoder = e.errorCode in 4000..4999
        _ui.update { it.copy(loading = false, info = null, error = when {
            isDecoder -> R.string.player_error_unsupported
            isNetwork -> R.string.player_error_network
            _ui.value.isLive -> R.string.channel_unavailable
            else -> R.string.player_error_source
        }) }
    }

    fun retryAfterError() { networkRetries = 0; streamIndex = 0; _ui.update { it.copy(error = null, loading = true) }; startStream(if (_ui.value.isLive) 0 else player.currentPosition) }

    // ---------- Tracks ----------
    private fun refreshTracks(tracks: Tracks) {
        val params = player.trackSelectionParameters
        val videoOverride = params.overrides.values.any { it.type == C.TRACK_TYPE_VIDEO }
        val q = mutableListOf<TrackOption>()
        val heights = mutableSetOf<Int>()
        tracks.groups.filter { it.type == C.TRACK_TYPE_VIDEO }.forEachIndexed { gi, g ->
            for (i in 0 until g.length) {
                val f = g.getTrackFormat(i)
                // Only offer qualities the device can decode and display (<= 1080p).
                if (!g.isTrackSupported(i) || f.height <= 0 || f.height > 1080 || !heights.add(f.height)) continue
                q += TrackOption("v:$gi:$i", "${f.height}p", videoOverride && g.isTrackSelected(i))
            }
        }
        if (q.size > 1) {
            q.sortByDescending { it.label.removeSuffix("p").toIntOrNull() ?: 0 }
            q.add(0, TrackOption("v:auto", context.getString(R.string.player_auto), !videoOverride))
        } else if (streams.map { it.quality }.distinct().size > 1) {
            // Separate files per quality: switching restarts the stream at the same position.
            streams.forEachIndexed { i, s -> q += TrackOption("s:$i", com.abumoath.tv.util.Format.qualityLabel(s.quality) ?: "#${i + 1}", i == streamIndex) }
        } else q.clear()

        val audio = mutableListOf<TrackOption>()
        tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }.forEachIndexed { gi, g ->
            for (i in 0 until g.length) {
                if (!g.isTrackSupported(i)) continue
                val f = g.getTrackFormat(i)
                val label = f.label ?: f.language?.let { Locale(it).displayLanguage }?.takeIf { it.isNotBlank() } ?: context.getString(R.string.audio_track_n, audio.size + 1)
                audio += TrackOption("a:$gi:$i", label, g.isTrackSelected(i))
            }
        }
        val texts = mutableListOf<TrackOption>()
        val textDisabled = params.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
        tracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }.forEachIndexed { gi, g ->
            for (i in 0 until g.length) {
                val f = g.getTrackFormat(i)
                val label = f.label ?: f.language?.let { Locale(it).displayLanguage }?.takeIf { it.isNotBlank() } ?: "#${texts.size + 1}"
                texts += TrackOption("t:$gi:$i", label, !textDisabled && g.isTrackSelected(i))
            }
        }
        if (texts.isNotEmpty()) texts.add(0, TrackOption("t:off", context.getString(R.string.player_off), textDisabled || texts.none { it.selected }))
        _ui.update { it.copy(qualities = q, audio = audio.takeIf { a -> a.size > 1 } ?: emptyList(), texts = texts) }
    }

    fun select(option: TrackOption) {
        val parts = option.key.split(":")
        val b = player.trackSelectionParameters.buildUpon()
        when {
            option.key == "v:auto" -> b.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            option.key == "t:off" -> b.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            parts[0] == "s" -> { val i = parts[1].toInt(); if (i != streamIndex) { val pos = player.currentPosition; streamIndex = i; startStream(pos) }; return }
            else -> {
                val type = when (parts[0]) { "v" -> C.TRACK_TYPE_VIDEO; "a" -> C.TRACK_TYPE_AUDIO; else -> C.TRACK_TYPE_TEXT }
                val group = player.currentTracks.groups.filter { it.type == type }.getOrNull(parts[1].toInt()) ?: return
                if (type == C.TRACK_TYPE_TEXT) b.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                b.setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, parts[2].toInt()))
            }
        }
        player.trackSelectionParameters = b.build()
    }

    /** Subtitle sync: a re-timed local copy of the subtitle file is used; playback continues at the same position. */
    fun shiftSubtitles(deltaMs: Long?) {
        if (subtitles.isEmpty()) return
        val newOffset = if (deltaMs == null) 0 else (_ui.value.subOffsetMs + deltaMs).coerceIn(-30_000, 30_000)
        _ui.update { it.copy(subOffsetMs = newOffset) }
        viewModelScope.launch {
            activeSubtitles = if (newOffset == 0L) emptyList() else subtitles.map { sub -> runCatching { resolver.shifted(sub, newOffset) }.getOrDefault(sub) }
            startStream(player.currentPosition)
        }
    }

    // ---------- Progress & next episode ----------
    private fun startProgressLoop() {
        progressJob?.cancel()
        if (kind == "channel") return
        progressJob = viewModelScope.launch { while (isActive) { delay(10_000); if (player.isPlaying) saveProgress() } }
    }

    private fun saveProgress(ended: Boolean = false) {
        if (kind != "title") return
        val dur = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: return
        val pos = if (ended) dur else player.currentPosition
        val titleId = _ui.value.titleId ?: return
        val k = kind; val id = itemId
        saveScope.launch { runCatching { library.saveProgress(k, id, titleId, pos, dur) } }
    }

    private fun onEnded() {
        saveProgress(ended = true)
        val next = _ui.value.nextEpisodeId ?: return
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 5 downTo 1) { _ui.update { it.copy(nextCountdown = i) }; delay(1000) }
            playNext()
        }
    }

    /** Channel up/down (live TV): next / previous channel of the same country. */
    fun zap(delta: Int) {
        if (kind != "channel" || zapList.size < 2) return
        val i = zapList.indexOf(itemId).coerceAtLeast(0)
        itemId = zapList[((i + delta) % zapList.size + zapList.size) % zapList.size]
        load()
    }

    fun toggleFavorite() {
        if (kind != "channel") return
        val now = !_ui.value.isFavorite
        _ui.update { it.copy(isFavorite = now) }
        viewModelScope.launch { runCatching { library.setInList("favorite", "channel", itemId, now) } }
    }

    fun cancelCountdown() { countdownJob?.cancel(); _ui.update { it.copy(nextCountdown = null) } }

    fun playNext() {
        val next = _ui.value.nextEpisodeId ?: return
        countdownJob?.cancel()
        saveProgress()
        kind = "episode"; itemId = next; resumeMs = 0
        load()
    }

    override fun onCleared() {
        saveProgress()
        runCatching { connectivity.unregisterNetworkCallback(netCallback) }
        player.removeListener(listener)
        player.release() // frees decoders and RAM buffers immediately (nothing is kept on disk)
        super.onCleared()
    }
}
