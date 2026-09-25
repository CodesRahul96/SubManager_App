package com.subscription.manager.ui.util

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop platforms do not have a physical/system back navigation event
}
