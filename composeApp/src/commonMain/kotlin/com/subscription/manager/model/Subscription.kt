package com.subscription.manager.model

import kotlinx.serialization.Serializable

@Serializable
enum class BillingCycle(val displayName: String, val monthsMultiplier: Double) {
    WEEKLY("Weekly", 4.33),
    MONTHLY("Monthly", 1.0),
    QUARTERLY("Quarterly", 0.333),
    YEARLY("Yearly", 0.0833)
}

@Serializable
enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

@Serializable
enum class SubscriptionCategory(val displayName: String, val iconName: String, val defaultColorHex: Long) {
    AI("AI & Tools", "auto_awesome", 0xFF6C8EEF),
    DESIGN("Design & Media", "palette", 0xFFFFC107),
    PRODUCTIVITY("Productivity", "task_alt", 0xFF4CAF50),
    ENTERTAINMENT("Entertainment", "movie", 0xFFFF5252),
    DEV("Developer Tools", "terminal", 0xFF9C27B0),
    CLOUD("Cloud & Storage", "cloud", 0xFF00BCD4),
    OTHER("Other", "category", 0xFF78909C)
}

@Serializable
enum class Currency(val code: String, val symbol: String, val rateToUsd: Double) {
    USD("USD", "$", 1.0),
    EUR("EUR", "€", 0.92),
    GBP("GBP", "£", 0.79),
    INR("INR", "₹", 83.5),
    CAD("CAD", "CA$", 1.36),
    AUD("AUD", "AU$", 1.51),
    JPY("JPY", "¥", 155.0)
}

@Serializable
data class Subscription(
    val id: String,
    val name: String,
    val description: String = "",
    val price: Double,
    val originalCurrency: Currency = Currency.USD,
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val category: SubscriptionCategory = SubscriptionCategory.PRODUCTIVITY,
    val nextBillingDateFormatted: String, // e.g. "June 25, 2026"
    val daysUntilRenewal: Int,
    val colorHex: Long, // 0xFFxxxxxx
    val iconName: String = "default",
    val isActive: Boolean = true,
    val reminderDaysBefore: Int = 3,
    val notes: String = "",
    val websiteUrl: String = ""
) {
    val monthlyEquivalent: Double
        get() = when (billingCycle) {
            BillingCycle.WEEKLY -> price * 4.33
            BillingCycle.MONTHLY -> price
            BillingCycle.QUARTERLY -> price / 3.0
            BillingCycle.YEARLY -> price / 12.0
        }

    val yearlyEquivalent: Double
        get() = monthlyEquivalent * 12.0
}

@Serializable
data class PaymentHistoryItem(
    val id: String,
    val subscriptionId: String,
    val name: String,
    val dateFormatted: String, // e.g. "June 25, 12:00"
    val amount: Double,
    val currency: Currency = Currency.USD,
    val billingCycleText: String = "per month",
    val colorHex: Long,
    val category: SubscriptionCategory
)

data class DaySpending(
    val dayName: String, // "Mon", "Tue", etc.
    val amount: Double,
    val isPeak: Boolean = false
)
