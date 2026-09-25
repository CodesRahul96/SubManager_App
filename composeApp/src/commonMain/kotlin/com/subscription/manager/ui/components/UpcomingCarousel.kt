package com.subscription.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
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
fun UpcomingCarousel(
    upcomingSubscriptions: List<Subscription>,
    formatPrice: (Double) -> String,
    onViewAllClick: () -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming",
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
                    .clickable(onClick = onViewAllClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }

        // Horizontal Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(upcomingSubscriptions.take(6)) { sub ->
                UpcomingItemCard(
                    subscription = sub,
                    formattedPrice = formatPrice(sub.price),
                    onClick = { onSubscriptionClick(sub) }
                )
            }
        }
    }
}

@Composable
fun UpcomingItemCard(
    subscription: Subscription,
    formattedPrice: String,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current

    val containerColor = if (appColors.isDark) {
        Color(subscription.colorHex).copy(alpha = 0.22f)
    } else {
        Color(subscription.colorHex).copy(alpha = 0.65f)
    }

    val borderColor = if (appColors.isDark) {
        Color(subscription.colorHex).copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(width = if (appColors.isDark) 1.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top Row: Logo Badge & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Icon Container
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (appColors.isDark) Color(0xFF282A34) else FigmaWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subscription.name.take(2).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = if (appColors.isDark) FigmaWhite else FigmaTextPrimary
                    )
                }

                // Price
                Text(
                    text = formattedPrice,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = appColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Days left badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (appColors.isDark) Color(0xFF22242E) else FigmaWhite.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (subscription.daysUntilRenewal <= 3) FigmaDanger else appColors.textSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${subscription.daysUntilRenewal} days left",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (subscription.daysUntilRenewal <= 3) FigmaDanger else appColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subscription Title
            Text(
                text = subscription.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = appColors.textPrimary,
                maxLines = 1
            )

            // Category
            Text(
                text = subscription.category.displayName,
                fontSize = 12.sp,
                color = appColors.textSecondary,
                maxLines = 1
            )
        }
    }
}
