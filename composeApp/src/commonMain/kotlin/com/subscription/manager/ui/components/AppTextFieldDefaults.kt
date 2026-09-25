package com.subscription.manager.ui.components

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.subscription.manager.theme.CustomAppColors
import com.subscription.manager.theme.FigmaDanger
import com.subscription.manager.theme.FigmaOrange
import com.subscription.manager.theme.FigmaWhite
import com.subscription.manager.theme.LocalAppColors

/**
 * Standard app-wide text field styling to guarantee optimal contrast,
 * readable typed text, clear labels and placeholders in both dark and light modes.
 */
object AppTextFieldDefaults {

    @Composable
    fun textStyle(appColors: CustomAppColors = LocalAppColors.current): TextStyle {
        return TextStyle(
            color = if (appColors.isDark) FigmaWhite else appColors.textPrimary,
            fontSize = 15.sp
        )
    }

    @Composable
    fun colors(
        appColors: CustomAppColors = LocalAppColors.current,
        containerColor: Color? = null
    ): TextFieldColors {
        val defaultContainer = if (appColors.isDark) Color(0xFF14151B) else Color(0xFFF9FAFB)
        val container = containerColor ?: defaultContainer

        val selColors = TextSelectionColors(
            handleColor = FigmaOrange,
            backgroundColor = FigmaOrange.copy(alpha = 0.3f)
        )

        return OutlinedTextFieldDefaults.colors(
            focusedTextColor = if (appColors.isDark) FigmaWhite else appColors.textPrimary,
            unfocusedTextColor = if (appColors.isDark) FigmaWhite else appColors.textPrimary,
            disabledTextColor = if (appColors.isDark) FigmaWhite.copy(alpha = 0.6f) else appColors.textPrimary.copy(alpha = 0.6f),
            errorTextColor = FigmaDanger,

            focusedContainerColor = container,
            unfocusedContainerColor = container,
            disabledContainerColor = if (appColors.isDark) Color(0xFF101116) else Color(0xFFF3F4F6),
            errorContainerColor = container,

            focusedBorderColor = FigmaOrange,
            unfocusedBorderColor = if (appColors.isDark) Color(0xFF2C2F3D) else appColors.border,
            disabledBorderColor = if (appColors.isDark) Color(0xFF22242F) else appColors.border.copy(alpha = 0.5f),
            errorBorderColor = FigmaDanger,

            focusedLabelColor = FigmaOrange,
            unfocusedLabelColor = if (appColors.isDark) Color(0xFFB0B4C8) else appColors.textSecondary,
            disabledLabelColor = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted,
            errorLabelColor = FigmaDanger,

            focusedPlaceholderColor = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted,
            unfocusedPlaceholderColor = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted,

            cursorColor = FigmaOrange,
            errorCursorColor = FigmaDanger,
            selectionColors = selColors,

            focusedLeadingIconColor = FigmaOrange,
            unfocusedLeadingIconColor = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary,
            focusedTrailingIconColor = FigmaOrange,
            unfocusedTrailingIconColor = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary
        )
    }
}
