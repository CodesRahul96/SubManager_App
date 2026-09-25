package com.subscription.manager.data.remote

import com.subscription.manager.model.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class SupabaseAuthRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@Serializable
data class SupabaseSubscriptionDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val description: String = "",
    val price: Double,
    @SerialName("original_currency") val originalCurrency: String = "USD",
    @SerialName("billing_cycle") val billingCycle: String = "MONTHLY",
    val category: String = "PRODUCTIVITY",
    @SerialName("next_billing_date_formatted") val nextBillingDateFormatted: String,
    @SerialName("days_until_renewal") val daysUntilRenewal: Int,
    @SerialName("color_hex") val colorHex: Long,
    @SerialName("icon_name") val iconName: String = "default",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("reminder_days_before") val reminderDaysBefore: Int = 3,
    val notes: String = "",
    @SerialName("website_url") val websiteUrl: String = ""
)

fun Subscription.toDto(userId: String): SupabaseSubscriptionDto {
    return SupabaseSubscriptionDto(
        id = id,
        userId = userId,
        name = name,
        description = description,
        price = price,
        originalCurrency = originalCurrency.code,
        billingCycle = billingCycle.name,
        category = category.name,
        nextBillingDateFormatted = nextBillingDateFormatted,
        daysUntilRenewal = daysUntilRenewal,
        colorHex = colorHex,
        iconName = iconName,
        isActive = isActive,
        reminderDaysBefore = reminderDaysBefore,
        notes = notes,
        websiteUrl = websiteUrl
    )
}

fun SupabaseSubscriptionDto.toDomain(): Subscription {
    return Subscription(
        id = id,
        name = name,
        description = description,
        price = price,
        originalCurrency = try { Currency.valueOf(originalCurrency) } catch (e: Exception) { Currency.USD },
        billingCycle = try { BillingCycle.valueOf(billingCycle) } catch (e: Exception) { BillingCycle.MONTHLY },
        category = try { SubscriptionCategory.valueOf(category) } catch (e: Exception) { SubscriptionCategory.PRODUCTIVITY },
        nextBillingDateFormatted = nextBillingDateFormatted,
        daysUntilRenewal = daysUntilRenewal,
        colorHex = colorHex,
        iconName = iconName,
        isActive = isActive,
        reminderDaysBefore = reminderDaysBefore,
        notes = notes,
        websiteUrl = websiteUrl
    )
}

object SupabaseConfig {
    const val PROJECT_URL = "https://qrsxehlegpzntijimgzx.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFyc3hlaGxlZ3B6bnRpamltZ3p4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAzMjgxOTAsImV4cCI6MjEwNTkwNDE5MH0.VYlGrAN39N11OPDbhdh5M2Q6lXnEsA0N-qZNA27Jjuk"
    const val PUBLISHABLE_KEY = "sb_publishable_NpCArfHvVlibs-uaF3AIOA_pltvOChl"
}

class SupabaseClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    var currentUserId: String? = null
        private set

    var currentAccessToken: String? = null
        private set

    var currentRefreshToken: String? = null
        private set

    var onTokensRefreshed: ((accessToken: String, refreshToken: String) -> Unit)? = null

    fun restoreSession(userId: String, token: String, refreshToken: String = "") {
        currentUserId = userId
        currentAccessToken = token
        currentRefreshToken = refreshToken
    }

    val isSessionActive: Boolean get() = currentAccessToken != null

    suspend fun refreshSession(): Result<String> = withContext(Dispatchers.Default) {
        val rToken = currentRefreshToken
        if (rToken.isNullOrBlank()) {
            return@withContext Result.failure(Exception("Session expired. Please sign in again."))
        }
        try {
            val response = httpClient.post("${SupabaseConfig.PROJECT_URL}/auth/v1/token?grant_type=refresh_token") {
                header("apikey", SupabaseConfig.ANON_KEY)
                contentType(ContentType.Application.Json)
                setBody(mapOf("refresh_token" to rToken))
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val jsonObj = json.parseToJsonElement(body).jsonObject
                val newAccessToken = jsonObj["access_token"]?.jsonPrimitive?.contentOrNull
                val newRefreshToken = jsonObj["refresh_token"]?.jsonPrimitive?.contentOrNull ?: rToken
                if (!newAccessToken.isNullOrBlank()) {
                    currentAccessToken = newAccessToken
                    currentRefreshToken = newRefreshToken
                    onTokensRefreshed?.invoke(newAccessToken, newRefreshToken)
                    Result.success(newAccessToken)
                } else {
                    Result.failure(Exception("Session expired. Please sign in again."))
                }
            } else {
                Result.failure(Exception("Session expired. Please sign in again."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOAuthSignInUrl(provider: String = "google"): String {
        return "${SupabaseConfig.PROJECT_URL}/auth/v1/authorize?provider=$provider&redirect_to=renewo://auth-callback"
    }

    suspend fun processOAuthSession(accessToken: String, refreshToken: String = ""): Result<Pair<User, Boolean>> = withContext(Dispatchers.Default) {
        currentAccessToken = accessToken
        if (refreshToken.isNotBlank()) {
            currentRefreshToken = refreshToken
        }
        try {
            val response = httpClient.get("${SupabaseConfig.PROJECT_URL}/auth/v1/user") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $accessToken")
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val userObj = json.parseToJsonElement(body).jsonObject
                val userId = userObj["id"]?.jsonPrimitive?.contentOrNull
                    ?: "usr_${Clock.System.now().toEpochMilliseconds()}"
                currentUserId = userId
                val userMetadata = userObj["user_metadata"]?.jsonObject
                val email = userObj["email"]?.jsonPrimitive?.contentOrNull ?: ""
                val extractedName = userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    ?: userMetadata?.get("name")?.jsonPrimitive?.contentOrNull
                    ?: email.substringBefore("@").replace(".", " ").capitalizeWords()
                val avatarColor = userMetadata?.get("avatar_color")?.jsonPrimitive?.contentOrNull
                    ?: "#FF6B4A"
                val planStr = userMetadata?.get("plan")?.jsonPrimitive?.contentOrNull ?: "BASIC"
                val userPlan = try { UserPlan.valueOf(planStr) } catch (e: Exception) { UserPlan.BASIC }

                // Check if new user: createdAt within last 120 seconds or has_password flag is missing/false
                val createdAtStr = userObj["created_at"]?.jsonPrimitive?.contentOrNull
                val lastSignInAtStr = userObj["last_sign_in_at"]?.jsonPrimitive?.contentOrNull
                val isNewUser = if (createdAtStr != null && lastSignInAtStr != null) {
                    createdAtStr.take(19) == lastSignInAtStr.take(19)
                } else {
                    true
                }

                val user = User(
                    id = userId,
                    fullName = extractedName,
                    email = email,
                    avatarColorHex = avatarColor,
                    plan = userPlan
                )
                Result.success(Pair(user, isNewUser))
            } else {
                val err = parseErrorMessage(body)
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCurrentUserProfile(): Result<User> = withContext(Dispatchers.Default) {
        val token = currentAccessToken ?: return@withContext Result.failure(Exception("No token"))
        try {
            val response = httpClient.get("${SupabaseConfig.PROJECT_URL}/auth/v1/user") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val userObj = json.parseToJsonElement(body).jsonObject
                val userId = userObj["id"]?.jsonPrimitive?.contentOrNull ?: currentUserId ?: ""
                val userMetadata = userObj["user_metadata"]?.jsonObject
                val email = userObj["email"]?.jsonPrimitive?.contentOrNull ?: ""
                val extractedName = userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    ?: userMetadata?.get("name")?.jsonPrimitive?.contentOrNull
                    ?: email.substringBefore("@").replace(".", " ").capitalizeWords()
                val avatarColor = userMetadata?.get("avatar_color")?.jsonPrimitive?.contentOrNull ?: "#FF6B4A"
                val planStr = userMetadata?.get("plan")?.jsonPrimitive?.contentOrNull ?: "BASIC"
                val userPlan = try { UserPlan.valueOf(planStr) } catch (e: Exception) { UserPlan.BASIC }

                val user = User(
                    id = userId,
                    fullName = extractedName,
                    email = email,
                    avatarColorHex = avatarColor,
                    plan = userPlan
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to fetch user profile: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun signUp(email: String, password: String, fullName: String): Result<User> = withContext(Dispatchers.Default) {
        try {
            val response = httpClient.post("${SupabaseConfig.PROJECT_URL}/auth/v1/signup") {
                header("apikey", SupabaseConfig.ANON_KEY)
                contentType(ContentType.Application.Json)
                setBody(
                    SupabaseAuthRequest(
                        email = email,
                        password = password,
                        data = mapOf(
                            "full_name" to fullName,
                            "avatar_color" to "#FF6B4A"
                        )
                    )
                )
            }

            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val jsonObj = json.parseToJsonElement(body).jsonObject
                val userId = jsonObj["id"]?.jsonPrimitive?.contentOrNull
                    ?: jsonObj["user"]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
                    ?: "usr_${Clock.System.now().toEpochMilliseconds()}"

                currentUserId = userId
                val token = jsonObj["access_token"]?.jsonPrimitive?.contentOrNull
                if (token != null) {
                    currentAccessToken = token
                }
                val refreshToken = jsonObj["refresh_token"]?.jsonPrimitive?.contentOrNull
                if (refreshToken != null) {
                    currentRefreshToken = refreshToken
                }

                val user = User(
                    id = userId,
                    fullName = fullName,
                    email = email,
                    avatarColorHex = "#FF6B4A"
                )
                Result.success(user)
            } else {
                val errorMsg = parseErrorMessage(body)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> = withContext(Dispatchers.Default) {
        try {
            val response = httpClient.post("${SupabaseConfig.PROJECT_URL}/auth/v1/token?grant_type=password") {
                header("apikey", SupabaseConfig.ANON_KEY)
                contentType(ContentType.Application.Json)
                setBody(
                    SupabaseAuthRequest(
                        email = email,
                        password = password
                    )
                )
            }

            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val jsonObj = json.parseToJsonElement(body).jsonObject
                currentAccessToken = jsonObj["access_token"]?.jsonPrimitive?.contentOrNull
                currentRefreshToken = jsonObj["refresh_token"]?.jsonPrimitive?.contentOrNull

                val userObj = jsonObj["user"]?.jsonObject
                val userId = userObj?.get("id")?.jsonPrimitive?.contentOrNull
                    ?: "usr_${Clock.System.now().toEpochMilliseconds()}"
                currentUserId = userId
                val userMetadata = userObj?.get("user_metadata")?.jsonObject

                val extractedName = userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    ?: email.substringBefore("@").replace(".", " ").capitalizeWords()
                val avatarColor = userMetadata?.get("avatar_color")?.jsonPrimitive?.contentOrNull
                    ?: "#FF6B4A"
                val planStr = userMetadata?.get("plan")?.jsonPrimitive?.contentOrNull ?: "BASIC"
                val userPlan = try { UserPlan.valueOf(planStr) } catch (e: Exception) { UserPlan.BASIC }

                val user = User(
                    id = userId,
                    fullName = extractedName,
                    email = email,
                    avatarColorHex = avatarColor,
                    plan = userPlan
                )
                Result.success(user)
            } else {
                val errorMsg = parseErrorMessage(body)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(fullName: String, avatarColorHex: String): Result<Unit> = withContext(Dispatchers.Default) {
        var token = currentAccessToken ?: return@withContext Result.failure(Exception("Not signed in"))
        try {
            var response = httpClient.put("${SupabaseConfig.PROJECT_URL}/auth/v1/user") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "data" to mapOf(
                            "full_name" to fullName,
                            "avatar_color" to avatarColorHex
                        )
                    )
                )
            }
            if (response.status.value == 401 || response.bodyAsText().contains("token is expired", ignoreCase = true)) {
                val refreshRes = refreshSession()
                if (refreshRes.isSuccess) {
                    token = refreshRes.getOrThrow()
                    response = httpClient.put("${SupabaseConfig.PROJECT_URL}/auth/v1/user") {
                        header("apikey", SupabaseConfig.ANON_KEY)
                        header("Authorization", "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "data" to mapOf(
                                    "full_name" to fullName,
                                    "avatar_color" to avatarColorHex
                                )
                            )
                        )
                    }
                }
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val err = parseErrorMessage(response.bodyAsText())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserPlan(plan: UserPlan): Result<Unit> = withContext(Dispatchers.Default) {
        var token = currentAccessToken ?: return@withContext Result.failure(Exception("Not signed in"))
        try {
            var response = httpClient.put("${SupabaseConfig.PROJECT_URL}/auth/v1/user") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "data" to mapOf(
                            "plan" to plan.name
                        )
                    )
                )
            }
            if (response.status.value == 401 || response.bodyAsText().contains("token is expired", ignoreCase = true)) {
                val refreshRes = refreshSession()
                if (refreshRes.isSuccess) {
                    token = refreshRes.getOrThrow()
                    response = httpClient.put("${SupabaseConfig.PROJECT_URL}/auth/v1/user") {
                        header("apikey", SupabaseConfig.ANON_KEY)
                        header("Authorization", "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "data" to mapOf(
                                    "plan" to plan.name
                                )
                            )
                        )
                    }
                }
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val err = parseErrorMessage(response.bodyAsText())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = withContext(Dispatchers.Default) {
        var token = currentAccessToken ?: return@withContext Result.failure(Exception("Not signed in"))
        try {
            var response = httpClient.put("${SupabaseConfig.PROJECT_URL}/auth/v1/user") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf("password" to newPassword)
                )
            }
            // Auto-refresh expired JWT token seamlessly and retry once
            val rawBody = response.bodyAsText()
            if (response.status.value == 401 || rawBody.contains("token is expired", ignoreCase = true) || rawBody.contains("invalid claims", ignoreCase = true)) {
                val refreshRes = refreshSession()
                if (refreshRes.isSuccess) {
                    token = refreshRes.getOrThrow()
                    response = httpClient.put("${SupabaseConfig.PROJECT_URL}/auth/v1/user") {
                        header("apikey", SupabaseConfig.ANON_KEY)
                        header("Authorization", "Bearer $token")
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf("password" to newPassword)
                        )
                    }
                } else {
                    return@withContext Result.failure(Exception("Session expired. Please sign out and sign in again."))
                }
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val err = parseErrorMessage(response.bodyAsText())
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ----------------------------------------------------
    // Database Operations (PostgreSQL via PostgREST)
    // ----------------------------------------------------

    suspend fun fetchSubscriptions(): Result<List<Subscription>> = withContext(Dispatchers.Default) {
        val token = currentAccessToken ?: return@withContext Result.success(emptyList())
        try {
            val response = httpClient.get("${SupabaseConfig.PROJECT_URL}/rest/v1/subscriptions?select=*&limit=100&order=created_at.desc") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            if (response.status.isSuccess()) {
                val list = json.decodeFromString<List<SupabaseSubscriptionDto>>(response.bodyAsText())
                Result.success(list.map { it.toDomain() })
            } else {
                Result.failure(Exception("Fetch failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertSubscription(sub: Subscription): Result<Unit> = withContext(Dispatchers.Default) {
        val token = currentAccessToken ?: return@withContext Result.failure(Exception("Not signed in"))
        val userId = currentUserId ?: return@withContext Result.failure(Exception("User ID missing"))
        try {
            val dto = sub.toDto(userId)
            val response = httpClient.post("${SupabaseConfig.PROJECT_URL}/rest/v1/subscriptions") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $token")
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(dto)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to save: ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSubscription(sub: Subscription): Result<Unit> = withContext(Dispatchers.Default) {
        val token = currentAccessToken ?: return@withContext Result.failure(Exception("Not signed in"))
        val userId = currentUserId ?: return@withContext Result.failure(Exception("User ID missing"))
        try {
            val dto = sub.toDto(userId)
            val response = httpClient.patch("${SupabaseConfig.PROJECT_URL}/rest/v1/subscriptions?id=eq.${sub.id}") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(dto)
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update: ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSubscription(id: String): Result<Unit> = withContext(Dispatchers.Default) {
        val token = currentAccessToken ?: return@withContext Result.failure(Exception("Not signed in"))
        try {
            val response = httpClient.delete("${SupabaseConfig.PROJECT_URL}/rest/v1/subscriptions?id=eq.$id") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete: ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        currentAccessToken = null
        currentRefreshToken = null
        currentUserId = null
    }

    private fun parseErrorMessage(jsonBody: String): String {
        return try {
            val element = json.parseToJsonElement(jsonBody).jsonObject
            val msg = element["msg"]?.jsonPrimitive?.contentOrNull
                ?: element["error_description"]?.jsonPrimitive?.contentOrNull
                ?: element["message"]?.jsonPrimitive?.contentOrNull
                ?: element["error"]?.jsonPrimitive?.contentOrNull
            when (msg) {
                "Invalid login credentials" -> "Invalid email or password."
                "Email not confirmed" -> "Email not confirmed. Please check your inbox or disable email confirmation in Supabase dashboard."
                "email rate limit exceeded" -> "Too many attempts. Please wait a minute or disable email confirmation in Supabase dashboard."
                "User already registered" -> "An account with this email already exists."
                else -> msg ?: "Authentication failed ($jsonBody)"
            }
        } catch (e: Exception) {
            "Network error: ${e.message}"
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }
}
