package com.subscription.manager.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.theme.*
import com.subscription.manager.ui.components.AppTextFieldDefaults
import com.subscription.manager.viewmodel.SubscriptionViewModel
import androidx.compose.foundation.text.selection.TextSelectionColors

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val focusManager = LocalFocusManager.current
    val authError by viewModel.authError.collectAsState()
    val isLoading by viewModel.isAuthLoading.collectAsState()

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // App Logo Icon Pill
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(FigmaOrange),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = "Subscription App",
                tint = FigmaWhite,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title and Subtitle with Smooth Fade Transition
        AnimatedContent(
            targetState = authMode,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.96f)) togetherWith
                        (fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.96f))
            }
        ) { mode ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (mode == AuthMode.SIGN_IN) "Welcome Back" else "Create Account",
                    style = FigmaTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (mode == AuthMode.SIGN_IN)
                        "Sign in with your email to access your subscriptions"
                    else
                        "Sign up to track and optimize your recurring subscriptions",
                    style = FigmaTypography.bodyMedium,
                    color = appColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Error message banner
        AnimatedVisibility(visible = authError != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FigmaDanger.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, FigmaDanger.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = FigmaDanger,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authError ?: "",
                        fontSize = 13.sp,
                        color = FigmaDanger,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        val authTextFieldColors = AppTextFieldDefaults.colors(
            appColors = appColors,
            containerColor = if (appColors.isDark) Color(0xFF1A1C26) else appColors.cardBackground
        )

        // Form Fields
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Full Name (Smooth expand/collapse for Sign Up)
            AnimatedVisibility(
                visible = authMode == AuthMode.SIGN_UP,
                enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeIn(tween(200)),
                exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Full Name",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (appColors.isDark) Color(0xFFB0B4C8) else appColors.textSecondary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("e.g. Rahul Sharma", color = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null,
                                tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary,
                                modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(16.dp),
                        colors = authTextFieldColors,
                        textStyle = androidx.compose.ui.text.TextStyle(color = if (appColors.isDark) FigmaWhite else appColors.textPrimary, fontSize = 15.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Email Address
            Text(
                text = "Email Address",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (appColors.isDark) Color(0xFFB0B4C8) else appColors.textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("user@example.com", color = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted) },
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = null,
                        tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary,
                        modifier = Modifier.size(20.dp))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                shape = RoundedCornerShape(16.dp),
                colors = authTextFieldColors,
                textStyle = androidx.compose.ui.text.TextStyle(color = if (appColors.isDark) FigmaWhite else appColors.textPrimary, fontSize = 15.sp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Password
            Text(
                text = "Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (appColors.isDark) Color(0xFFB0B4C8) else appColors.textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("At least 6 characters", color = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted) },
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null,
                        tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary,
                        modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (authMode == AuthMode.SIGN_UP) {
                            viewModel.signUp(fullName, email, password)
                        } else {
                            viewModel.signIn(email, password)
                        }
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                colors = authTextFieldColors,
                textStyle = androidx.compose.ui.text.TextStyle(color = if (appColors.isDark) FigmaWhite else appColors.textPrimary, fontSize = 15.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }


        Spacer(modifier = Modifier.height(26.dp))

        // Primary Submit Button
        Button(
            onClick = {
                focusManager.clearFocus()
                if (authMode == AuthMode.SIGN_UP) {
                    viewModel.signUp(fullName, email, password)
                } else {
                    viewModel.signIn(email, password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FigmaOrange,
                contentColor = FigmaWhite
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = FigmaWhite,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                AnimatedContent(
                    targetState = authMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(150))
                    }
                ) { mode ->
                    Text(
                        text = if (mode == AuthMode.SIGN_UP) "Create Account" else "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Toggle Option Link between Sign In and Create Account
        AnimatedContent(
            targetState = authMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(150))
            }
        ) { mode ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        authMode = if (mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN
                        viewModel.clearAuthError()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mode == AuthMode.SIGN_IN) "Don't have an account? " else "Already have an account? ",
                    style = FigmaTypography.bodyMedium,
                    color = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary
                )
                Text(
                    text = if (mode == AuthMode.SIGN_IN) "Create Account" else "Sign In",
                    style = FigmaTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = FigmaOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
