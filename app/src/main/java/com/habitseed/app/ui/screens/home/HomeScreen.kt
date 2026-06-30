package com.habitseed.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.habitseed.app.data.local.model.TodayHabitStatus
import com.habitseed.app.domain.gamification.GardenLevelInfo
import com.habitseed.app.domain.gamification.PlantGrowthCalculator
import com.habitseed.app.domain.gamification.PlantHealthCalculator
import com.habitseed.app.domain.util.DateUtils
import com.habitseed.app.ui.components.PlantVisualizer
import com.habitseed.app.ui.feedback.rememberHabitSeedHaptics
import com.habitseed.app.ui.screens.addhabit.AddHabitSheet
import com.habitseed.app.ui.theme.HabitSeedDimens
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHabitDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddHabitSheet by remember { mutableStateOf(false) }
    val heroPlant = uiState.todayHabits.firstOrNull()?.habit
    val haptics = rememberHabitSeedHaptics()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptics.selection()
                    showAddHabitSheet = true
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = HabitSeedDimens.ScreenPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GreetingHeader(
                    name = firstNameOrFallback(uiState.user?.name),
                    waterDrops = uiState.user?.waterDrops ?: 0,
                    avatarUrl = uiState.user?.avatarUrl
                )
            }

            item {
                DashboardHeroCard(
                    progressPercent = uiState.progressPercent,
                    completedToday = uiState.completedToday,
                    scheduledToday = uiState.scheduledToday,
                    gardenStreak = uiState.gardenStreak,
                    gardenLevelInfo = uiState.gardenLevelInfo,
                    plantTypeId = heroPlant?.plantType ?: "sunflower",
                    growthStage = heroPlant?.plantGrowthLevel ?: 0
                )
            }

            item {
                Text(
                    text = "Today's habits",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.todayHabits.isEmpty()) {
                item {
                    EmptyGardenCard()
                }
            } else {
                items(uiState.todayHabits, key = { it.habit.id }) { habitStatus ->
                    TodayHabitRow(
                        habitStatus = habitStatus,
                        onClick = {
                            haptics.selection()
                            onNavigateToHabitDetail(habitStatus.habit.id)
                        }
                    )
                }
            }
        }
    }

    if (showAddHabitSheet) {
        AddHabitSheet(
            onDismiss = { showAddHabitSheet = false }
        )
    }
}

@Composable
private fun GreetingHeader(
    name: String,
    waterDrops: Int,
    avatarUrl: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${greetingForTime()}, $name!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Let's keep your garden thriving today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "$name avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$waterDrops drops",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DashboardHeroCard(
    progressPercent: Int,
    completedToday: Int,
    scheduledToday: Int,
    gardenStreak: Int,
    gardenLevelInfo: GardenLevelInfo,
    plantTypeId: String,
    growthStage: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "$progressPercent% complete",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Your greenhouse",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Level ${gardenLevelInfo.level} · ${gardenLevelInfo.title}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = encouragementText(progressPercent, scheduledToday),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { (gardenLevelInfo.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = gardenXpText(gardenLevelInfo),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completedToday of $scheduledToday habits watered today",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Garden streak: ${dayCountText(gardenStreak)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            PlantVisualizer(
                plantTypeId = plantTypeId,
                growthStage = growthStage,
                modifier = Modifier.size(148.dp)
            )
        }
    }
}

@Composable
private fun EmptyGardenCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlantVisualizer(
                plantTypeId = "sunflower",
                growthStage = 0,
                modifier = Modifier.size(124.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your garden is ready for its first seed.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the orange button to plant a new habit and start growing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TodayHabitRow(
    habitStatus: TodayHabitStatus,
    onClick: () -> Unit
) {
    val habit = habitStatus.habit
    val plantHealth = PlantHealthCalculator.healthFor(
        isCompletedToday = habitStatus.isCompletedToday,
        lastCompletedDateKey = habitStatus.lastCompletedDateKey,
        todayDateKey = DateUtils.todayDateKey()
    )
    val fallbackHabitColor = MaterialTheme.colorScheme.primary
    val habitColor = remember(habit.colorHex, fallbackHabitColor) {
        colorFromHex(habit.colorHex, fallbackHabitColor)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(habitColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForHabit(habit.iconName),
                    contentDescription = habit.title,
                    tint = habitColor
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${plantName(habit.plantType)} · ${PlantGrowthCalculator.labelFor(habit.plantGrowthLevel)} · ${plantHealth.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        text = if (habitStatus.isCompletedToday) "Watered" else "Ready",
                        background = if (habitStatus.isCompletedToday) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (habitStatus.isCompletedToday) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Text(
                        text = "${habit.currentStreak} day streak",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+10 drops",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (habitStatus.isCompletedToday) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = if (habitStatus.isCompletedToday) "Completed today" else "Not completed yet",
                    tint = if (habitStatus.isCompletedToday) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    background: Color,
    contentColor: Color
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun iconForHabit(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "sprout", "grow", "plant" -> Icons.Filled.Spa
        "flower" -> Icons.Filled.LocalFlorist
        "water", "hydrate" -> Icons.Filled.LocalDrink
        "mind", "mindful" -> Icons.Filled.SelfImprovement
        "heart", "health" -> Icons.Filled.Favorite
        "book", "learn" -> Icons.Filled.Book
        "spark", "routine" -> Icons.Filled.AutoAwesome
        else -> Icons.Filled.Spa
    }
}

private fun plantName(plantTypeId: String): String {
    return plantTypeId
        .split("_")
        .joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
}

private fun colorFromHex(colorHex: String, fallback: Color): Color {
    return runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
        .getOrDefault(fallback)
}

private fun greetingForTime(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

private fun firstNameOrFallback(name: String?): String {
    return name
        ?.trim()
        ?.split(" ")
        ?.firstOrNull { it.isNotBlank() }
        ?: "Gardener"
}

private fun encouragementText(progressPercent: Int, scheduledToday: Int): String {
    return when {
        scheduledToday == 0 -> "Start with one small habit and watch your garden come alive."
        progressPercent >= 100 -> "Everything is thriving today. Your habits are in full bloom."
        progressPercent >= 50 -> "You're building steady momentum. A few more drops and you're there."
        else -> "A little progress today will help your garden grow stronger tomorrow."
    }
}

private fun gardenXpText(gardenLevelInfo: GardenLevelInfo): String {
    return gardenLevelInfo.nextLevelXp?.let { nextLevelXp ->
        "${gardenLevelInfo.currentXp} / $nextLevelXp XP"
    } ?: "${gardenLevelInfo.currentXp} XP"
}

private fun dayCountText(days: Int): String {
    val unit = if (days == 1) "day" else "days"
    return "$days $unit"
}
