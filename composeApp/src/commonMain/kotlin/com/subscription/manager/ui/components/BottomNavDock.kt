package com.subscription.manager.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.subscription.manager.theme.*
import com.subscription.manager.viewmodel.AppTab

@Composable
fun BottomNavDock(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    Surface(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .height(68.dp)
            .shadow(
                elevation = if (appColors.isDark) 8.dp else 16.dp,
                shape = RoundedCornerShape(34.dp),
                spotColor = if (appColors.isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color = appColors.dockBorder,
                shape = RoundedCornerShape(34.dp)
            ),
        shape = RoundedCornerShape(34.dp),
        color = appColors.dockBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            NavDockItem(
                icon = Icons.Outlined.Home,
                contentDescription = "Home",
                isSelected = currentTab == AppTab.HOME,
                inactiveColor = appColors.dockInactiveIcon,
                onClick = { onTabSelected(AppTab.HOME) }
            )

            // Subscriptions Tab
            NavDockItem(
                icon = Icons.Outlined.AccountBalanceWallet,
                contentDescription = "Subscriptions",
                isSelected = currentTab == AppTab.SUBSCRIPTIONS,
                inactiveColor = appColors.dockInactiveIcon,
                onClick = { onTabSelected(AppTab.SUBSCRIPTIONS) }
            )

            // Center Quick Add Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(FigmaOrange)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAddClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subscription",
                    tint = FigmaWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Insights Tab
            NavDockItem(
                icon = Icons.Outlined.BarChart,
                contentDescription = "Insights",
                isSelected = currentTab == AppTab.INSIGHTS,
                inactiveColor = appColors.dockInactiveIcon,
                onClick = { onTabSelected(AppTab.INSIGHTS) }
            )

            // Settings Tab
            NavDockItem(
                icon = Icons.Outlined.Settings,
                contentDescription = "Settings",
                isSelected = currentTab == AppTab.SETTINGS,
                inactiveColor = appColors.dockInactiveIcon,
                onClick = { onTabSelected(AppTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavDockItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) FigmaOrange else Color.Transparent,
        animationSpec = tween(durationMillis = 250)
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) FigmaWhite else inactiveColor,
        animationSpec = tween(durationMillis = 250)
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
