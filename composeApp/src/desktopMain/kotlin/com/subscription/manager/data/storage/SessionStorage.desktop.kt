package com.subscription.manager.data.storage

import java.io.File
import java.util.Properties

class DesktopSessionStorage : SessionStorage {
    private val file = File(System.getProperty("user.home"), ".subscription_manager_prefs.properties")

    private fun loadProps(): Properties {
        val props = Properties()
        if (file.exists()) {
            try {
                file.inputStream().use { props.load(it) }
            } catch (e: Exception) {
                // Ignore corrupt file
            }
        }
        return props
    }

    private fun saveProps(props: Properties) {
        try {
            file.outputStream().use { props.store(it, null) }
        } catch (e: Exception) {
            // Ignore write error
        }
    }

    override fun saveSession(session: SavedSession) {
        val props = loadProps()
        props.setProperty("user_id", session.userId)
        props.setProperty("auth_token", session.token)
        props.setProperty("refresh_token", session.refreshToken)
        props.setProperty("user_email", session.email)
        props.setProperty("user_name", session.fullName)
        props.setProperty("user_avatar", session.avatarColorHex)
        saveProps(props)
    }

    override fun getSavedSession(): SavedSession? {
        val props = loadProps()
        val userId = props.getProperty("user_id") ?: return null
        val token = props.getProperty("auth_token") ?: ""
        val refreshToken = props.getProperty("refresh_token") ?: ""
        val email = props.getProperty("user_email") ?: ""
        val fullName = props.getProperty("user_name") ?: ""
        val avatar = props.getProperty("user_avatar") ?: "#FF6B4A"
        return SavedSession(userId, token, refreshToken, email, fullName, avatar)
    }

    override fun clearSession() {
        val props = loadProps()
        props.remove("user_id")
        props.remove("auth_token")
        props.remove("refresh_token")
        props.remove("user_email")
        props.remove("user_name")
        props.remove("user_avatar")
        saveProps(props)
    }

    override fun saveSelectedCurrency(currencyCode: String) {
        val props = loadProps()
        props.setProperty("pref_currency", currencyCode)
        saveProps(props)
    }

    override fun getSavedCurrency(): String? {
        return loadProps().getProperty("pref_currency")
    }

    override fun saveMonthlyBudget(budgetBaseUsd: Double) {
        val props = loadProps()
        props.setProperty("pref_budget", budgetBaseUsd.toString())
        saveProps(props)
    }

    override fun getSavedMonthlyBudget(): Double? {
        return loadProps().getProperty("pref_budget")?.toDoubleOrNull()
    }

    override fun saveThemeMode(themeModeName: String) {
        val props = loadProps()
        props.setProperty("pref_theme", themeModeName)
        saveProps(props)
    }

    override fun getSavedThemeMode(): String? {
        return loadProps().getProperty("pref_theme")
    }
}

actual fun createSessionStorage(): SessionStorage = DesktopSessionStorage()
