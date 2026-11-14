package com.abumoath.tv.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.abumoath.tv.R
import com.abumoath.tv.data.source.ContentSync
import com.abumoath.tv.ui.components.GoldButton
import com.abumoath.tv.ui.theme.Brand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(val sync: ContentSync) : ViewModel() {
    /** null = working, true = ready, false = first sync failed (no internet). */
    private val _ready = MutableStateFlow<Boolean?>(null)
    val ready = _ready.asStateFlow()

    init { start() }

    fun start() {
        _ready.value = null
        viewModelScope.launch {
            if (sync.hasContent) {
                _ready.value = true
                // Daily automatic update in the background (existing content stays visible meanwhile).
                if (sync.isStale()) launch { sync.sync() }
            } else {
                val r = sync.sync() // first launch: build the library once
                _ready.value = r !is ContentSync.Status.Failed
            }
        }
    }
}

/** Cinematic splash. On first launch it also prepares the channel & movie library (~30-60 s). */
@Composable
fun SplashScreen(onFinished: () -> Unit, vm: SplashViewModel = hiltViewModel()) {
    val anim = remember { Animatable(0f) }
    val ready by vm.ready.collectAsStateWithLifecycle()
    val status by vm.sync.status.collectAsStateWithLifecycle()
    var animDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { anim.animateTo(1f, tween(900, easing = FastOutSlowInEasing)); delay(400); animDone = true }
    LaunchedEffect(ready, animDone) { if (ready == true && animDone) onFinished() }

    Box(
        Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF1B2547), Brand.Black), radius = 1400f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(anim.value)) {
            Image(painterResource(R.drawable.brand_logo), contentDescription = null, modifier = Modifier.size(220.dp).scale(0.85f + 0.15f * anim.value))
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.brand_ar), fontSize = 44.sp, color = Brand.GoldLight, style = MaterialTheme.typography.displaySmall)
            Text(stringResource(R.string.tagline), color = Brand.Muted, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(28.dp))
            when {
                ready == false -> {
                    Text(stringResource(R.string.first_sync_failed), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    val fr = remember { FocusRequester() }
                    GoldButton(stringResource(R.string.retry), vm::start, Modifier.focusRequester(fr))
                    LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }
                }
                ready == null && !vm.sync.hasContent -> {
                    val step = (status as? ContentSync.Status.Running)?.step ?: 1
                    Text(stringResource(R.string.first_sync), color = Brand.Text)
                    Text(stringResource(if (step == 1) R.string.sync_channels else R.string.sync_movies), color = Brand.Muted)
                }
            }
        }
    }
}
