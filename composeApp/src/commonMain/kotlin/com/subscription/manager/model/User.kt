package com.subscription.manager.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarColorHex: String = "#FF6B4A",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
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
