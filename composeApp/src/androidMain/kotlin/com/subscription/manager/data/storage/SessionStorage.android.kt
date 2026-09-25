package com.subscription.manager.data.storage

import android.content.Context
import android.content.SharedPreferences

class AndroidSessionStorage(
    private val prefs: SharedPreferences
) : SessionStorage {

    override fun saveSession(session: SavedSession) {
        prefs.edit()
            .putString("user_id", session.userId)
            .putString("auth_token", session.token)
            .putString("refresh_token", session.refreshToken)
            .putString("user_email", session.email)
            .putString("user_name", session.fullName)
            .putString("user_avatar", session.avatarColorHex)
            .apply()
    }

    override fun getSavedSession(): SavedSession? {
        val userId = prefs.getString("user_id", null) ?: return null
        val token = prefs.getString("auth_token", "") ?: ""
        val refreshToken = prefs.getString("refresh_token", "") ?: ""
        val email = prefs.getString("user_email", "") ?: ""
        val fullName = prefs.getString("user_name", "") ?: ""
        val avatar = prefs.getString("user_avatar", "#FF6B4A") ?: "#FF6B4A"
        return SavedSession(userId, token, refreshToken, email, fullName, avatar)
    }

    override fun clearSession() {
        prefs.edit()
            .remove("user_id")
            .remove("auth_token")
            .remove("refresh_token")
            .remove("user_email")
            .remove("user_name")
            .remove("user_avatar")
            .apply()
    }

    override fun saveSelectedCurrency(currencyCode: String) {
        prefs.edit().putString("pref_currency", currencyCode).apply()
    }

    override fun getSavedCurrency(): String? {
        return prefs.getString("pref_currency", null)
    }

    override fun saveMonthlyBudget(budgetBaseUsd: Double) {
        prefs.edit().putFloat("pref_budget", budgetBaseUsd.toFloat()).apply()
    }

    override fun getSavedMonthlyBudget(): Double? {
        if (!prefs.contains("pref_budget")) return null
        return prefs.getFloat("pref_budget", 0f).toDouble()
    }

    override fun saveThemeMode(themeModeName: String) {
        prefs.edit().putString("pref_theme", themeModeName).apply()
    }

    override fun getSavedThemeMode(): String? {
        return prefs.getString("pref_theme", null)
    }

    override fun saveSubscriptionsJson(json: String) {
        prefs.edit().putString("cached_subscriptions", json).apply()
    }

    override fun getSavedSubscriptionsJson(): String? {
        return prefs.getString("cached_subscriptions", null)
    }
}

actual fun createSessionStorage(): SessionStorage {
    val prefs = AppContext.context.getSharedPreferences("subscription_manager_prefs", Context.MODE_PRIVATE)
    return AndroidSessionStorage(prefs)
}
