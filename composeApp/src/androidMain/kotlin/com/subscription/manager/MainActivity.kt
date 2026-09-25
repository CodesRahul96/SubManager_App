package com.subscription.manager

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.subscription.manager.data.storage.AppContext.init(applicationContext)

        // Enable edge-to-edge with fully transparent bars
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )

        setContent {
            App(
                onExitApp = { finish() },
                onThemeChanged = { isDark -> applyStatusBarIconColor(isDark) }
            )
        }
    }

    /**
     * Sets status bar and navigation bar icon color based on the app's resolved dark/light theme.
     * isDark = true  → white icons (light icons on dark background)
     * isDark = false → dark icons (dark icons on light background)
     */
    private fun applyStatusBarIconColor(isDark: Boolean) {
        val decorView: View = window.decorView
        WindowCompat.getInsetsController(window, decorView).apply {
            isAppearanceLightStatusBars = !isDark      // false = white icons in dark mode ✓
            isAppearanceLightNavigationBars = !isDark  // false = white icons in dark mode ✓
        }
    }
}
