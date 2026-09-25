package com.subscription.manager.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.subscription.manager.model.*
import com.subscription.manager.theme.*
import kotlin.math.roundToInt

data class PresetBrand(
    val name: String,
    val defaultPrice: Double,
    val category: SubscriptionCategory,
    val colorHex: Long,
    val websiteUrl: String
)

val PopularPresets = listOf(
    PresetBrand("Netflix", 15.49, SubscriptionCategory.ENTERTAINMENT, 0xFFFFCDD2, "https://netflix.com"),
    PresetBrand("Spotify", 11.99, SubscriptionCategory.ENTERTAINMENT, 0xFFC8E6C9, "https://spotify.com"),
    PresetBrand("YouTube Premium", 13.99, SubscriptionCategory.ENTERTAINMENT, 0xFFFFCCBC, "https://youtube.com"),
    PresetBrand("ChatGPT Plus", 20.00, SubscriptionCategory.AI, 0xFFD8E5F8, "https://openai.com"),
    PresetBrand("Claude Pro", 20.00, SubscriptionCategory.AI, 0xFFFFD76F, "https://claude.ai"),
    PresetBrand("Prime Video", 14.99, SubscriptionCategory.ENTERTAINMENT, 0xFFB3E5FC, "https://amazon.com"),
    PresetBrand("Figma", 15.00, SubscriptionCategory.DESIGN, 0xFFFFE0B2, "https://figma.com"),
    PresetBrand("Adobe CC", 54.99, SubscriptionCategory.DESIGN, 0xFFFFE898, "https://adobe.com"),
    PresetBrand("GitHub Copilot", 10.00, SubscriptionCategory.DEV, 0xFFE0E0E0, "https://github.com"),
    PresetBrand("Notion", 10.00, SubscriptionCategory.PRODUCTIVITY, 0xFFF1F1F3, "https://notion.so"),
    PresetBrand("iCloud+", 2.99, SubscriptionCategory.CLOUD, 0xFFE1BEE7, "https://apple.com"),
    PresetBrand("Google One", 1.99, SubscriptionCategory.CLOUD, 0xFFDCEDC8, "https://one.google.com")
)

val ColorOptions = listOf(
    0xFFFF6B4A, // Signature Orange
    0xFF3B82F6, // Slate Blue
    0xFF10B981, // Emerald Green
    0xFFFFD76F, // Warm Gold
    0xFFEC4899, // Rose Pink
    0xFF8B5CF6, // Royal Purple
    0xFF06B6D4, // Cyan Teal
    0xFFFFCDD2, // Pastel Coral
    0xFFC8E6C9, // Pastel Mint
    0xFFD8E5F8, // Pastel Sky
    0xFFFFE0B2  // Pastel Peach
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSubscriptionDialog(
    subscriptionToEdit: Subscription?,
    currentCurrency: Currency,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        name: String,
        description: String,
        price: Double,
        currency: Currency,
        billingCycle: BillingCycle,
        category: SubscriptionCategory,
        daysUntilRenewal: Int,
        nextBillingDateFormatted: String,
        colorHex: Long,
        reminderDaysBefore: Int,
        notes: String,
        websiteUrl: String
    ) -> Unit
) {
    val appColors = LocalAppColors.current

    var name by remember { mutableStateOf(subscriptionToEdit?.name ?: "") }
    val initialPriceStr = subscriptionToEdit?.let { sub ->
        val converted = sub.price * currentCurrency.rateToUsd
        val rounded = kotlin.math.round(converted * 100.0) / 100.0
        if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    } ?: ""
    var priceStr by remember { mutableStateOf(initialPriceStr) }
    var description by remember { mutableStateOf(subscriptionToEdit?.description ?: "") }
    var selectedCycle by remember { mutableStateOf(subscriptionToEdit?.billingCycle ?: BillingCycle.MONTHLY) }
    var selectedCategory by remember { mutableStateOf(subscriptionToEdit?.category ?: SubscriptionCategory.ENTERTAINMENT) }
    var daysUntilRenewalStr by remember { mutableStateOf(subscriptionToEdit?.daysUntilRenewal?.toString() ?: "30") }
    var selectedColor by remember { mutableStateOf(subscriptionToEdit?.colorHex ?: 0xFFFF6B4A) }
    var reminderDays by remember { mutableStateOf(subscriptionToEdit?.reminderDaysBefore ?: 3) }
    var notes by remember { mutableStateOf(subscriptionToEdit?.notes ?: "") }
    var websiteUrl by remember { mutableStateOf(subscriptionToEdit?.websiteUrl ?: "") }
    var showMoreOptions by remember { mutableStateOf(notes.isNotBlank() || websiteUrl.isNotBlank() || description.isNotBlank()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dialogBg = if (appColors.isDark) Color(0xFF181A22) else FigmaWhite
    val dialogBorder = if (appColors.isDark) Color(0xFF282B36) else Color(0xFFE5E7EB)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (appColors.isDark) FigmaWhite else appColors.textPrimary,
        unfocusedTextColor = if (appColors.isDark) FigmaWhite else appColors.textPrimary,
        focusedContainerColor = if (appColors.isDark) Color(0xFF13141A) else Color(0xFFF9FAFB),
        unfocusedContainerColor = if (appColors.isDark) Color(0xFF13141A) else Color(0xFFF9FAFB),
        focusedBorderColor = FigmaOrange,
        unfocusedBorderColor = if (appColors.isDark) Color(0xFF2E313E) else appColors.border,
        focusedLabelColor = FigmaOrange,
        unfocusedLabelColor = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary,
        focusedPlaceholderColor = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted,
        unfocusedPlaceholderColor = if (appColors.isDark) Color(0xFF6B7082) else appColors.textMuted,
        cursorColor = FigmaOrange
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = dialogBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, dialogBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (subscriptionToEdit == null) "New Subscription" else "Edit Subscription",
                            style = FigmaTypography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (appColors.isDark) FigmaWhite else appColors.textPrimary
                        )
                        Text(
                            text = if (subscriptionToEdit == null) "Track recurring payments & renewal alerts" else "Update price, renewal cycle & alerts",
                            style = FigmaTypography.bodySmall,
                            color = if (appColors.isDark) Color(0xFF9EA3B5) else appColors.textSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
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

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = dialogBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Quick Presets (Only when adding a new subscription)
                    if (subscriptionToEdit == null) {
                        Text(
                            text = "Quick Presets",
                            style = FigmaTypography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(PopularPresets) { preset ->
                                val isSelected = name.equals(preset.name, ignoreCase = true)
                                val convertedPrice = preset.defaultPrice * currentCurrency.rateToUsd
                                val priceFormatted = if (convertedPrice >= 100) {
                                    "${currentCurrency.symbol}${convertedPrice.roundToInt()}"
                                } else {
                                    "${currentCurrency.symbol}${kotlin.math.round(convertedPrice * 10.0) / 10.0}"
                                }

                                Surface(
                                    onClick = {
                                        name = preset.name
                                        val rounded = kotlin.math.round(convertedPrice * 100.0) / 100.0
                                        priceStr = if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
                                        selectedCategory = preset.category
                                        selectedColor = preset.colorHex
                                        websiteUrl = preset.websiteUrl
                                        errorMessage = null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF242735) else Color(preset.colorHex).copy(alpha = 0.5f)),
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF333745) else Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) FigmaWhite else Color(preset.colorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = preset.name.take(1).uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1A1C20)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = preset.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) FigmaWhite else (if (appColors.isDark) Color(0xFFE2E4EC) else appColors.textPrimary)
                                            )
                                            Text(
                                                text = "$priceFormatted/mo",
                                                fontSize = 10.sp,
                                                color = if (isSelected) FigmaWhite.copy(alpha = 0.85f) else (if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Service Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Service Name") },
                        placeholder = { Text("e.g. Netflix, Spotify, ChatGPT") },
                        leadingIcon = {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = FigmaOrange)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Price and Currency
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it; errorMessage = null },
                        label = { Text("Price in ${currentCurrency.code} (${currentCurrency.symbol})") },
                        placeholder = { Text("e.g. 19.99") },
                        leadingIcon = {
                            Text(
                                text = currentCurrency.symbol,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = FigmaOrange,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Billing Cycle
                    Text(
                        text = "Billing Cycle",
                        style = FigmaTypography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BillingCycle.entries.forEach { cycle ->
                            val isSelected = selectedCycle == cycle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF222430) else Color(0xFFF3F4F6))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF2F3242) else Color(0xFFE5E7EB)),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCycle = cycle }
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cycle.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FigmaWhite else (if (appColors.isDark) Color(0xFFDCDFEA) else appColors.textPrimary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Category Selector
                    Text(
                        text = "Category",
                        style = FigmaTypography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SubscriptionCategory.entries.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (appColors.isDark) Color(0xFF222430) else Color(0xFFF3F4F6),
                                    labelColor = if (appColors.isDark) Color(0xFFDCDFEA) else appColors.textPrimary,
                                    selectedContainerColor = FigmaOrange,
                                    selectedLabelColor = FigmaWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (appColors.isDark) Color(0xFF2F3242) else Color(0xFFE5E7EB),
                                    selectedBorderColor = FigmaOrange,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Renewal Date & Quick Presets
                    Text(
                        text = "Days Until Next Renewal",
                        style = FigmaTypography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Days Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(7, 14, 30, 90, 365).forEach { presetDays ->
                            val isSelected = daysUntilRenewalStr == presetDays.toString()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) (if (appColors.isDark) Color(0xFF3B2E28) else FigmaOrange.copy(alpha = 0.12f))
                                        else (if (appColors.isDark) Color(0xFF1E202A) else Color(0xFFF3F4F6))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF2D303D) else Color(0xFFE5E7EB)),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { daysUntilRenewalStr = presetDays.toString() }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (presetDays == 365) "1 Year" else "${presetDays}d",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = daysUntilRenewalStr,
                        onValueChange = { daysUntilRenewalStr = it },
                        label = { Text("Exact Days Count") },
                        placeholder = { Text("e.g. 30") },
                        leadingIcon = {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = FigmaOrange)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Card Accent Color
                    Text(
                        text = "Card Color Theme",
                        style = FigmaTypography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ColorOptions) { hex ->
                            val isSelected = selectedColor == hex
                            val color = Color(hex)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) FigmaWhite else (if (appColors.isDark) Color(0xFF333745) else Color(0xFFE5E7EB)),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (hex == 0xFFF1F1F3 || hex == 0xFFFFE898) Color.Black else FigmaWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Reminder Alert Preference
                    Text(
                        text = "Reminder Alert",
                        style = FigmaTypography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            1 to "1d",
                            2 to "2d",
                            3 to "3d",
                            5 to "5d",
                            7 to "7d"
                        ).forEach { (days, label) ->
                            val isSelected = reminderDays == days
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) (if (appColors.isDark) Color(0xFF3B2E28) else FigmaOrange.copy(alpha = 0.12f))
                                        else (if (appColors.isDark) Color(0xFF1E202A) else Color(0xFFF3F4F6))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFF2D303D) else Color(0xFFE5E7EB)),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { reminderDays = days }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FigmaOrange else (if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle More Options (Notes, Website Link, Description)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showMoreOptions = !showMoreOptions }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showMoreOptions) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = FigmaOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showMoreOptions) "Fewer details" else "Add notes or cancellation link (optional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = FigmaOrange
                        )
                    }

                    AnimatedVisibility(visible = showMoreOptions) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = websiteUrl,
                                onValueChange = { websiteUrl = it },
                                label = { Text("Website or Manage URL") },
                                placeholder = { Text("https://...") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Link, contentDescription = null, tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors
                            )

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notes / Account details") },
                                placeholder = { Text("e.g. Family plan, split with friend") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.NoteAlt, contentDescription = null, tint = if (appColors.isDark) Color(0xFFA0A3AF) else appColors.textSecondary)
                                },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = textFieldColors
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FigmaDanger.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FigmaDanger.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = FigmaDanger, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = FigmaDanger,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Sticky Bottom Action Buttons
                HorizontalDivider(color = dialogBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (appColors.isDark) Color(0xFFB0B4C4) else appColors.textSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (appColors.isDark) Color(0xFF333745) else Color(0xFFE5E7EB))
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val parsedPrice = priceStr.toDoubleOrNull()
                            if (name.isBlank()) {
                                errorMessage = "Please enter a service name"
                                return@Button
                            }
                            if (parsedPrice == null || parsedPrice <= 0) {
                                errorMessage = "Please enter a valid price greater than 0"
                                return@Button
                            }
                            val days = daysUntilRenewalStr.toIntOrNull() ?: 30

                            val formattedRenewalDate = when {
                                days == 0 -> "Renews today"
                                days == 1 -> "Renews tomorrow"
                                days < 30 -> "In $days days"
                                days == 30 -> "Next Month"
                                days < 365 -> "In ${days / 30} months"
                                else -> "Next Year"
                            }

                            onSave(
                                subscriptionToEdit?.id,
                                name.trim(),
                                description.trim().ifEmpty { "${selectedCycle.displayName} plan" },
                                parsedPrice,
                                currentCurrency,
                                selectedCycle,
                                selectedCategory,
                                days,
                                formattedRenewalDate,
                                selectedColor,
                                reminderDays,
                                notes.trim(),
                                websiteUrl.trim()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FigmaOrange),
                        modifier = Modifier.weight(1.3f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (subscriptionToEdit == null) "Add Subscription" else "Save Changes",
                            color = FigmaWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
