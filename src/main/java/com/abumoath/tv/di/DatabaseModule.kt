package com.abumoath.tv.di

import android.content.Context
import androidx.room.Room
import com.abumoath.tv.data.local.LocalDb
import com.abumoath.tv.data.local.SubscriptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LocalDb {
        return Room.databaseBuilder(
            context,
            LocalDb::class.java,
            "abumoath_tv_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideSubscriptionDao(db: LocalDb): SubscriptionDao {
        return db.subscriptionDao()
    }
}