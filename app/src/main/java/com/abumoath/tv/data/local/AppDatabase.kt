package com.abumoath.tv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abumoath.tv.data.local.dao.SubscriptionDao
import com.abumoath.tv.data.local.entity.SubscriptionEntity

@Database(
    entities = [SubscriptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
}