package com.abumoath.tv

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.abumoath.tv.ui.navigation.AppNavHost
import com.abumoath.tv.ui.theme.AbuMoathTheme
import com.abumoath.tv.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Arabic is applied before any UI is inflated (first launch defaults to Arabic + RTL). */
    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("language", "ar") ?: "ar"
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_AbuMoath)
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.black)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent { AbuMoathTheme { AppNavHost() } }
    }
}
