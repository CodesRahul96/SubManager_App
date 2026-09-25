package com.subscription.manager.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
enum class UserPlan(val displayName: String, val maxSubscriptions: Int) {
    BASIC("Basic (Free)", 10),
    PRO("Renewo Pro", 200)
}

@Serializable
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarColorHex: String = "#FF6B4A",
    val plan: UserPlan = UserPlan.BASIC,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    val isPro: Boolean
        get() = plan == UserPlan.PRO

    val initials: String
        get() = fullName.trim()
            .split(Regex("\\s+"))
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { "U" }

    val firstName: String
        get() = fullName.trim().split(Regex("\\s+")).firstOrNull()?.ifBlank { "User" } ?: "User"
}

@Serializable
data class RegisteredUserRecord(
    val user: User,
    val passwordHash: String
)
