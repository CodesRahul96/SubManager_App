package com.subscription.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.subscription.manager.model.Subscription
import com.subscription.manager.theme.*

@Composable
fun SubscriptionDetailDialog(
    subscription: Subscription,
    formattedMonthlyPrice: String,
    formattedYearlyPrice: String,
    onDismiss: () -> Unit,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val appColors = LocalAppColors.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = if (appColors.isDark) Color(0xFF181A22) else FigmaWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF282B36) else Color(0xFFE5E7EB)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column {
                // Top Color Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color(subscription.colorHex)),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.25f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = FigmaWhite, modifier = Modifier.size(18.dp))
                    }

                    // Centered Brand Avatar overlapping banner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 30.dp)
                            .size(68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (appColors.isDark) Color(0xFF222430) else FigmaWhite)
                            .border(2.dp, if (appColors.isDark) Color(0xFF333748) else FigmaWhite, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subscription.name.take(2).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = if (appColors.isDark) FigmaWhite else Color(0xFF1A1C20)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Subscription Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = subscription.name,
                        style = FigmaTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                    )

                    if (subscription.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subscription.description,
                            style = FigmaTypography.bodyMedium,
                            color = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price Tag
                    Text(
                        text = formattedMonthlyPrice,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                    )
                    Text(
                        text = "per ${subscription.billingCycle.displayName.lowercase()} ($formattedYearlyPrice / year)",
                        style = FigmaTypography.bodySmall,
                        color = if (appColors.isDark) Color(0xFF9EA3B5) else appColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key Specs Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = if (appColors.isDark) Color(0xFF13141A) else appColors.background,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF252834) else appColors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow("Category", subscription.category.displayName, appColors)
                            HorizontalDivider(color = if (appColors.isDark) Color(0xFF252834) else appColors.border, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Next Billing Date", subscription.nextBillingDateFormatted, appColors)
                            HorizontalDivider(color = if (appColors.isDark) Color(0xFF252834) else appColors.border, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Renewal Countdown", "${subscription.daysUntilRenewal} days remaining", appColors)
                            HorizontalDivider(color = if (appColors.isDark) Color(0xFF252834) else appColors.border, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Reminder Alert", "${subscription.reminderDaysBefore} days before renew", appColors)
                            if (subscription.notes.isNotBlank()) {
                                HorizontalDivider(color = if (appColors.isDark) Color(0xFF252834) else appColors.border, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 8.dp))
                                DetailRow("Notes", subscription.notes, appColors)
                            }
                        }
                    }

                    if (subscription.websiteUrl.isNotBlank()) {
                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                val url = if (!subscription.websiteUrl.startsWith("http://") && !subscription.websiteUrl.startsWith("https://")) {
                                    "https://${subscription.websiteUrl}"
                                } else {
                                    subscription.websiteUrl
                                }
                                try {
                                    uriHandler.openUri(url)
                                } catch (e: Exception) {
                                    // ignore open uri error
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF2E3240) else Color(0xFFE5E7EB))
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp), tint = FigmaOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Visit Website", color = if (appColors.isDark) FigmaWhite else appColors.textPrimary, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Status Toggle Button
                    Button(
                        onClick = onToggleActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subscription.isActive) (if (appColors.isDark) Color(0xFF2A2D3C) else Color(0xFF263238)) else FigmaOrange
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (subscription.isActive) "Pause Subscription" else "Resume Subscription",
                            fontWeight = FontWeight.SemiBold,
                            color = FigmaWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Edit & Delete row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (appColors.isDark) Color(0xFFDCDFEA) else appColors.textPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF333748) else appColors.border)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit")
                        }

                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FigmaDanger.copy(alpha = 0.15f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = FigmaDanger, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", color = FigmaDanger, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = if (appColors.isDark) Color(0xFF1C1E26) else FigmaWhite,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Delete Subscription",
                    fontWeight = FontWeight.Bold,
                    color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${subscription.name}? This cannot be undone.",
                    color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = FigmaDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, appColors: CustomAppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = FigmaTypography.bodySmall, color = if (appColors.isDark) Color(0xFF9EA3B5) else appColors.textSecondary)
        Text(text = value, style = FigmaTypography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (appColors.isDark) FigmaWhite else appColors.textPrimary)
    }
}
