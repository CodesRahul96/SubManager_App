package com.subscription.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.theme.*

@Composable
fun TopHeader(
    userName: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    userInitials: String? = null,
    avatarColorHex: String? = null,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    val avatarBgColor = remember(avatarColorHex, appColors.isDark) {
        if (!avatarColorHex.isNullOrBlank()) {
            try {
                Color(avatarColorHex.removePrefix("#").toLong(16) or 0xFF000000L)
            } catch (e: Exception) {
                FigmaOrange
            }
        } else {
            FigmaOrange
        }
    }

    val displayInitials = remember(userName, userInitials) {
        userInitials ?: userName.split(Regex("\\s+"))
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { "U" }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile Info
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayInitials,
                    color = FigmaWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Welcome back,",
                    style = FigmaTypography.bodySmall,
                    color = appColors.textSecondary
                )
                Text(
                    text = userName,
                    style = FigmaTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
            }
        }

        // Action Icons: Search & Notifications
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.border, CircleShape)
                    .clickable(onClick = onSearchClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = appColors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.border, CircleShape)
                    .clickable(onClick = onNotificationsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = appColors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(FigmaOrange)
                    )
                }
            }
        }
    }
}
