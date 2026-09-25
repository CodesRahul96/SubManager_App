package com.subscription.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.model.AppThemeMode
import com.subscription.manager.model.Currency
import com.subscription.manager.theme.*
import com.subscription.manager.ui.components.AppTextFieldDefaults
import com.subscription.manager.viewmodel.SubscriptionViewModel

@Composable
fun SettingsScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    val avatarBgColor = remember(currentUser?.avatarColorHex) {
        val hex = currentUser?.avatarColorHex
        if (!hex.isNullOrBlank()) {
            try {
                androidx.compose.ui.graphics.Color(hex.removePrefix("#").toLong(16) or 0xFF000000L)
            } catch (e: Exception) {
                FigmaOrange
            }
        } else {
            FigmaOrange
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Settings & Preferences",
                style = FigmaTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Card (Clickable to customize)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { showProfileDialog = true },
                shape = RoundedCornerShape(22.dp),
                color = if (appColors.isDark) Color(0xFF181A22) else appColors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF282B36) else appColors.border)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(avatarBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.initials ?: userName.split(Regex("\\s+")).mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifEmpty { "U" },
                            color = FigmaWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.fullName ?: userName,
                            style = FigmaTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = currentUser?.email ?: "Local Account",
                                style = FigmaTypography.bodySmall,
                                color = if (appColors.isDark) Color(0xFF9EA3B2) else appColors.textSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (currentUser?.isPro == true) FigmaOrange else (if (appColors.isDark) Color(0xFF2E313E) else Color(0xFFE5E7EB))
                            ) {
                                Text(
                                    text = if (currentUser?.isPro == true) "PRO" else "BASIC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (currentUser?.isPro == true) FigmaWhite else (if (appColors.isDark) Color(0xFFB0B4C4) else Color(0xFF4B5563)),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tap to customize name & avatar color",
                            fontSize = 11.sp,
                            color = FigmaOrange,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Profile",
                        tint = if (appColors.isDark) Color(0xFF7E8292) else appColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pro Membership Card / Banner
            if (currentUser?.isPro != true) {
                Surface(
                    onClick = { viewModel.openUpgradePaywall() },
                    shape = RoundedCornerShape(20.dp),
                    color = if (appColors.isDark) Color(0xFF261D19) else Color(0xFFFFF7ED),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FigmaOrange.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(FigmaOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = FigmaWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Upgrade to Pro",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (appColors.isDark) FigmaWhite else FigmaTextPrimary
                                )
                                Text(
                                    text = "Track up to 200 subscriptions & unlock CSV export",
                                    fontSize = 11.sp,
                                    color = if (appColors.isDark) Color(0xFFD1D5DB) else Color(0xFF6B7280)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Outlined.ArrowForwardIos,
                            contentDescription = null,
                            tint = FigmaOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (appColors.isDark) Color(0xFF1E2822) else Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = FigmaWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Renewo Pro Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (appColors.isDark) FigmaWhite else FigmaTextPrimary
                            )
                            Text(
                                text = "Up to 200 subscriptions • All features unlocked",
                                fontSize = 11.sp,
                                color = if (appColors.isDark) Color(0xFFD1D5DB) else Color(0xFF6B7280)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Section: Account & Security (Moved to top right under Profile)
        item {
            Text(
                text = "Account & Security",
                style = FigmaTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (appColors.isDark) Color(0xFF181A22) else appColors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF282B36) else appColors.border)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.Email,
                        title = "Account Email",
                        subtitle = currentUser?.email ?: "local@device.user",
                        onClick = { viewModel.showToast("Logged in as: ${currentUser?.email ?: "local@device.user"}") }
                    )
                    HorizontalDivider(color = if (appColors.isDark) Color(0xFF262934) else appColors.border, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Lock,
                        title = "Change Password",
                        subtitle = "Update your account login password",
                        onClick = { showPasswordDialog = true }
                    )
                    HorizontalDivider(color = if (appColors.isDark) Color(0xFF262934) else appColors.border, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Logout,
                        title = "Sign Out",
                        subtitle = "Switch account or log out of this device",
                        textColor = FigmaDanger,
                        onClick = { showLogoutConfirm = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Appearance & Theme
        item {
            Text(
                text = "Appearance",
                style = FigmaTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (appColors.isDark) Color(0xFF181A22) else appColors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF282B36) else appColors.border)
            ) {
                Column {
                    SettingsItem(
                        icon = if (appColors.isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        title = "App Theme",
                        subtitle = currentThemeMode.displayName,
                        onClick = { showThemeDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Preferences
        item {
            Text(
                text = "Financial Settings",
                style = FigmaTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (appColors.isDark) Color(0xFF181A22) else appColors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF282B36) else appColors.border)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.AttachMoney,
                        title = "Currency",
                        subtitle = "${selectedCurrency.name} (${selectedCurrency.symbol})",
                        onClick = { showCurrencyDialog = true }
                    )
                    HorizontalDivider(color = if (appColors.isDark) Color(0xFF262934) else appColors.border, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.AccountBalance,
                        title = "Monthly Budget Limit",
                        subtitle = viewModel.formatCurrency(monthlyBudget),
                        onClick = { showBudgetDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Notifications & Alerts
        item {
            Text(
                text = "Notifications & Reminders",
                style = FigmaTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (appColors.isDark) Color(0xFF181A22) else appColors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF282B36) else appColors.border)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.NotificationsActive,
                        title = "Renewal Alerts",
                        subtitle = "Alert 3 days before charge",
                        onClick = { viewModel.showToast("Default alert is set to 3 days before renewal") }
                    )
                    HorizontalDivider(color = if (appColors.isDark) Color(0xFF262934) else appColors.border, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Email,
                        title = "Weekly Digest",
                        subtitle = "Summary report every Sunday",
                        onClick = { viewModel.showToast("Weekly spending summary enabled") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }


        // Section: Data & Backup
        item {
            Text(
                text = "Data & App Management",
                style = FigmaTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (appColors.isDark) Color(0xFF181A22) else appColors.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF282B36) else appColors.border)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "Export Data (CSV / JSON)",
                        subtitle = if (currentUser?.isPro == true) "Export subscriptions & billing history" else "Pro feature • Export subscriptions as CSV",
                        onClick = {
                            if (currentUser?.isPro == true) {
                                viewModel.exportDataAsCsv()
                            } else {
                                viewModel.openUpgradePaywall()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Version info footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Renewo v1.1.0",
                    fontSize = 12.sp,
                    color = if (appColors.isDark) Color(0xFF7E8292) else appColors.textMuted
                )
            }
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = if (appColors.isDark) Color(0xFF1C1E26) else FigmaWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Choose Theme",
                    fontWeight = FontWeight.Bold,
                    color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = mode == currentThemeMode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) (if (appColors.isDark) Color(0xFF2C2522) else FigmaOrange.copy(alpha = 0.1f))
                                    else Color.Transparent
                                )
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mode.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FigmaOrange else if (appColors.isDark) Color(0xFFDCDFEA) else appColors.textPrimary
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = FigmaOrange)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Done", color = FigmaOrange, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Currency Selection Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            containerColor = if (appColors.isDark) Color(0xFF1C1E26) else FigmaWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Select Currency",
                    fontWeight = FontWeight.Bold,
                    color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Currency.entries.forEach { currency ->
                        val isSelected = currency == selectedCurrency
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) (if (appColors.isDark) Color(0xFF2C2522) else FigmaOrange.copy(alpha = 0.1f))
                                    else Color.Transparent
                                )
                                .clickable {
                                    viewModel.changeCurrency(currency)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currency.code} - ${currency.symbol}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FigmaOrange else if (appColors.isDark) Color(0xFFDCDFEA) else appColors.textPrimary
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = FigmaOrange)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Done", color = FigmaOrange, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Monthly Budget Dialog
    if (showBudgetDialog) {
        val currentBudgetInSelected = monthlyBudget * selectedCurrency.rateToUsd
        val initialInput = if (currentBudgetInSelected <= 0.0) "" else {
            val rounded = kotlin.math.round(currentBudgetInSelected * 100.0) / 100.0
            if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
        }
        var budgetInput by remember(showBudgetDialog, monthlyBudget, selectedCurrency) { mutableStateOf(initialInput) }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            containerColor = if (appColors.isDark) Color(0xFF1C1E26) else FigmaWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Set Monthly Budget Limit",
                    fontWeight = FontWeight.Bold,
                    color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Budget (${selectedCurrency.code})") },
                    placeholder = { Text("e.g. 1500") },
                    leadingIcon = { Text(selectedCurrency.symbol, fontWeight = FontWeight.Bold, color = FigmaOrange) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    colors = AppTextFieldDefaults.colors(
                        appColors = appColors,
                        containerColor = if (appColors.isDark) Color(0xFF14151B) else FigmaWhite
                    ),
                    textStyle = AppTextFieldDefaults.textStyle(appColors),
                    shape = RoundedCornerShape(14.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull() ?: 0.0
                        viewModel.changeBudget(amount)
                        showBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save", color = FigmaWhite, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel", color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = if (appColors.isDark) Color(0xFF1C1E26) else FigmaWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Sign Out?",
                    fontWeight = FontWeight.Bold,
                    color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                )
            },
            text = {
                Text(
                    "Are you sure you want to sign out of your account on this device?",
                    color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOut()
                        showLogoutConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaDanger),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign Out", color = FigmaWhite, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary)
                }
            }
        )
    }

    // Profile Customization Dialog
    if (showProfileDialog) {
        val profilePalette = listOf(
            "#FF6B4A", // Coral Figma Orange
            "#3B82F6", // Royal Blue
            "#10B981", // Emerald Green
            "#8B5CF6", // Royal Purple
            "#EC4899", // Neon Pink
            "#F59E0B", // Amber Gold
            "#06B6D4", // Cyan Blue
            "#EF4444"  // Ruby Red
        )

        var tempName by remember { mutableStateOf(currentUser?.fullName ?: userName) }
        var tempColor by remember { mutableStateOf(currentUser?.avatarColorHex ?: "#FF6B4A") }

        val previewAvatarColor = remember(tempColor) {
            try {
                androidx.compose.ui.graphics.Color(tempColor.removePrefix("#").toLong(16) or 0xFF000000L)
            } catch (e: Exception) {
                FigmaOrange
            }
        }

        val previewInitials = remember(tempName) {
            tempName.trim().split(Regex("\\s+"))
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")
                .ifEmpty { "U" }
        }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = if (appColors.isDark) Color(0xFF1C1E26) else FigmaWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Customize Profile",
                    fontWeight = FontWeight.Bold,
                    color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Live Avatar Preview with dynamic initials and accent color
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(previewAvatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = previewInitials,
                            color = FigmaWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("Your full name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppTextFieldDefaults.colors(
                            appColors = appColors,
                            containerColor = if (appColors.isDark) Color(0xFF14151B) else FigmaWhite
                        ),
                        textStyle = AppTextFieldDefaults.textStyle(appColors)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Choose Profile Accent Color",
                        style = FigmaTypography.bodySmall,
                        color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 10.dp)
                    )

                    // Palette Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        profilePalette.take(4).forEach { hex ->
                            val color = androidx.compose.ui.graphics.Color(hex.removePrefix("#").toLong(16) or 0xFF000000L)
                            val isSelected = tempColor == hex
                            Surface(
                                onClick = { tempColor = hex },
                                shape = CircleShape,
                                color = color,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, FigmaWhite) else null,
                                shadowElevation = if (isSelected) 4.dp else 1.dp,
                                modifier = Modifier.size(46.dp)
                            ) {
                                if (isSelected) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = FigmaWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Palette Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        profilePalette.drop(4).forEach { hex ->
                            val color = androidx.compose.ui.graphics.Color(hex.removePrefix("#").toLong(16) or 0xFF000000L)
                            val isSelected = tempColor == hex
                            Surface(
                                onClick = { tempColor = hex },
                                shape = CircleShape,
                                color = color,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, FigmaWhite) else null,
                                shadowElevation = if (isSelected) 4.dp else 1.dp,
                                modifier = Modifier.size(46.dp)
                            ) {
                                if (isSelected) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = FigmaWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.updateProfile(tempName, tempColor)
                        }
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", color = FigmaWhite, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel", color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary)
                }
            }
        )
    }

    // Change Password Dialog
    if (showPasswordDialog) {
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!isSaving) showPasswordDialog = false
            },
            containerColor = if (appColors.isDark) Color(0xFF1C1E26) else FigmaWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = FigmaOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Change Password",
                        fontWeight = FontWeight.Bold,
                        color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter a new secure password of at least 6 characters.",
                        style = FigmaTypography.bodySmall,
                        color = if (appColors.isDark) Color(0xFF9EA3B2) else appColors.textSecondary
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMsg = null
                        },
                        label = { Text("New Password") },
                        placeholder = { Text("At least 6 characters") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppTextFieldDefaults.colors(
                            appColors = appColors,
                            containerColor = if (appColors.isDark) Color(0xFF14151B) else FigmaWhite
                        ),
                        textStyle = AppTextFieldDefaults.textStyle(appColors)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMsg = null
                        },
                        label = { Text("Confirm New Password") },
                        placeholder = { Text("Re-enter new password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppTextFieldDefaults.colors(
                            appColors = appColors,
                            containerColor = if (appColors.isDark) Color(0xFF14151B) else FigmaWhite
                        ),
                        textStyle = AppTextFieldDefaults.textStyle(appColors)
                    )

                    if (errorMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FigmaDanger.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FigmaDanger.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = errorMsg ?: "",
                                color = FigmaDanger,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validationErr = com.subscription.manager.util.SecurityValidator.validatePassword(newPassword)
                        when {
                            validationErr != null -> {
                                errorMsg = validationErr
                            }
                            newPassword != confirmPassword -> {
                                errorMsg = "Passwords do not match."
                            }
                            else -> {
                                isSaving = true
                                viewModel.changePassword(newPassword) { success, msg ->
                                    isSaving = false
                                    if (success) {
                                        showPasswordDialog = false
                                    } else {
                                        errorMsg = msg
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaOrange),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    Text(
                        if (isSaving) "Updating..." else "Update Password",
                        color = FigmaWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordDialog = false },
                    enabled = !isSaving
                ) {
                    Text("Cancel", color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    val effectiveTextColor = textColor ?: appColors.textPrimary
    val iconBg = if (textColor != null) {
        textColor.copy(alpha = 0.12f)
    } else if (appColors.isDark) {
        Color(0xFF242735)
    } else {
        FigmaOrange.copy(alpha = 0.08f)
    }
    val iconTint = textColor ?: if (appColors.isDark) Color(0xFFFF9E80) else FigmaOrange

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = FigmaTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = effectiveTextColor
                )
                Text(
                    text = subtitle,
                    style = FigmaTypography.bodySmall,
                    color = if (appColors.isDark) Color(0xFF9EA3B5) else appColors.textSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = if (appColors.isDark) Color(0xFF6B7082) else appColors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
