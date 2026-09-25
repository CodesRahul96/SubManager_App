package com.subscription.manager

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.data.AuthRepository
import com.subscription.manager.data.SubscriptionRepository
import com.subscription.manager.theme.FigmaOrange
import com.subscription.manager.theme.FigmaWhite
import com.subscription.manager.theme.LocalAppColors
import com.subscription.manager.theme.SubscriptionAppTheme
import com.subscription.manager.ui.components.AddSubscriptionDialog
import com.subscription.manager.ui.components.BottomNavDock
import com.subscription.manager.ui.components.SubscriptionDetailDialog
import com.subscription.manager.ui.screens.AuthScreen
import com.subscription.manager.ui.screens.HomeScreen
import com.subscription.manager.ui.screens.InsightsScreen
import com.subscription.manager.ui.screens.SettingsScreen
import com.subscription.manager.ui.screens.SubscriptionsScreen
import com.subscription.manager.ui.util.BackHandler
import com.subscription.manager.viewmodel.AppTab
import com.subscription.manager.viewmodel.SubscriptionViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

@Composable
fun App(
    onExitApp: () -> Unit = {},
    onThemeChanged: (isDark: Boolean) -> Unit = {}
) {
    val supabaseClient = remember { com.subscription.manager.data.remote.SupabaseClient() }
    val authRepository = remember { AuthRepository(supabaseClient) }
    val repository = remember { SubscriptionRepository(supabaseClient) }
    val viewModel = remember { SubscriptionViewModel(repository, authRepository) }

    val uiState by viewModel.uiState.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    // Compute actual isDark from app theme mode (handles SYSTEM/LIGHT/DARK overrides)
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        com.subscription.manager.model.AppThemeMode.DARK -> true
        com.subscription.manager.model.AppThemeMode.LIGHT -> false
        com.subscription.manager.model.AppThemeMode.SYSTEM -> isSystemDark
    }

    // Notify Activity to update status bar icon tint with correct isDark value
    SideEffect {
        onThemeChanged(isDark)
    }

    // Collect OAuth deep link tokens and authenticate
    LaunchedEffect(Unit) {
        com.subscription.manager.data.remote.OAuthBridge.oauthTokens.collect { (accessToken, refreshToken) ->
            viewModel.handleOAuthCallback(accessToken, refreshToken)
        }
    }

    var lastBackPressTime by rememberSaveable { mutableStateOf(0L) }

    // Intercept back button to prevent accidental app exits
    BackHandler(enabled = true) {
        if (!isAuthenticated) {
            // On Auth Screen: double back to exit
            val now = Clock.System.now().toEpochMilliseconds()
            if (now - lastBackPressTime < 2000L) {
                onExitApp()
            } else {
                lastBackPressTime = now
                viewModel.showToast("Press back again to exit")
            }
        } else if (uiState.isAddDialogOpen) {
            viewModel.closeAddDialog()
        } else if (uiState.detailSubscription != null) {
            viewModel.closeDetail()
        } else if (uiState.activeTab != AppTab.HOME) {
            // In another tab: return to Home tab
            viewModel.selectTab(AppTab.HOME)
        } else {
            // On Home tab: double back within 2 seconds to exit
            val now = Clock.System.now().toEpochMilliseconds()
            if (now - lastBackPressTime < 2000L) {
                onExitApp()
            } else {
                lastBackPressTime = now
                viewModel.showToast("Press back again to exit")
            }
        }
    }

    // Auto dismiss toast after 3 seconds
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            delay(3000)
            viewModel.dismissToast()
        }
    }

    SubscriptionAppTheme(themeMode = themeMode) {
        val appColors = LocalAppColors.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.background)
        ) {
            val authFlowState = when {
                !isAuthenticated -> 0 // Auth Screen
                uiState.isOAuthPasswordSetupRequired -> 1 // Create Password for OAuth users
                uiState.isNewUserSetupRequired -> 2 // Onboarding Setup (Currency, Budget, Subscriptions)
                else -> 3 // Dashboard
            }

            AnimatedContent(
                targetState = authFlowState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier.fillMaxSize()
            ) { state ->
                when (state) {
                    0 -> {
                        // Real User Authentication Screen (Sign Up / Sign In / Google OAuth)
                        AuthScreen(viewModel = viewModel)
                    }
                    1 -> {
                        // OAuth New User Password Creation Step
                        com.subscription.manager.ui.screens.OAuthPasswordSetupScreen(
                            viewModel = viewModel,
                            onPasswordSet = {
                                viewModel.completeOAuthPasswordSetup()
                            }
                        )
                    }
                    2 -> {
                        // New Account Onboarding & Setup Screen (Currency, Budget, Starter Subscriptions, Alerts)
                        com.subscription.manager.ui.screens.OnboardingSetupScreen(
                            viewModel = viewModel,
                            onComplete = {
                                viewModel.completeNewUserSetup()
                            }
                        )
                    }
                    else -> {
                        // Authenticated Main Dashboard
                    Scaffold(
                        containerColor = appColors.background,
                        contentColor = appColors.textPrimary
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .background(appColors.background)
                        ) {
                            // Active Screen Content with Crossfade animation
                            Crossfade(
                                targetState = uiState.activeTab,
                                modifier = Modifier.fillMaxSize()
                            ) { tab ->
                                when (tab) {
                                    AppTab.HOME -> HomeScreen(viewModel = viewModel)
                                    AppTab.SUBSCRIPTIONS -> SubscriptionsScreen(viewModel = viewModel)
                                    AppTab.INSIGHTS -> InsightsScreen(viewModel = viewModel)
                                    AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                                }
                            }

                            // Floating Bottom Navigation Dock (Adapts to light & dark theme)
                            BottomNavDock(
                                currentTab = uiState.activeTab,
                                onTabSelected = { viewModel.selectTab(it) },
                                onAddClick = { viewModel.openAddDialog() },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )

                            // Add / Edit Subscription Dialog
                            if (uiState.isAddDialogOpen) {
                                AddSubscriptionDialog(
                                    subscriptionToEdit = uiState.editingSubscription,
                                    currentCurrency = selectedCurrency,
                                    onDismiss = { viewModel.closeAddDialog() },
                                    onSave = { id, name, desc, price, curr, cycle, cat, days, nextDate, color, rem, notes, web ->
                                        viewModel.saveSubscription(
                                            id = id,
                                            name = name,
                                            description = desc,
                                            price = price,
                                            currency = curr,
                                            billingCycle = cycle,
                                            category = cat,
                                            daysUntilRenewal = days,
                                            nextBillingDateFormatted = nextDate,
                                            colorHex = color,
                                            reminderDaysBefore = rem,
                                            notes = notes,
                                            websiteUrl = web
                                        )
                                    }
                                )
                            }

                            // Subscription Details Dialog
                            if (uiState.detailSubscription != null) {
                                val sub = uiState.detailSubscription!!
                                SubscriptionDetailDialog(
                                    subscription = sub,
                                    formattedMonthlyPrice = viewModel.formatCurrency(sub.monthlyEquivalent),
                                    formattedYearlyPrice = viewModel.formatCurrency(sub.yearlyEquivalent),
                                    onDismiss = { viewModel.closeDetail() },
                                    onToggleActive = { viewModel.toggleSubscriptionActive(sub.id) },
                                    onEdit = {
                                        viewModel.closeDetail()
                                        viewModel.openAddDialog(sub)
                                    },
                                    onDelete = { viewModel.deleteSubscription(sub.id) }
                                )
                            }

                            // Renewo Pro Upgrade Paywall Dialog
                            if (uiState.isUpgradePaywallOpen) {
                                com.subscription.manager.ui.components.UpgradePaywallDialog(
                                    viewModel = viewModel,
                                    onDismiss = { viewModel.closeUpgradePaywall() }
                                )
                            }
                        }
                    }
                }
            }
        }

            // Global Toast Notification Banner
            AnimatedVisibility(
                visible = uiState.toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (appColors.isDark) appColors.cardBackground else FigmaOrange,
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = uiState.toastMessage ?: "",
                        color = FigmaWhite,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
