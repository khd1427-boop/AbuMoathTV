@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.abumoath.tv.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import com.abumoath.tv.data.prefs.PlaybackPrefs
import com.abumoath.tv.data.model.StreamDto
import com.abumoath.tv.data.model.SubtitleDto
import okhttp3.OkHttpClient

/**
 * Builds an ExoPlayer tuned for the Xiaomi Mi TV Stick (Amlogic S805Y, 1 GB RAM, 1080p output):
 *  - streaming only: no disk cache, media is never written to storage;
 *  - bounded RAM buffer (~32 MB) with fast start (1.5 s) and quick rebuffer (3 s);
 *  - adaptive bitrate (HLS/DASH) capped at 1080p and at the measured connection speed;
 *  - hardware decoders first, with decoder fallback enabled.
 */
object PlayerFactory {

    fun create(context: Context, prefs: PlaybackPrefs): Pair<ExoPlayer, DefaultTrackSelector> {
        val trackSelector = DefaultTrackSelector(context)
        trackSelector.setParameters(trackSelector.buildUponParameters()
            .apply {
                when (prefs.defaultQuality) {
                    "SD" -> setMaxVideoSize(854, 480)
                    "HD" -> setMaxVideoSize(1280, 720)
                    else -> setMaxVideoSize(1920, 1080) // device output limit
                }
                if (prefs.defaultQuality == "auto" && prefs.maxBitrate > 0) setMaxVideoBitrate(prefs.maxBitrate)
                setPreferredAudioLanguages("ar", "en")
                if (prefs.subtitleLang == "off") {
                    setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                } else {
                    setPreferredTextLanguage(if (prefs.subtitleLang == "eng") "en" else "ar")
                    setSelectUndeterminedTextLanguage(false)
                }
                setExceedRendererCapabilitiesIfNecessary(false) // never pick tracks the device cannot decode
            }
            .build())

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(15_000, 40_000, 1_500, 3_000)
            .setTargetBufferBytes(32 * 1024 * 1024)
            .setPrioritizeTimeOverSizeThresholds(false)
            .build()

        val renderers = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

        val player = ExoPlayer.Builder(context, renderers)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
            .setHandleAudioBecomingNoisy(true)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
        return player to trackSelector
    }

    /** Creates a media source for one stream with its own headers (Referer / User-Agent if required). */
    fun mediaSource(
        client: OkHttpClient,
        stream: StreamDto,
        subtitles: List<SubtitleDto>,
        prefs: PlaybackPrefs,
        title: String
    ): MediaSource {
        val dsf = OkHttpDataSource.Factory(client).apply {
            stream.userAgent?.let { setUserAgent(it) }
            stream.referrer?.let { setDefaultRequestProperties(mapOf("Referer" to it, "Origin" to it.trimEnd('/'))) }
        }
        val mime = when (stream.protocol) {
            "dash" -> MimeTypes.APPLICATION_MPD
            "mp4" -> MimeTypes.VIDEO_MP4
            "webm" -> MimeTypes.VIDEO_WEBM
            else -> MimeTypes.APPLICATION_M3U8
        }
        val subs = subtitles.map { s ->
            val lang2 = lang2(s.language)
            MediaItem.SubtitleConfiguration.Builder(Uri.parse(s.url))
                .setMimeType(s.mimeType)
                .setLanguage(lang2)
                .setLabel(s.label)
                .setId("sub-${s.id}")
                .setSelectionFlags(if (prefs.subtitleLang != "off" && lang2 == (if (prefs.subtitleLang == "eng") "en" else "ar")) C.SELECTION_FLAG_DEFAULT else 0)
                .build()
        }
        val item = MediaItem.Builder()
            .setUri(stream.url)
            .setMimeType(mime)
            .setMediaId(stream.id.toString())
            .setSubtitleConfigurations(subs)
            .setMediaMetadata(androidx.media3.common.MediaMetadata.Builder().setTitle(title).build())
            .build()
        return DefaultMediaSourceFactory(dsf)
            .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(4))
            .createMediaSource(item)
    }

    private val LANG2 = mapOf("ara" to "ar", "eng" to "en", "fra" to "fr", "spa" to "es", "tur" to "tr", "deu" to "de", "ita" to "it",
        "hin" to "hi", "kor" to "ko", "jpn" to "ja", "zho" to "zh", "rus" to "ru", "por" to "pt", "urd" to "ur", "fas" to "fa")
    fun lang2(code: String) = LANG2[code] ?: code
}
