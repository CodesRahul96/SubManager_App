package com.subscription.manager.ui.util

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler as AndroidActivityBackHandler

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    AndroidActivityBackHandler(enabled = enabled, onBack = onBack)
}
