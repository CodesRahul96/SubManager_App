package com.subscription.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.model.Subscription
import com.subscription.manager.theme.*

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    formattedPrice: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    val containerColor = if (appColors.isDark) {
        Color(subscription.colorHex).copy(alpha = if (subscription.isActive) 0.20f else 0.08f)
    } else {
        Color(subscription.colorHex).copy(alpha = if (subscription.isActive) 0.88f else 0.45f)
    }

    val borderColor = if (appColors.isDark) {
        Color(subscription.colorHex).copy(alpha = if (subscription.isActive) 0.45f else 0.18f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(width = if (appColors.isDark) 1.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Name & Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Brand Avatar Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (appColors.isDark) Color(0xFF282A34) else FigmaWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subscription.name.take(2).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (appColors.isDark) FigmaWhite else FigmaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subscription.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = appColors.textPrimary
                        )
                        if (!subscription.isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(appColors.textSecondary.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Paused",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = appColors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subscription.nextBillingDateFormatted,
                        fontSize = 12.sp,
                        color = appColors.textSecondary
                    )
                }
            }

            // Right: Price & Frequency
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formattedPrice,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "per ${subscription.billingCycle.displayName.lowercase()}",
                    fontSize = 11.sp,
                    color = appColors.textSecondary
                )
            }
        }
    }
}
