package com.habitseed.app.ui.screens.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitseed.app.data.local.model.ShopItemWithStatus
import com.habitseed.app.ui.components.PlantAssetMapper
import com.habitseed.app.ui.feedback.rememberHabitSeedHaptics
import com.habitseed.app.ui.theme.HabitSeedDimens

private enum class StoreFilter(val label: String) {
    All("All"),
    Basic("Basic"),
    Rare("Rare"),
    Epic("Epic")
}

@Composable
fun ShopScreen(
    viewModel: ShopViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val shopItems by viewModel.shopItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFilter by remember { mutableStateOf(StoreFilter.All) }
    val haptics = rememberHabitSeedHaptics()

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            if (message.endsWith("unlocked.")) {
                haptics.success()
            } else {
                haptics.warning()
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    val filteredItems = remember(shopItems, selectedFilter) {
        shopItems.filter { item ->
            when (selectedFilter) {
                StoreFilter.All -> true
                StoreFilter.Basic -> item.rarity.equals("basic", ignoreCase = true)
                StoreFilter.Rare -> item.rarity.equals("rare", ignoreCase = true)
                StoreFilter.Epic -> item.rarity.equals("epic", ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = HabitSeedDimens.ScreenPadding),
            contentPadding = PaddingValues(vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(
                key = "store_header",
                span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }
            ) {
                StoreHeader(waterDrops = user?.waterDrops ?: 0)
            }

            item(
                key = "store_filters",
                span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StoreFilter.entries.forEach { filter ->
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = selectedFilter == filter,
                            onClick = {
                                haptics.selection()
                                selectedFilter = filter
                            },
                            label = {
                                Text(
                                    text = filter.label,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            items(filteredItems, key = { it.item.id }) { item ->
                StorePlantTile(
                    item = item,
                    canAfford = (user?.waterDrops ?: 0) >= item.item.priceDrops,
                    onPurchase = { viewModel.purchaseItem(item) }
                )
            }
        }
    }
}

@Composable
private fun StoreHeader(waterDrops: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFlorist,
                        contentDescription = "Seed Store",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Seed Store",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(999.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = "Available drops",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = waterDrops.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Text(
            text = "Unlock new plants for your garden and spend the drops you've earned.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StorePlantTile(
    item: ShopItemWithStatus,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    val rarityUi = rarityUi(item.rarity)
    val plantKey = item.item.linkedPlantTypeId ?: item.item.assetName

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(304.dp),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = rarityUi.background,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(rarityUi.dot, CircleShape)
                        )
                        Text(
                            text = rarityUi.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = rarityUi.text,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = PlantAssetMapper.imageFor(
                            plantTypeId = plantKey,
                            growthStage = 5
                        )
                    ),
                    contentDescription = item.item.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.item.name,
                    style = MaterialTheme.typography.titleMedium.copy(lineHeight = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = item.item.priceDrops.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            if (item.isPurchased) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Owned",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = canAfford,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = if (canAfford) "Unlock" else "Need more drops",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private data class RarityUi(
    val label: String,
    val background: Color,
    val text: Color,
    val dot: Color
)

@Composable
private fun rarityUi(rarity: String): RarityUi {
    return when (rarity.lowercase()) {
        "epic" -> RarityUi(
            label = "EPIC",
            background = MaterialTheme.colorScheme.secondary,
            text = MaterialTheme.colorScheme.onSecondary,
            dot = MaterialTheme.colorScheme.onSecondary
        )
        "rare" -> RarityUi(
            label = "RARE",
            background = MaterialTheme.colorScheme.primary,
            text = MaterialTheme.colorScheme.onPrimary,
            dot = MaterialTheme.colorScheme.onPrimary
        )
        else -> RarityUi(
            label = "BASIC",
            background = MaterialTheme.colorScheme.surfaceVariant,
            text = MaterialTheme.colorScheme.onSurface,
            dot = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
