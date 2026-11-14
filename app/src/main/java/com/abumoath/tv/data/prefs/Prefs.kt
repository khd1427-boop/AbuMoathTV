package com.abumoath.tv.data.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** User preferences (non-sensitive). */
data class PlaybackPrefs(
    val defaultQuality: String = "auto",     // auto | FHD | HD | SD
    val subtitleLang: String = "ara",        // ara | eng | off
    val subtitleSize: Float = 1.0f,          // 0.8 / 1.0 / 1.25 / 1.5
    val subtitleColor: Int = 0xFFFFFFFF.toInt(),
    val subtitleBackground: String = "none", // none | semi | black
    val subtitlePosition: Float = 0.08f,     // bottom padding fraction
    val maxBitrate: Int = 0                  // from speed test, 0 = unlimited
)

@Singleton
class AppSettings @Inject constructor(@ApplicationContext context: Context) {
    private val sp: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _playback = MutableStateFlow(readPlayback())
    val playback: StateFlow<PlaybackPrefs> = _playback.asStateFlow()

    /** Language is read synchronously before the Activity is created (Arabic on first launch). */
    var language: String
        get() = sp.getString("language", "ar") ?: "ar"
        set(v) = sp.edit().putString("language", v).apply()

    fun updatePlayback(transform: (PlaybackPrefs) -> PlaybackPrefs) {
        val p = transform(_playback.value)
        sp.edit()
            .putString("q", p.defaultQuality).putString("sub_lang", p.subtitleLang)
            .putFloat("sub_size", p.subtitleSize).putInt("sub_color", p.subtitleColor)
            .putString("sub_bg", p.subtitleBackground).putFloat("sub_pos", p.subtitlePosition)
            .putInt("max_bitrate", p.maxBitrate)
            .apply()
        _playback.value = p
    }

    private fun readPlayback() = PlaybackPrefs(
        defaultQuality = sp.getString("q", "auto") ?: "auto",
        subtitleLang = sp.getString("sub_lang", "ara") ?: "ara",
        subtitleSize = sp.getFloat("sub_size", 1.0f),
        subtitleColor = sp.getInt("sub_color", 0xFFFFFFFF.toInt()),
        subtitleBackground = sp.getString("sub_bg", "none") ?: "none",
        subtitlePosition = sp.getFloat("sub_pos", 0.08f),
        maxBitrate = sp.getInt("max_bitrate", 0)
    )
}

