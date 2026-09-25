package com.subscription.manager.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.subscription.manager.theme.*
import com.subscription.manager.viewmodel.SubscriptionViewModel

@Composable
fun UpgradePaywallDialog(
    viewModel: SubscriptionViewModel,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isPro = currentUser?.isPro ?: false

    var selectedCycle by remember { mutableStateOf("YEARLY") } // "YEARLY" or "MONTHLY"
    var isUpgrading by remember { mutableStateOf(false) }

    val dialogBg = if (appColors.isDark) Color(0xFF161822) else FigmaWhite
    val dialogBorder = if (appColors.isDark) Color(0xFF282B36) else Color(0xFFE5E7EB)

    Dialog(
        onDismissRequest = {
            if (!isUpgrading) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = dialogBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, dialogBorder),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isUpgrading,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (appColors.isDark) Color(0xFF242735) else Color(0xFFF3F4F6))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (appColors.isDark) FigmaWhite else appColors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Pro Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(FigmaOrange, Color(0xFFFF9E80))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = FigmaWhite,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Upgrade to Renewo Pro",
                    style = FigmaTypography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (appColors.isDark) FigmaWhite else appColors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Unlock higher limits, full data exports, and premium financial tracking features.",
                    style = FigmaTypography.bodySmall,
                    color = if (appColors.isDark) Color(0xFF9EA3B5) else appColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Comparison List
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (appColors.isDark) Color(0xFF1D202C) else Color(0xFFF9FAFB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, dialogBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProFeatureRow(
                            title = "Up to 200 Subscriptions",
                            subtitle = "Free tier capped at 10 active subscriptions",
                            icon = Icons.Outlined.AllInclusive
                        )
                        HorizontalDivider(color = dialogBorder, thickness = 0.8.dp)
                        ProFeatureRow(
                            title = "Full Data Export (CSV & JSON)",
                            subtitle = "Download detailed payment records anytime",
                            icon = Icons.Outlined.FileDownload
                        )
                        HorizontalDivider(color = dialogBorder, thickness = 0.8.dp)
                        ProFeatureRow(
                            title = "Advanced Spending Analytics",
                            subtitle = "Deep category breakdown & peak spending days",
                            icon = Icons.Outlined.Insights
                        )
                        HorizontalDivider(color = dialogBorder, thickness = 0.8.dp)
                        ProFeatureRow(
                            title = "Cloud Sync & Multi-Device",
                            subtitle = "Encrypted Supabase sync across all devices",
                            icon = Icons.Outlined.CloudDone
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Plan pricing selection cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Yearly Option (Best Value)
                    PlanCard(
                        title = "Yearly",
                        priceText = "$19.99 / yr",
                        subText = "$1.66 / mo (Save 45%)",
                        isBestValue = true,
                        isSelected = selectedCycle == "YEARLY",
                        onClick = { selectedCycle = "YEARLY" },
                        modifier = Modifier.weight(1f)
                    )

                    // Monthly Option
                    PlanCard(
                        title = "Monthly",
                        priceText = "$2.99 / mo",
                        subText = "Cancel anytime",
                        isBestValue = false,
                        isSelected = selectedCycle == "MONTHLY",
                        onClick = { selectedCycle = "MONTHLY" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Upgrade Button
                Button(
                    onClick = {
                        isUpgrading = true
                        viewModel.upgradeToPro {
                            isUpgrading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaOrange),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isUpgrading && !isPro
                ) {
                    if (isUpgrading) {
                        CircularProgressIndicator(
                            color = FigmaWhite,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else if (isPro) {
                        Text(
                            text = "You are currently on Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FigmaWhite
                        )
                    } else {
                        Text(
                            text = if (selectedCycle == "YEARLY") "Upgrade to Pro – $19.99/yr" else "Upgrade to Pro – $2.99/mo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FigmaWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "No commitments. Instant activation.",
                    fontSize = 11.sp,
                    color = if (appColors.isDark) Color(0xFF7E8292) else appColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    priceText: String,
    subText: String,
    isBestValue: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) {
            if (appColors.isDark) Color(0xFF33231B) else FigmaOrange.copy(alpha = 0.10f)
        } else {
            if (appColors.isDark) Color(0xFF1D202C) else Color(0xFFF9FAFB)
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF2E3240) else Color(0xFFE5E7EB))
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isBestValue) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FigmaOrange,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "BEST VALUE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FigmaWhite,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isSelected) FigmaOrange else appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = priceText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subText,
                fontSize = 11.sp,
                color = if (appColors.isDark) Color(0xFF9EA3B5) else appColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProFeatureRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val appColors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(FigmaOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FigmaOrange,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = appColors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (appColors.isDark) Color(0xFF9EA3B5) else appColors.textSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = FigmaOrange,
            modifier = Modifier.size(18.dp)
        )
    }
}
