package com.abumoath.tv.util

import androidx.annotation.StringRes
import com.abumoath.tv.R
import java.io.IOException

/** Maps failures to translated, user-facing messages. */
object ApiErrors {
    @StringRes
    fun messageFor(t: Throwable): Int = when {
        t is IOException -> R.string.error_network
        t.message == "not_found" -> R.string.error_not_found
        else -> R.string.error_unknown
    }
}
