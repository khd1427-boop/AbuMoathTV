package com.abumoath.tv.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abumoath.tv.data.local.SubscriptionEntity
import com.abumoath.tv.data.subscription.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel() {

    // يحول الـ Flow إلى StateFlow لاستخدامه في واجهة Compose
    val subscriptions: StateFlow<List<SubscriptionEntity>> = repository.getAllSubscriptions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSubscription(name: String, type: String, url: String?, server: String?, username: String?, password: String?) {
        viewModelScope.launch {
            val newSubscription = SubscriptionEntity(
                name = name,
                type = type,
                url = url,
                server = server,
                username = username,
                password = password,
                lastUpdated = System.currentTimeMillis()
            )
            repository.addSubscription(newSubscription)
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }
}