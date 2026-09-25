package com.subscription.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.theme.*
import com.subscription.manager.ui.components.AppTextFieldDefaults
import com.subscription.manager.util.SecurityValidator
import com.subscription.manager.viewmodel.SubscriptionViewModel

@Composable
fun OAuthPasswordSetupScreen(
    viewModel: SubscriptionViewModel,
    onPasswordSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = appColors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Lock Icon Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FigmaOrange.copy(alpha = 0.15f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = FigmaOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Create Account Password",
                style = FigmaTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Since you signed in with Google, set up a secure password so you can also log in directly anytime.",
                style = FigmaTypography.bodyMedium,
                color = appColors.textSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Error banner if any
            if (errorMsg != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FigmaDanger.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FigmaDanger.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMsg ?: "",
                        color = FigmaDanger,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // New Password Field
            Text(
                text = "New Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (appColors.isDark) Color(0xFFB0B4C8) else appColors.textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMsg = null
                },
                placeholder = { Text("At least 8 characters", color = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = AppTextFieldDefaults.colors(
                    appColors = appColors,
                    containerColor = if (appColors.isDark) Color(0xFF1A1C26) else appColors.cardBackground
                ),
                textStyle = AppTextFieldDefaults.textStyle(appColors)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            Text(
                text = "Confirm Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (appColors.isDark) Color(0xFFB0B4C8) else appColors.textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMsg = null
                },
                placeholder = { Text("Re-enter new password", color = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = AppTextFieldDefaults.colors(
                    appColors = appColors,
                    containerColor = if (appColors.isDark) Color(0xFF1A1C26) else appColors.cardBackground
                ),
                textStyle = AppTextFieldDefaults.textStyle(appColors)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    val validationErr = SecurityValidator.validatePassword(password)
                    when {
                        validationErr != null -> {
                            errorMsg = validationErr
                        }
                        password != confirmPassword -> {
                            errorMsg = "Passwords do not match."
                        }
                        else -> {
                            isSaving = true
                            viewModel.changePassword(password) { success, msg ->
                                isSaving = false
                                if (success) {
                                    onPasswordSet()
                                } else {
                                    errorMsg = msg
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FigmaOrange,
                    contentColor = FigmaWhite
                ),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = FigmaWhite,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Save Password & Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
