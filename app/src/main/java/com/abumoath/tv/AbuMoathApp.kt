package com.abumoath.tv

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Named

/**
 * Application entry point.
 *
 * The image loader is tuned for the Xiaomi Mi TV Stick (1 GB RAM):
 *  - memory cache limited to 12 % of the app heap,
 *  - RGB_565 bitmaps where possible (half the memory of ARGB_8888),
 *  - images are decoded at the size of the composable that shows them (Coil default),
 *  - small on-disk cache for posters only (never video).
 */
@HiltAndroidApp
class AbuMoathApp : Application(), ImageLoaderFactory {

    @Inject
    @field:Named("images")
    lateinit var imageClient: OkHttpClient

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(imageClient)
            .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.12).build() }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("images"))
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .components { add(SvgDecoder.Factory()) }
            .allowRgb565(true)
            .crossfade(180)
            .respectCacheHeaders(false)
            .build()
}
