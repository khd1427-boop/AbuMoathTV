package com.abumoath.tv.di

import android.content.Context
import androidx.room.Room
import com.abumoath.tv.BuildConfig
import com.abumoath.tv.data.local.LocalDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val UA = "AbuMoathTV/${BuildConfig.VERSION_NAME} (Android TV)"

    /** One shared connection pool for content lists, images and video keeps memory low on a 1 GB device. */
    @Provides @Singleton @Named("base")
    fun baseClient(): OkHttpClient = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(5, 2, TimeUnit.MINUTES))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            val r = chain.request()
            chain.proceed(if (r.header("User-Agent") == null) r.newBuilder().header("User-Agent", UA).build() else r)
        }
        .build()

    @Provides @Singleton @Named("images")
    fun imageClient(@Named("base") base: OkHttpClient): OkHttpClient = base

    @Provides @Singleton @Named("video")
    fun videoClient(@Named("base") base: OkHttpClient): OkHttpClient =
        base.newBuilder().readTimeout(30, TimeUnit.SECONDS).followSslRedirects(true).build()

    @Provides @Singleton
    fun db(@ApplicationContext context: Context): LocalDb =
        Room.databaseBuilder(context, LocalDb::class.java, "abumoath.db")
            .fallbackToDestructiveMigration()
            .build()
}
