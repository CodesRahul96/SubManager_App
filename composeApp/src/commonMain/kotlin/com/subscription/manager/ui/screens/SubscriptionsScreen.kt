package com.subscription.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscription.manager.model.SubscriptionCategory
import com.subscription.manager.theme.*
import com.subscription.manager.ui.components.AppTextFieldDefaults
import com.subscription.manager.ui.components.SubscriptionCard
import com.subscription.manager.viewmodel.SortOption
import com.subscription.manager.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()
    val filteredSubs by viewModel.filteredSubscriptions.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Screen Title & Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Subscriptions",
                    style = FigmaTypography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                val currentUser by viewModel.currentUser.collectAsState()
                val isPro = currentUser?.isPro == true
                val maxLimit = if (isPro) com.subscription.manager.util.SecurityValidator.MAX_SUBSCRIPTIONS_PRO else com.subscription.manager.util.SecurityValidator.MAX_SUBSCRIPTIONS_BASIC

                Text(
                    text = "${filteredSubs.size}/$maxLimit services • ${if (isPro) "Pro" else "Basic"}",
                    style = FigmaTypography.bodySmall,
                    color = appColors.textSecondary
                )
            }

            // Add button
            Button(
                onClick = { viewModel.openAddDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = FigmaOrange),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = FigmaWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add New", fontWeight = FontWeight.Bold, color = FigmaWhite, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by name, tag or service...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary)
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = AppTextFieldDefaults.colors(
                appColors = appColors,
                containerColor = if (appColors.isDark) Color(0xFF14151B) else appColors.cardBackground
            ),
            textStyle = AppTextFieldDefaults.textStyle(appColors),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Tabs & Sort Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Pills
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "ALL" to "All",
                    "ACTIVE" to "Active",
                    "PAUSED" to "Paused",
                    "UPCOMING" to "Soon"
                ).forEach { (key, label) ->
                    val isSelected = uiState.filterStatus == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF181A22) else appColors.cardBackground)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF282B36) else appColors.border),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setFilterStatus(key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) FigmaWhite else (if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary)
                        )
                    }
                }
            }

            // Sort Dropdown
            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (appColors.isDark) Color(0xFF181A22) else appColors.cardBackground)
                        .border(1.dp, if (appColors.isDark) Color(0xFF282B36) else appColors.border, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = if (appColors.isDark) FigmaWhite else appColors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(if (appColors.isDark) Color(0xFF1C1E26) else FigmaWhite)
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.displayName,
                                    color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                                )
                            },
                            onClick = {
                                viewModel.setSortBy(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text("All Categories", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FigmaOrange,
                        selectedLabelColor = FigmaWhite,
                        containerColor = appColors.cardBackground,
                        labelColor = appColors.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = appColors.border,
                        selectedBorderColor = FigmaOrange,
                        enabled = true,
                        selected = uiState.selectedCategory == null
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            items(SubscriptionCategory.entries) { category ->
                val isSelected = uiState.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FigmaOrange,
                        selectedLabelColor = FigmaWhite,
                        containerColor = appColors.cardBackground,
                        labelColor = appColors.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = appColors.border,
                        selectedBorderColor = FigmaOrange,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subscriptions List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredSubs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No subscriptions found",
                                style = FigmaTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try modifying your filter or tap below to add a service.",
                                style = FigmaTypography.bodySmall,
                                color = appColors.textSecondary
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
                items(filteredSubs) { sub ->
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
