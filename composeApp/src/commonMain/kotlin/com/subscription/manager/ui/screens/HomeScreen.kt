package com.subscription.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.model.Subscription
import com.subscription.manager.theme.*
import com.subscription.manager.ui.components.*
import com.subscription.manager.viewmodel.AppTab
import com.subscription.manager.viewmodel.SubscriptionViewModel

@Composable
fun HomeScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val subscriptions by viewModel.subscriptions.collectAsState()
    val upcomingSubscriptions by viewModel.upcomingSubscriptions.collectAsState()
    val totalMonthlySpend by viewModel.totalMonthlySpend.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val formattedBalance = viewModel.formatCurrency(totalMonthlySpend)
    val formattedBudget = viewModel.formatCurrency(monthlyBudget)
    val spentRatio = (totalMonthlySpend / monthlyBudget.coerceAtLeast(1.0)).toFloat()
    val activeCount = subscriptions.count { it.isActive }

    // Dynamically find the next upcoming billing date
    val nextBillingLabel = remember(upcomingSubscriptions) {
        val earliest = upcomingSubscriptions.minByOrNull { it.daysUntilRenewal }
        if (earliest != null) {
            val parts = earliest.nextBillingDateFormatted.split("/", "-", ".")
            if (parts.size >= 2) "${parts[0]}/${parts[1]}" else earliest.nextBillingDateFormatted
        } else "--/--"
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top User Profile Header
        item {
            TopHeader(
                userName = currentUser?.fullName ?: userName,
                userInitials = currentUser?.initials,
                avatarColorHex = currentUser?.avatarColorHex,
                notificationCount = upcomingSubscriptions.count { it.daysUntilRenewal <= 3 },
                onNotificationsClick = {
                    viewModel.showToast("${upcomingSubscriptions.count { it.daysUntilRenewal <= 7 }} subscriptions renewing soon!")
                },
                onSearchClick = {
                    viewModel.selectTab(AppTab.SUBSCRIPTIONS)
                },
                isPro = currentUser?.isPro ?: false
            )
        }

        // Orange Balance Hero Card
        item {
            BalanceHeroCard(
                formattedBalance = formattedBalance,
                billingDate = nextBillingLabel,
                monthlyBudgetFormatted = formattedBudget,
                spentRatio = spentRatio,
                activeCount = activeCount
            )
        }

        // Upcoming Carousel (Horizontal Scroll) - only shown when subscriptions exist
        if (upcomingSubscriptions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                UpcomingCarousel(
                    upcomingSubscriptions = upcomingSubscriptions,
                    formatPrice = { viewModel.formatCurrency(it) },
                    onViewAllClick = { viewModel.selectTab(AppTab.SUBSCRIPTIONS) },
                    onSubscriptionClick = { viewModel.openDetail(it) }
                )
            }
        }

        // All Subscriptions Header
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Subscriptions",
                    style = FigmaTypography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )

                Text(
                    text = "View all",
                    style = FigmaTypography.labelMedium,
                    color = appColors.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.selectTab(AppTab.SUBSCRIPTIONS) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Subscriptions List Items (Brand pastel cards from Figma or Empty State)
        if (subscriptions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = appColors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, appColors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(FigmaOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Outlined.AddCircleOutline,
                                contentDescription = null,
                                tint = FigmaOrange,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Subscriptions Yet",
                            style = FigmaTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add your recurring bills and subscriptions to start tracking renewal dates and spending.",
                            style = FigmaTypography.bodySmall,
                            color = appColors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Button(
                            onClick = { viewModel.openAddDialog() },
                            shape = RoundedCornerShape(14.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = FigmaOrange,
                                contentColor = FigmaWhite
                            )
                        ) {
                            Text(
                                text = "+ Add Subscription",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(subscriptions) { sub ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    SubscriptionCard(
                        subscription = sub,
                        formattedPrice = viewModel.formatCurrency(sub.price),
                        onClick = { viewModel.openDetail(sub) }
                    )
                }
            }
        }
    }
}
