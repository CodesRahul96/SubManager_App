package com.subscription.manager.data

import com.subscription.manager.data.remote.SupabaseClient
import com.subscription.manager.model.RegisteredUserRecord
import com.subscription.manager.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

import com.subscription.manager.data.storage.SavedSession
import com.subscription.manager.data.storage.SessionStorage
import com.subscription.manager.data.storage.createSessionStorage

class AuthRepository(
    private val supabaseClient: SupabaseClient = SupabaseClient(),
    private val sessionStorage: SessionStorage = createSessionStorage()
) {

    // Internal fallback cache of registered users
    private val _registeredUsers = mutableMapOf<String, RegisteredUserRecord>()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Automatically restore persistent session on startup
        val saved = sessionStorage.getSavedSession()
        if (saved != null && saved.userId.isNotBlank()) {
            supabaseClient.restoreSession(saved.userId, saved.token)
            _currentUser.value = User(
                id = saved.userId,
                fullName = saved.fullName,
                email = saved.email,
                avatarColorHex = saved.avatarColorHex
            )
            _isAuthenticated.value = true
        }
    }

    fun clearError() {
        _authError.value = null
    }

    /**
     * Sign up a new user with real Supabase credentials
     */
    suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        avatarColorHex: String = "#FF6B4A"
    ): Result<User> {
        val cleanName = com.subscription.manager.util.SecurityValidator.sanitizeInput(fullName, maxLength = 80)
        val cleanEmail = email.trim().lowercase()

        if (cleanName.isBlank()) {
            val err = "Please enter your full name."
            _authError.value = err
            return Result.failure(IllegalArgumentException(err))
        }

        if (!com.subscription.manager.util.SecurityValidator.isValidEmail(cleanEmail)) {
            val err = "Please enter a valid email address (e.g. name@domain.com)."
            _authError.value = err
            return Result.failure(IllegalArgumentException(err))
        }

        val passwordError = com.subscription.manager.util.SecurityValidator.validatePassword(password)
        if (passwordError != null) {
            _authError.value = passwordError
            return Result.failure(IllegalArgumentException(passwordError))
        }

        _isLoading.value = true
        _authError.value = null

        // Try Supabase Auth API first
        val supabaseResult = supabaseClient.signUp(cleanEmail, password, cleanName)
        _isLoading.value = false

        return if (supabaseResult.isSuccess) {
            val user = supabaseResult.getOrThrow()
            _currentUser.value = user
            _isAuthenticated.value = true
            _authError.value = null
            sessionStorage.saveSession(
                SavedSession(
                    userId = user.id,
                    token = supabaseClient.currentAccessToken ?: "",
                    email = user.email,
                    fullName = user.fullName,
                    avatarColorHex = user.avatarColorHex
                )
            )
            Result.success(user)
        } else {
            val errorMsg = supabaseResult.exceptionOrNull()?.message ?: "Sign up failed"
            // If it's email rate limit or unconfirmed, let the user know cleanly
            _authError.value = errorMsg
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Sign in with Supabase credentials
     */
    suspend fun signIn(email: String, password: String): Result<User> {
        val cleanEmail = email.trim().lowercase()

        if (!isValidEmail(cleanEmail)) {
            val err = "Please enter a valid email address."
            _authError.value = err
            return Result.failure(IllegalArgumentException(err))
        }

        if (password.isBlank()) {
            val err = "Password cannot be empty."
            _authError.value = err
            return Result.failure(IllegalArgumentException(err))
        }

        _isLoading.value = true
        _authError.value = null

        val supabaseResult = supabaseClient.signIn(cleanEmail, password)
        _isLoading.value = false

        return if (supabaseResult.isSuccess) {
            val user = supabaseResult.getOrThrow()
            _currentUser.value = user
            _isAuthenticated.value = true
            _authError.value = null
            sessionStorage.saveSession(
                SavedSession(
                    userId = user.id,
                    token = supabaseClient.currentAccessToken ?: "",
                    email = user.email,
                    fullName = user.fullName,
                    avatarColorHex = user.avatarColorHex
                )
            )
            Result.success(user)
        } else {
            val errorMsg = supabaseResult.exceptionOrNull()?.message ?: "Sign in failed"
            _authError.value = errorMsg
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        supabaseClient.signOut()
        sessionStorage.clearSession()
        _currentUser.value = null
        _isAuthenticated.value = false
        _authError.value = null
    }

    /**
     * Update user profile
     */
    suspend fun updateProfile(fullName: String, avatarColorHex: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            fullName = fullName.trim(),
            avatarColorHex = avatarColorHex
        )
        _currentUser.value = updated
        sessionStorage.saveSession(
            SavedSession(
                userId = updated.id,
                token = supabaseClient.currentAccessToken ?: "",
                email = updated.email,
                fullName = updated.fullName,
                avatarColorHex = updated.avatarColorHex
            )
        )
        supabaseClient.updateProfile(fullName.trim(), avatarColorHex)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && email.contains("@") && email.substringAfter("@").contains(".")
    }

    private fun hashPassword(password: String): String {
        // Simple multiplatform deterministic hash for local verification
        var h = 1125899906842597L
        for (ch in password) {
            h = 31 * h + ch.code
        }
        return h.toString(16)
    }
}
