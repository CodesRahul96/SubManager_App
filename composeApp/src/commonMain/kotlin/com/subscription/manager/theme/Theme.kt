package com.subscription.manager.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.subscription.manager.model.AppThemeMode

data class CustomAppColors(
    val isDark: Boolean,
    val background: Color,
    val cardBackground: Color,
    val dockBackground: Color,
    val dockBorder: Color,
    val dockInactiveIcon: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val cardTintAlpha: Float
)

val LocalAppColors = staticCompositionLocalOf {
    CustomAppColors(
        isDark = false,
        background = LightBackground,
        cardBackground = LightCardBackground,
        dockBackground = LightDockBackground,
        dockBorder = LightBorder,
        dockInactiveIcon = LightTextSecondary,
        border = LightBorder,
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        textMuted = LightTextMuted,
        cardTintAlpha = 0.88f
    )
}

private val LightColorScheme = lightColorScheme(
    primary = FigmaOrange,
    onPrimary = FigmaWhite,
    primaryContainer = FigmaOrangeLight,
    onPrimaryContainer = FigmaWhite,
    secondary = LightDockBackground,
    onSecondary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightCardBackground,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBackground,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = FigmaOrange,
    onPrimary = FigmaWhite,
    primaryContainer = FigmaOrangeDark,
    onPrimaryContainer = FigmaWhite,
    secondary = DarkDockBackground,
    onSecondary = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkCardBackground,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBackground,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

@Composable
fun SubscriptionAppTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val customColors = if (isDark) {
        CustomAppColors(
            isDark = true,
            background = DarkBackground,
            cardBackground = DarkCardBackground,
            dockBackground = DarkDockBackground,
            dockBorder = DarkBorder,
            dockInactiveIcon = DarkTextSecondary,
            border = DarkBorder,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = DarkTextMuted,
            cardTintAlpha = 0.40f
        )
    } else {
        CustomAppColors(
            isDark = false,
            background = LightBackground,
            cardBackground = LightCardBackground,
            dockBackground = LightDockBackground, // Clean white dock in light mode!
            dockBorder = LightBorder,
            dockInactiveIcon = LightTextSecondary,
            border = LightBorder,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            cardTintAlpha = 0.88f
        )
    }

    CompositionLocalProvider(LocalAppColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FigmaTypography,
            content = content
        )
    }
}
