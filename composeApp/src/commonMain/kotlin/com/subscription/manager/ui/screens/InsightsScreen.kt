package com.subscription.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.model.PaymentHistoryItem
import com.subscription.manager.theme.*
import com.subscription.manager.ui.components.InsightsBarChart
import com.subscription.manager.viewmodel.SubscriptionViewModel

@Composable
fun InsightsScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()
    val weeklySpending by viewModel.weeklySpending.collectAsState()
    val paymentHistory by viewModel.paymentHistory.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val totalMonthlySpend by viewModel.totalMonthlySpend.collectAsState()
    val totalMonthlySpendFormatted = viewModel.formatCurrency(totalMonthlySpend)

    val currentUser by viewModel.currentUser.collectAsState()
    val isPro = currentUser?.isPro ?: false

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header matching Figma
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Insights",
                    style = FigmaTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )

                IconButton(
                    onClick = {
                        if (isPro) {
                            viewModel.showToast("Report exported for March 2026")
                        } else {
                            viewModel.openUpgradePaywall()
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.border, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = appColors.textPrimary
                    )
                }
            }
        }

        // Interactive Bar Chart
        item {
            InsightsBarChart(
                weeklySpending = weeklySpending,
                selectedDay = uiState.insightsSelectedDay,
                onSelectDay = { viewModel.selectInsightsDay(it) },
                formatAmount = { viewModel.formatCurrency(it) }
            )
        }

        // Expenses Summary Card (matching Figma: "Expenses March 2026: -$183.77 +12%")
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(elevation = if (appColors.isDark) 2.dp else 4.dp, shape = RoundedCornerShape(22.dp))
                    .border(1.dp, appColors.border, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                color = appColors.cardBackground
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Expenses",
                            style = FigmaTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                        Text(
                            text = "March 2026",
                            style = FigmaTypography.bodySmall,
                            color = appColors.textSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "-$totalMonthlySpendFormatted",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = appColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = FigmaDanger,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+12%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaDanger
                            )
                        }
                    }
                }
            }
        }

        // Category Breakdown Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, appColors.border, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                color = appColors.cardBackground
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spending by Category",
                            style = FigmaTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )

                        if (isPro) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FigmaOrange
                            ) {
                                Text(
                                    text = "PRO ANALYTICS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FigmaWhite,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (categoryBreakdown.isEmpty()) {
                        Text(
                            text = "No category spending data yet",
                            style = FigmaTypography.bodySmall,
                            color = appColors.textSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        val total = categoryBreakdown.values.sum().coerceAtLeast(1.0)
                        val displayCategories = if (isPro) {
                            categoryBreakdown.entries.sortedByDescending { it.value }
                        } else {
                            categoryBreakdown.entries.sortedByDescending { it.value }.take(3)
                        }

                        displayCategories.forEach { (cat, amount) ->
                            val ratio = (amount / total).toFloat()
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.textPrimary
                                    )
                                    Text(
                                        text = "${viewModel.formatCurrency(amount)} (${(ratio * 100).toInt()}%)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appColors.textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(cat.defaultColorHex),
                                    trackColor = appColors.background
                                )
                            }
                        }

                        if (!isPro && categoryBreakdown.size > 3) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = { viewModel.openUpgradePaywall() },
                                shape = RoundedCornerShape(12.dp),
                                color = if (appColors.isDark) Color(0xFF261D19) else Color(0xFFFFF7ED),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FigmaOrange.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "+${categoryBreakdown.size - 3} more categories available in Pro",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FigmaOrange
                                    )
                                    Text(
                                        text = "Unlock",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FigmaOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // History Section (Matching Left Screen in Figma)
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    style = FigmaTypography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )

                Text(
                    text = "View all",
                    style = FigmaTypography.labelMedium,
                    color = appColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Payment History Items matching Figma's Claude & Canva cards or Empty State
        if (paymentHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No payment history yet",
                        style = FigmaTypography.bodyMedium,
                        color = appColors.textSecondary
                    )
                }
            }
        } else {
            items(paymentHistory) { item ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    PaymentHistoryCard(
                        item = item,
                        formattedAmount = viewModel.formatCurrency(item.amount)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryCard(
    item: PaymentHistoryItem,
    formattedAmount: String
) {
    val appColors = LocalAppColors.current

    val containerColor = if (appColors.isDark) {
        Color(item.colorHex).copy(alpha = 0.20f)
    } else {
        Color(item.colorHex).copy(alpha = 0.88f)
    }

    val borderColor = if (appColors.isDark) {
        Color(item.colorHex).copy(alpha = 0.45f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = if (appColors.isDark) 1.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (appColors.isDark) Color(0xFF282A34) else FigmaWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name.take(2).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (appColors.isDark) FigmaWhite else FigmaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = appColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.dateFormatted,
                        fontSize = 12.sp,
                        color = appColors.textSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedAmount,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.billingCycleText,
                    fontSize = 11.sp,
                    color = appColors.textSecondary
                )
            }
        }
    }
}
