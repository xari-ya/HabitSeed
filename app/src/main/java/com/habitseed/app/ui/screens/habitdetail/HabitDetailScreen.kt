package com.habitseed.app.ui.screens.habitdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.domain.gamification.PlantGrowthCalculator
import com.habitseed.app.ui.components.PlantVisualizer
import com.habitseed.app.ui.components.StatCard
import com.habitseed.app.ui.components.SwipeToCompleteSlider
import com.habitseed.app.ui.feedback.rememberHabitSeedHaptics
import com.habitseed.app.ui.theme.ForestGreen
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HabitDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCelebration by remember { mutableStateOf(false) }
    val haptics = rememberHabitSeedHaptics()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            showCelebration = true
            if (message.isMilestoneReward()) {
                haptics.success()
            }
            snackbarHostState.showSnackbar(message)
            showCelebration = false
        }
    }

    val habit = uiState.habit
    if (habit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.48f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ForestGreen, Color(0xFF4C8A68))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = growthLabel(habit.plantGrowthLevel),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = habit.description ?: "Keep showing up and your plant will thank you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showCelebration,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                CelebrationBubble()
                            }
                        }
                        PlantVisualizer(
                            plantTypeId = habit.plantType,
                            growthStage = habit.plantGrowthLevel,
                            modifier = Modifier.size(240.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.52f),
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Health",
                            value = uiState.plantHealthInfo.label,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        StatCard(
                            label = "Streak",
                            value = "${habit.currentStreak}d",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        StatCard(
                            label = "Growth",
                            value = PlantGrowthCalculator.completionsText(habit.totalCompletions),
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HabitGrowthSummary(
                        bestStreak = habit.bestStreak,
                        completionRate = uiState.completionRate,
                        totalCompletions = habit.totalCompletions
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Recent watering",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DailyLogStrip(logs = uiState.recentLogs)
                    Spacer(modifier = Modifier.weight(1f))
                    SwipeToCompleteSlider(
                        onComplete = { viewModel.completeHabit() },
                        text = "Swipe to Water",
                        completedText = "Watered Today",
                        isCompleted = uiState.isCompletedToday,
                        enabled = !uiState.isCompletedToday,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}

private fun String.isMilestoneReward(): Boolean {
    return startsWith("Your plant reached") ||
        startsWith("Fully grown plant") ||
        startsWith("Perfect day")
}

@Composable
private fun HabitGrowthSummary(
    bestStreak: Int,
    completionRate: Int,
    totalCompletions: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Best streak: ${dayCountText(bestStreak)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "30-day consistency: $completionRate%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Total watered: ${wateredCountText(totalCompletions)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyLogStrip(logs: List<HabitLogEntity>) {
    if (logs.isEmpty()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "No watering history yet. Your first swipe will start the streak.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 4.dp)
    ) {
        items(logs, key = { it.id }) { log ->
            val date = com.habitseed.app.domain.util.DateUtils.parseDateKey(log.dateKey)
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            Surface(
                color = if (log.isCompleted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (log.isCompleted) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = null,
                            tint = if (log.isCompleted) {
                                MaterialTheme.colorScheme.onSecondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CelebrationBubble() {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+10 drops · +10 XP",
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun growthLabel(level: Int): String {
    return PlantGrowthCalculator.labelFor(level)
}

private fun dayCountText(days: Int): String {
    val unit = if (days == 1) "day" else "days"
    return "$days $unit"
}

private fun wateredCountText(count: Int): String {
    val unit = if (count == 1) "time" else "times"
    return "$count $unit"
}
