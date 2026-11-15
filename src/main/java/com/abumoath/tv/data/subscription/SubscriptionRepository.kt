package com.abumoath.tv.data.subscription

import com.abumoath.tv.data.local.SubscriptionDao
import com.abumoath.tv.data.local.SubscriptionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> {
        return subscriptionDao.getAll()
    }

    suspend fun addSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.insert(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.delete(subscription)
    }
}