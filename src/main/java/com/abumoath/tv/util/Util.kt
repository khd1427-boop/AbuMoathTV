package com.abumoath.tv.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** Applies the in-app language (Arabic by default) to a context. */
object LocaleHelper {
    fun wrap(base: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}

object Format {
    fun time(ms: Long): String {
        val total = (ms.coerceAtLeast(0) / 1000)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s) else String.format(Locale.US, "%02d:%02d", m, s)
    }

    fun qualityLabel(q: String?): String? = when (q) {
        "FHD" -> "Full HD"
        "HD" -> "HD"
        "SD" -> "SD"
        else -> null
    }
}
