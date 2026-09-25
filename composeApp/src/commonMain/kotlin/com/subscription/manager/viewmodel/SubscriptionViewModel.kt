package com.subscription.manager.viewmodel

import com.subscription.manager.data.AuthRepository
import com.subscription.manager.data.SubscriptionRepository
import com.subscription.manager.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

enum class AppTab(val title: String) {
    HOME("Home"),
    SUBSCRIPTIONS("Subscriptions"),
    INSIGHTS("Insights"),
    SETTINGS("Settings")
}

enum class SortOption(val displayName: String) {
    DUE_SOON("Renewal Date"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    NAME("Name (A-Z)")
}

data class AppUiState(
    val activeTab: AppTab = AppTab.HOME,
    val searchQuery: String = "",
    val selectedCategory: SubscriptionCategory? = null,
    val filterStatus: String = "ALL", // "ALL", "ACTIVE", "PAUSED", "UPCOMING"
    val sortBy: SortOption = SortOption.DUE_SOON,
    val isAddDialogOpen: Boolean = false,
    val editingSubscription: Subscription? = null,
    val detailSubscription: Subscription? = null,
    val toastMessage: String? = null,
    val insightsSelectedDay: DaySpending? = null,
    val isNewUserSetupRequired: Boolean = false,
    val isOAuthPasswordSetupRequired: Boolean = false
)

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val authRepository: AuthRepository = AuthRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // Authentication State
    val currentUser: StateFlow<User?> = authRepository.currentUser
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
    val authError: StateFlow<String?> = authRepository.authError
    val isAuthLoading: StateFlow<Boolean> = authRepository.isLoading

    val subscriptions: StateFlow<List<Subscription>> = repository.subscriptions
    val paymentHistory: StateFlow<List<PaymentHistoryItem>> = repository.paymentHistory
    val weeklySpending: StateFlow<List<DaySpending>> = repository.weeklySpending
    val selectedCurrency: StateFlow<Currency> = repository.selectedCurrency
    val monthlyBudget: StateFlow<Double> = repository.monthlyBudget
    val userName: StateFlow<String> = repository.userName
    val themeMode: StateFlow<AppThemeMode> = repository.themeMode

    // Computed total monthly spend in base USD
    val totalMonthlySpend: StateFlow<Double> = subscriptions.map { list ->
        list.filter { it.isActive }.sumOf { it.monthlyEquivalent }
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    // Upcoming subscriptions sorted by days left
    val upcomingSubscriptions: StateFlow<List<Subscription>> = subscriptions.map { list ->
        list.filter { it.isActive }.sortedBy { it.daysUntilRenewal }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Filtered subscriptions list for the Subscriptions screen
    val filteredSubscriptions: StateFlow<List<Subscription>> = combine(
        subscriptions,
        _uiState
    ) { subs, state ->
        var list = subs

        // Filter by search query
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.displayName.lowercase().contains(q)
            }
        }

        // Filter by category
        if (state.selectedCategory != null) {
            list = list.filter { it.category == state.selectedCategory }
        }

        // Filter by status tab
        list = when (state.filterStatus) {
            "ACTIVE" -> list.filter { it.isActive }
            "PAUSED" -> list.filter { !it.isActive }
            "UPCOMING" -> list.filter { it.isActive && it.daysUntilRenewal <= 14 }
            else -> list
        }

        // Sort
        when (state.sortBy) {
            SortOption.DUE_SOON -> list.sortedBy { it.daysUntilRenewal }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.monthlyEquivalent }
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.monthlyEquivalent }
            SortOption.NAME -> list.sortedBy { it.name.lowercase() }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Category breakdown for insights
    val categoryBreakdown: StateFlow<Map<SubscriptionCategory, Double>> = subscriptions.map { list ->
        list.filter { it.isActive }
            .groupBy { it.category }
            .mapValues { (_, subs) -> subs.sumOf { it.monthlyEquivalent } }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    init {
        // Set default selected day for insights to peak day if present
        _uiState.update { current ->
            current.copy(insightsSelectedDay = weeklySpending.value.firstOrNull { it.isPeak })
        }
        // If session was restored, set user name and sync data from Supabase
        val restoredUser = currentUser.value
        if (restoredUser != null) {
            repository.setUserName(restoredUser.fullName)
            scope.launch {
                repository.syncWithSupabase()
            }
        }
    }

    fun signUp(fullName: String, email: String, password: String, avatarColorHex: String = "#FF6B4A") {
        scope.launch {
            val result = authRepository.signUp(fullName, email, password, avatarColorHex)
            result.onSuccess { user ->
                repository.setUserName(user.fullName)
                _uiState.update { it.copy(isNewUserSetupRequired = true) }
                showToast("Welcome to Renewo, ${user.firstName}!")
            }
        }
    }

    fun completeNewUserSetup() {
        _uiState.update { it.copy(isNewUserSetupRequired = false, isOAuthPasswordSetupRequired = false) }
    }

    fun completeOAuthPasswordSetup() {
        _uiState.update { it.copy(isOAuthPasswordSetupRequired = false, isNewUserSetupRequired = true) }
    }

    fun getOAuthSignInUrl(provider: String = "google"): String {
        return authRepository.getOAuthSignInUrl(provider)
    }

    fun handleOAuthCallback(accessToken: String, refreshToken: String) {
        scope.launch {
            val result = authRepository.handleOAuthCallback(accessToken, refreshToken)
            result.onSuccess { (user, isNewUser) ->
                repository.setUserName(user.fullName)
                repository.syncWithSupabase()
                if (isNewUser) {
                    _uiState.update { it.copy(isOAuthPasswordSetupRequired = true, isNewUserSetupRequired = false) }
                    showToast("Welcome to Renewo, ${user.firstName}! Please create your password.")
                } else {
                    _uiState.update { it.copy(isOAuthPasswordSetupRequired = false, isNewUserSetupRequired = false) }
                    showToast("Welcome back, ${user.firstName}!")
                }
            }
            result.onFailure { e ->
                showToast(e.message ?: "Authentication failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        scope.launch {
            val result = authRepository.signIn(email, password)
            result.onSuccess { user ->
                repository.setUserName(user.fullName)
                repository.syncWithSupabase()
                showToast("Welcome back, ${user.firstName}!")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        repository.setUserName("User")
        _uiState.update { it.copy(isNewUserSetupRequired = false, isOAuthPasswordSetupRequired = false) }
        showToast("Signed out successfully")
    }

    fun clearAuthError() {
        authRepository.clearError()
    }

    fun updateProfile(fullName: String, avatarColorHex: String) {
        scope.launch {
            authRepository.updateProfile(fullName, avatarColorHex)
            val updated = authRepository.currentUser.value
            if (updated != null) {
                repository.setUserName(updated.fullName)
            }
            showToast("Profile updated successfully!")
        }
    }

    fun changePassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            val res = authRepository.changePassword(newPassword)
            if (res.isSuccess) {
                showToast("Password updated successfully!")
                onResult(true, "Password updated successfully!")
            } else {
                val errMsg = res.exceptionOrNull()?.message ?: "Failed to update password"
                onResult(false, errMsg)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: SubscriptionCategory?) {
        _uiState.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
        }
    }

    fun setFilterStatus(status: String) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    fun setSortBy(sort: SortOption) {
        _uiState.update { it.copy(sortBy = sort) }
    }

    fun selectInsightsDay(day: DaySpending) {
        _uiState.update { it.copy(insightsSelectedDay = day) }
    }

    fun openAddDialog(subscription: Subscription? = null) {
        _uiState.update {
            it.copy(
                isAddDialogOpen = true,
                editingSubscription = subscription
            )
        }
    }

    fun closeAddDialog() {
        _uiState.update {
            it.copy(
                isAddDialogOpen = false,
                editingSubscription = null
            )
        }
    }

    fun openDetail(subscription: Subscription) {
        _uiState.update { it.copy(detailSubscription = subscription) }
    }

    fun closeDetail() {
        _uiState.update { it.copy(detailSubscription = null) }
    }

    fun toggleSubscriptionActive(id: String) {
        scope.launch {
            repository.toggleSubscriptionActive(id)
            // Refresh detail if open
            val updated = subscriptions.value.find { it.id == id }
            if (updated != null && _uiState.value.detailSubscription?.id == id) {
                _uiState.update { it.copy(detailSubscription = updated) }
            }
            showToast("Subscription status updated")
        }
    }

    fun deleteSubscription(id: String) {
        scope.launch {
            repository.deleteSubscription(id)
            closeDetail()
            showToast("Subscription deleted")
        }
    }

    fun saveSubscription(
        id: String?,
        name: String,
        description: String,
        price: Double,
        currency: Currency,
        billingCycle: BillingCycle,
        category: SubscriptionCategory,
        daysUntilRenewal: Int,
        nextBillingDateFormatted: String,
        colorHex: Long,
        reminderDaysBefore: Int,
        notes: String,
        websiteUrl: String
    ) {
        val sanitizedName = com.subscription.manager.util.SecurityValidator.sanitizeInput(name, maxLength = 60)
        val sanitizedDesc = com.subscription.manager.util.SecurityValidator.sanitizeInput(description, maxLength = 200)
        val sanitizedNotes = com.subscription.manager.util.SecurityValidator.sanitizeInput(notes, maxLength = 500)
        val sanitizedUrl = com.subscription.manager.util.SecurityValidator.sanitizeInput(websiteUrl, maxLength = 250)

        if (sanitizedName.isBlank()) {
            showToast("Subscription name cannot be empty")
            return
        }

        if (!com.subscription.manager.util.SecurityValidator.validatePrice(price)) {
            showToast("Please enter a valid price between 0 and 999,999")
            return
        }

        // Convert entered price from the given currency to base USD for storage
        val basePrice = if (currency.rateToUsd > 0) price / currency.rateToUsd else price

        scope.launch {
            if (id == null) {
                // New subscription
                val newSub = Subscription(
                    id = "sub-${Clock.System.now().toEpochMilliseconds()}",
                    name = sanitizedName,
                    description = sanitizedDesc,
                    price = basePrice,
                    originalCurrency = currency,
                    billingCycle = billingCycle,
                    category = category,
                    nextBillingDateFormatted = nextBillingDateFormatted,
                    daysUntilRenewal = daysUntilRenewal,
                    colorHex = colorHex,
                    isActive = true,
                    reminderDaysBefore = reminderDaysBefore,
                    notes = notes,
                    websiteUrl = websiteUrl
                )
                repository.addSubscription(newSub)
                showToast("Added $name successfully!")
            } else {
                // Edit existing
                val existing = subscriptions.value.find { it.id == id }
                if (existing != null) {
                    val updated = existing.copy(
                        name = sanitizedName,
                        description = sanitizedDesc,
                        price = basePrice,
                        originalCurrency = currency,
                        billingCycle = billingCycle,
                        category = category,
                        nextBillingDateFormatted = nextBillingDateFormatted,
                        daysUntilRenewal = daysUntilRenewal,
                        colorHex = colorHex,
                        reminderDaysBefore = reminderDaysBefore,
                        notes = sanitizedNotes,
                        websiteUrl = sanitizedUrl
                    )
                    repository.updateSubscription(updated)
                    showToast("Updated $sanitizedName successfully!")
                }
            }
        }
        closeAddDialog()
    }

    fun changeCurrency(currency: Currency) {
        repository.setCurrency(currency)
        showToast("Currency set to ${currency.code} (${currency.symbol})")
    }

    fun setThemeMode(mode: AppThemeMode) {
        repository.setThemeMode(mode)
        showToast("Theme set to ${mode.displayName}")
    }

    fun changeBudget(budgetInSelectedCurrency: Double) {
        val curr = selectedCurrency.value
        val baseUsd = if (curr.rateToUsd > 0) budgetInSelectedCurrency / curr.rateToUsd else budgetInSelectedCurrency
        repository.setMonthlyBudget(baseUsd)
        showToast("Monthly budget updated")
    }

    fun clearAllData() {
        repository.clearAllData()
        showToast("All data cleared")
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // Helper formatters
    fun formatCurrency(amount: Double): String {
        val curr = selectedCurrency.value
        val converted = amount * curr.rateToUsd
        val rounded = kotlin.math.round(converted * 100.0) / 100.0
        val parts = rounded.toString().split(".")
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1].padEnd(2, '0').take(2) else "00"
        return "${curr.symbol}$intPart.$decPart"
    }
}
