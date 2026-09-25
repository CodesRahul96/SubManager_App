package com.subscription.manager.util

object SecurityValidator {

    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    /**
     * Strict email validation preventing malicious payloads
     */
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.length in 5..254 && EMAIL_REGEX.matches(trimmed)
    }

    /**
     * Password strength check:
     * - Minimum 8 characters
     * - At least one uppercase or lowercase letter
     * - At least one digit or special character
     */
    fun validatePassword(password: String): String? {
        if (password.length < 8) {
            return "Password must be at least 8 characters long."
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigitOrSpecial = password.any { !it.isLetter() }

        if (!hasLetter) {
            return "Password must contain at least one letter."
        }
        if (!hasDigitOrSpecial) {
            return "Password must contain at least one number or special character."
        }
        return null
    }

    /**
     * Sanitizes inputs to prevent injection or invalid characters
     */
    fun sanitizeInput(input: String, maxLength: Int = 100): String {
        return input.trim()
            .replace("\r", "")
            .replace("\n", " ")
            .take(maxLength)
    }

    /**
     * Validates subscription price (positive number, up to 999,999)
     */
    fun validatePrice(price: Double): Boolean {
        return price >= 0.0 && price <= 999_999.0 && !price.isNaN() && !price.isInfinite()
    }
}
