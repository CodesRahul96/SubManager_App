package com.subscription.manager.data

import com.subscription.manager.data.remote.SupabaseClient
import com.subscription.manager.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

import com.subscription.manager.data.storage.SessionStorage
import com.subscription.manager.data.storage.createSessionStorage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SubscriptionRepository(
    private val supabaseClient: SupabaseClient? = null,
    private val sessionStorage: SessionStorage = createSessionStorage()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    private val _paymentHistory = MutableStateFlow<List<PaymentHistoryItem>>(emptyList())
    val paymentHistory: StateFlow<List<PaymentHistoryItem>> = _paymentHistory.asStateFlow()

    private val _weeklySpending = MutableStateFlow<List<DaySpending>>(emptyList())
    val weeklySpending: StateFlow<List<DaySpending>> = _weeklySpending.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(Currency.USD)
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(0.0) // Monthly budget limit in USD
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    init {
        // Restore persistent user settings
        sessionStorage.getSavedCurrency()?.let { code ->
            try { _selectedCurrency.value = Currency.valueOf(code) } catch (e: Exception) {}
        }
        sessionStorage.getSavedMonthlyBudget()?.let { budget ->
            _monthlyBudget.value = budget
        }
        sessionStorage.getSavedThemeMode()?.let { mode ->
            try { _themeMode.value = AppThemeMode.valueOf(mode) } catch (e: Exception) {}
        }

        // Instantly hydrate cached subscriptions locally on startup (0-delay rendering)
        sessionStorage.getSavedSubscriptionsJson()?.let { cachedJson ->
            try {
                if (cachedJson.isNotBlank()) {
                    val cachedList = json.decodeFromString<List<Subscription>>(cachedJson)
                    _subscriptions.value = cachedList
                    recomputeDerivedData(cachedList)
                }
            } catch (e: Exception) {
                // If corrupted, fallback gracefully
            }
        }
    }

    private fun recomputeDerivedData(subs: List<Subscription>) {
        // Build payment history from subscriptions
        val history = subs.map { sub ->
            PaymentHistoryItem(
                id = "tx-${sub.id}",
                subscriptionId = sub.id,
                name = sub.name,
                dateFormatted = sub.nextBillingDateFormatted,
                amount = sub.price,
                currency = sub.originalCurrency,
                billingCycleText = "per ${sub.billingCycle.displayName.lowercase()}",
                colorHex = sub.colorHex,
                category = sub.category
            )
        }
        _paymentHistory.value = history

        // Build spending overview for days
        val totalDaily = subs.filter { it.isActive }.sumOf { it.monthlyEquivalent } / 30.0
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val daySpendList = days.mapIndexed { index, day ->
            val factor = when (index) {
                4 -> 1.4 // Fri peak
                5 -> 1.3 // Sat
                else -> 0.85 + (index * 0.05)
            }
            DaySpending(
                dayName = day,
                amount = totalDaily * factor,
                isPeak = index == 4
            )
        }
        _weeklySpending.value = daySpendList
    }

    private fun persistSubscriptions(subs: List<Subscription>) {
        try {
            val serialized = json.encodeToString(subs)
            sessionStorage.saveSubscriptionsJson(serialized)
        } catch (e: Exception) {
            // Log or ignore
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        sessionStorage.saveThemeMode(mode.name)
    }

    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
        sessionStorage.saveSelectedCurrency(currency.name)
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        sessionStorage.saveMonthlyBudget(budget)
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    suspend fun syncWithSupabase() {
        val client = supabaseClient ?: return
        if (client.isSessionActive) {
            val remoteResult = client.fetchSubscriptions()
            if (remoteResult.isSuccess) {
                val remoteList = remoteResult.getOrThrow()
                _subscriptions.value = remoteList
                recomputeDerivedData(remoteList)
                persistSubscriptions(remoteList)
            }
        }
    }

    suspend fun addSubscription(subscription: Subscription) {
        _subscriptions.update { current ->
            val updated = listOf(subscription) + current
            persistSubscriptions(updated)
            updated
        }
        // Also add a payment entry to history
        val newPayment = PaymentHistoryItem(
            id = "tx-${Clock.System.now().toEpochMilliseconds()}",
            subscriptionId = subscription.id,
            name = subscription.name,
            dateFormatted = subscription.nextBillingDateFormatted,
            amount = subscription.price,
            currency = subscription.originalCurrency,
            billingCycleText = "per ${subscription.billingCycle.displayName.lowercase()}",
            colorHex = subscription.colorHex,
            category = subscription.category
        )
        _paymentHistory.update { current ->
            listOf(newPayment) + current
        }

        recomputeDerivedData(_subscriptions.value)

        // Sync to Supabase
        supabaseClient?.insertSubscription(subscription)
    }

    suspend fun updateSubscription(updated: Subscription) {
        _subscriptions.update { current ->
            val list = current.map { if (it.id == updated.id) updated else it }
            persistSubscriptions(list)
            list
        }
        recomputeDerivedData(_subscriptions.value)
        // Sync to Supabase
        supabaseClient?.updateSubscription(updated)
    }

    suspend fun deleteSubscription(id: String) {
        _subscriptions.update { current ->
            val list = current.filterNot { it.id == id }
            persistSubscriptions(list)
            list
        }
        recomputeDerivedData(_subscriptions.value)
        // Sync to Supabase
        supabaseClient?.deleteSubscription(id)
    }

    suspend fun toggleSubscriptionActive(id: String) {
        var updatedSub: Subscription? = null
        _subscriptions.update { current ->
            val list = current.map {
                if (it.id == id) {
                    val toggled = it.copy(isActive = !it.isActive)
                    updatedSub = toggled
                    toggled
                } else it
            }
            persistSubscriptions(list)
            list
        }
        recomputeDerivedData(_subscriptions.value)
        updatedSub?.let {
            supabaseClient?.updateSubscription(it)
        }
    }

    fun clearAllData() {
        _subscriptions.value = emptyList()
        _paymentHistory.value = emptyList()
        _weeklySpending.value = emptyList()
        _monthlyBudget.value = 0.0
        sessionStorage.saveSubscriptionsJson("")
    }
}
