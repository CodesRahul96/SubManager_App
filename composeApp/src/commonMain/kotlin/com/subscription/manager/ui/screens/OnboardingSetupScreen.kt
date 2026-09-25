package com.subscription.manager.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.model.Currency
import com.subscription.manager.theme.*
import com.subscription.manager.ui.components.AppTextFieldDefaults
import com.subscription.manager.ui.components.PopularPresets
import com.subscription.manager.ui.components.PresetBrand
import com.subscription.manager.viewmodel.SubscriptionViewModel

@Composable
fun OnboardingSetupScreen(
    viewModel: SubscriptionViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    var currentStep by remember { mutableStateOf(1) } // 1: Currency & Budget, 2: Select Subscriptions, 3: Reminder

    // Step 1 State
    var selectedCurrency by remember { mutableStateOf(Currency.USD) }
    var monthlyBudgetText by remember { mutableStateOf("0") }

    // Step 2 State (Selected starter subscriptions)
    val selectedPresets = remember { mutableStateListOf<PresetBrand>() }

    // Step 3 State
    var reminderDays by remember { mutableStateOf(3) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = appColors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header with Progress Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(appColors.cardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = appColors.textPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                // Step Progress Indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { stepIndex ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (currentStep == stepIndex) 28.dp else 12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (stepIndex <= currentStep) FigmaOrange else appColors.border
                                )
                        )
                    }
                }

                // Skip Button
                TextButton(
                    onClick = {
                        applySetupAndComplete(
                            viewModel = viewModel,
                            currency = selectedCurrency,
                            budgetText = monthlyBudgetText,
                            selectedPresets = selectedPresets,
                            reminderDays = reminderDays,
                            onComplete = onComplete
                        )
                    }
                ) {
                    Text(
                        text = "Skip",
                        color = appColors.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step Content Animated Transition
            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    1 -> StepCurrencyAndBudget(
                        currency = selectedCurrency,
                        onCurrencySelected = { selectedCurrency = it },
                        budgetText = monthlyBudgetText,
                        onBudgetChange = { monthlyBudgetText = it },
                        appColors = appColors
                    )
                    2 -> StepSelectStarterSubscriptions(
                        selectedPresets = selectedPresets,
                        currency = selectedCurrency,
                        appColors = appColors
                    )
                    3 -> StepReminderPreference(
                        reminderDays = reminderDays,
                        onSelectReminder = { reminderDays = it },
                        appColors = appColors
                    )
                }
            }

            // Bottom Action Button
            Button(
                onClick = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        applySetupAndComplete(
                            viewModel = viewModel,
                            currency = selectedCurrency,
                            budgetText = monthlyBudgetText,
                            selectedPresets = selectedPresets,
                            reminderDays = reminderDays,
                            onComplete = onComplete
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FigmaOrange,
                    contentColor = FigmaWhite
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentStep == 3) "Finish Setup & Enter App" else "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepCurrencyAndBudget(
    currency: Currency,
    onCurrencySelected: (Currency) -> Unit,
    budgetText: String,
    onBudgetChange: (String) -> Unit,
    appColors: CustomAppColors
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                text = "Welcome! Let's set up your budget",
                style = FigmaTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose your preferred currency and your target monthly subscription limit.",
                style = FigmaTypography.bodyMedium,
                color = appColors.textSecondary
            )
        }

        item {
            Text(
                text = "Select Currency",
                style = FigmaTypography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Currency.entries.forEach { curr ->
                    val isSelected = currency == curr
                    Surface(
                        onClick = { onCurrencySelected(curr) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) (if (appColors.isDark) Color(0xFF2C2522) else FigmaOrange.copy(alpha = 0.12f)) else appColors.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF282B36) else appColors.border)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF282B36) else appColors.border)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = curr.symbol,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) FigmaWhite else appColors.textPrimary,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = curr.name,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = curr.code,
                                        fontSize = 12.sp,
                                        color = appColors.textSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FigmaOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Target Monthly Budget (${currency.symbol})",
                style = FigmaTypography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = budgetText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' }) {
                        onBudgetChange(input)
                    }
                },
                leadingIcon = {
                    Text(
                        text = currency.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FigmaOrange
                    )
                },
                placeholder = { Text("e.g. 150") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = AppTextFieldDefaults.colors(
                    appColors = appColors,
                    containerColor = if (appColors.isDark) Color(0xFF14151B) else appColors.cardBackground
                ),
                textStyle = AppTextFieldDefaults.textStyle(appColors),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StepSelectStarterSubscriptions(
    selectedPresets: MutableList<PresetBrand>,
    currency: Currency,
    appColors: CustomAppColors
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Add your existing services",
                style = FigmaTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap any services you currently use to add them right away. You can also add custom ones later.",
                style = FigmaTypography.bodyMedium,
                color = appColors.textSecondary
            )
        }

        item {
            Text(
                text = "Popular Subscriptions (${selectedPresets.size} selected)",
                style = FigmaTypography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textSecondary
            )
        }

        items(PopularPresets) { preset ->
            val isSelected = selectedPresets.contains(preset)
            val convertedPrice = preset.defaultPrice * currency.rateToUsd
            val priceFormatted = if (convertedPrice >= 100) "${currency.symbol}${convertedPrice.toInt()}" else "${currency.symbol}${kotlin.math.round(convertedPrice * 10.0) / 10.0}"

            Surface(
                onClick = {
                    if (isSelected) {
                        selectedPresets.remove(preset)
                    } else {
                        selectedPresets.add(preset)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) (if (appColors.isDark) Color(preset.colorHex).copy(alpha = 0.22f) else Color(preset.colorHex).copy(alpha = 0.35f)) else appColors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF282B36) else appColors.border)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(preset.colorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.name.take(2).uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A1C20),
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = preset.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = appColors.textPrimary
                            )
                            Text(
                                text = "$priceFormatted / mo • ${preset.category.displayName}",
                                fontSize = 12.sp,
                                color = appColors.textSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF282B36) else appColors.border)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = FigmaWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StepReminderPreference(
    reminderDays: Int,
    onSelectReminder: (Int) -> Unit,
    appColors: CustomAppColors
) {
    val options = listOf(
        1 to "1 Day Before (Recommended for quick renewals)",
        2 to "2 Days Before",
        3 to "3 Days Before (Default - gives time to cancel or review)",
        5 to "5 Days Before",
        7 to "1 Week Before (Best for yearly plans)"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Renewal Alerts",
            style = FigmaTypography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )
        Text(
            text = "How many days before billing should we alert you so you never get charged by surprise?",
            style = FigmaTypography.bodyMedium,
            color = appColors.textSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        options.forEach { (days, label) ->
            val isSelected = reminderDays == days
            Surface(
                onClick = { onSelectReminder(days) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) (if (appColors.isDark) Color(0xFF2C2522) else FigmaOrange.copy(alpha = 0.12f)) else appColors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF282B36) else appColors.border)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = if (isSelected) FigmaOrange else appColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFFDCDFEA) else appColors.textPrimary)
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FigmaOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun applySetupAndComplete(
    viewModel: SubscriptionViewModel,
    currency: Currency,
    budgetText: String,
    selectedPresets: List<PresetBrand>,
    reminderDays: Int,
    onComplete: () -> Unit
) {
    // 1. Set selected currency
    viewModel.changeCurrency(currency)

    // 2. Set monthly budget
    val budgetValue = budgetText.toDoubleOrNull() ?: 0.0
    viewModel.changeBudget(budgetValue)

    // 3. Add selected starter subscriptions (if any)
    selectedPresets.forEachIndexed { index, preset ->
        val presetPrice = preset.defaultPrice * currency.rateToUsd
        viewModel.saveSubscription(
            id = null,
            name = preset.name,
            description = "Recurring subscription",
            price = presetPrice,
            currency = currency,
            billingCycle = com.subscription.manager.model.BillingCycle.MONTHLY,
            category = preset.category,
            daysUntilRenewal = 14 + (index * 3),
            nextBillingDateFormatted = "Next Month",
            colorHex = preset.colorHex,
            reminderDaysBefore = reminderDays,
            notes = "",
            websiteUrl = preset.websiteUrl
        )
    }

    onComplete()
}
