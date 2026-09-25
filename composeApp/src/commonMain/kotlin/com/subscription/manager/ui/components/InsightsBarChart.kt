package com.subscription.manager.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.model.DaySpending
import com.subscription.manager.theme.*

@Composable
fun InsightsBarChart(
    weeklySpending: List<DaySpending>,
    selectedDay: DaySpending?,
    onSelectDay: (DaySpending) -> Unit,
    formatAmount: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val maxSpend = weeklySpending.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 50.0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(
                elevation = if (appColors.isDark) 2.dp else 4.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .border(width = 1.dp, color = appColors.border, shape = RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        color = appColors.cardBackground
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp)
        ) {
            // Chart Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Activity",
                    style = FigmaTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )

                if (selectedDay != null) {
                    Text(
                        text = "${selectedDay.dayName}: ${formatAmount(selectedDay.amount)}",
                        style = FigmaTypography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            if (weeklySpending.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No weekly spending data yet",
                        style = FigmaTypography.bodyMedium,
                        color = appColors.textSecondary
                    )
                }
            } else {
                // Bars Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                weeklySpending.forEach { day ->
                    val isSelected = selectedDay?.dayName == day.dayName || (selectedDay == null && day.isPeak)
                    val barHeightRatio = (day.amount / maxSpend).toFloat().coerceIn(0.12f, 1f)
                    
                    val animatedHeight by animateFloatAsState(
                        targetValue = barHeightRatio,
                        animationSpec = tween(durationMillis = 600)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelectDay(day) }
                            )
                    ) {
                        // Tooltip for selected / peak bar
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FigmaOrange)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$${day.amount.toInt()}",
                                    color = FigmaWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Bar Pillar
                        val barColor = if (isSelected) {
                            FigmaOrange
                        } else if (appColors.isDark) {
                            Color(0xFF2E313D)
                        } else {
                            Color(0xFF1E1F25)
                        }

                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .fillMaxHeight(animatedHeight * 0.75f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(barColor)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Day label
                        Text(
                            text = day.dayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) FigmaOrange else appColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
}
