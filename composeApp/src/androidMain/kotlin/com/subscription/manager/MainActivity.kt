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

        handleOAuthIntent(intent)

        setContent {
            App(
                onExitApp = { finish() },
                onThemeChanged = { isDark -> applyStatusBarIconColor(isDark) }
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "renewo" && uri.host == "auth-callback") {
            // OAuth redirect with fragment #access_token=...&refresh_token=... or query params
            val rawFragment = uri.fragment ?: ""
            val rawQuery = uri.query ?: ""
            val paramString = if (rawFragment.isNotBlank()) rawFragment else rawQuery

            val params = paramString.split("&").associate { param ->
                val parts = param.split("=")
                if (parts.size == 2) parts[0] to parts[1] else "" to ""
            }

            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"] ?: ""

            if (!accessToken.isNullOrBlank()) {
                com.subscription.manager.data.remote.OAuthBridge.onAuthTokensReceived(accessToken, refreshToken)
            }
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
