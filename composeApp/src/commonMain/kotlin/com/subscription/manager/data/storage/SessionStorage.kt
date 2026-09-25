package com.subscription.manager.data.storage

data class SavedSession(
    val userId: String,
    val token: String,
    val email: String,
    val fullName: String,
    val avatarColorHex: String
)

interface SessionStorage {
    fun saveSession(session: SavedSession)
    fun getSavedSession(): SavedSession?
    fun clearSession()

    fun saveSelectedCurrency(currencyCode: String)
    fun getSavedCurrency(): String?

    fun saveMonthlyBudget(budgetBaseUsd: Double)
    fun getSavedMonthlyBudget(): Double?

    fun saveThemeMode(themeModeName: String)
    fun getSavedThemeMode(): String?
}

expect fun createSessionStorage(): SessionStorage
