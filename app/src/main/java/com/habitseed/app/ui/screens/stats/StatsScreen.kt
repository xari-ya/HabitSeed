package com.habitseed.app.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitseed.app.domain.model.DailyCompletionStat
import com.habitseed.app.ui.theme.HabitSeedDimens
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = HabitSeedDimens.ScreenPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Your Harvest",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Track how consistently your garden has grown over the last month.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                WeekStrip(
                    dates = uiState.currentWeek,
                    highlight = LocalDate.now()
                )
            }

            item {
                StatsChartCard(
                    stats = uiState.dailyStats,
                    averageDailyCompletions = uiState.averageDailyCompletions
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HarvestStatCard(
                        label = "Garden Level",
                        value = "Level ${uiState.gardenLevelInfo.level}",
                        modifier = Modifier.weight(1f)
                    )
                    HarvestStatCard(
                        label = "XP Progress",
                        value = gardenXpText(uiState.gardenLevelInfo.currentXp, uiState.gardenLevelInfo.nextLevelXp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HarvestStatCard(
                        label = "Current Streak",
                        value = "${uiState.currentStreak} Days",
                        modifier = Modifier.weight(1f)
                    )
                    HarvestStatCard(
                        label = "Plants Fully Grown",
                        value = uiState.plantsFullyGrown.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HarvestStatCard(
                        label = "Best Streak",
                        value = "${uiState.bestStreak} Days",
                        modifier = Modifier.weight(1f)
                    )
                    HarvestStatCard(
                        label = "30-Day Consistency",
                        value = "${uiState.monthlyConsistencyPercent}%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HarvestStatCard(
                        label = "Lifetime Drops",
                        value = uiState.lifetimeDropsEarned.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    HarvestStatCard(
                        label = "Garden Title",
                        value = uiState.gardenLevelInfo.title,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(
    dates: List<LocalDate>,
    highlight: LocalDate
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dates.forEach { date ->
            val isToday = date == highlight
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(42.dp)
                        .background(
                            color = if (isToday) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (isToday) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun gardenXpText(currentXp: Int, nextLevelXp: Int?): String {
    return nextLevelXp?.let { "$currentXp / $it" } ?: "$currentXp XP"
}

@Composable
private fun StatsChartCard(
    stats: List<DailyCompletionStat>,
    averageDailyCompletions: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Text(
                text = "Last 30 days",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Average $averageDailyCompletions completions per day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 18.dp)
            ) {
                CompletionLineChart(stats = stats)
            }
        }
    }
}

@Composable
private fun CompletionLineChart(stats: List<DailyCompletionStat>) {
    if (stats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No harvest data yet",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val maxValue = (stats.maxOfOrNull { it.completionCount } ?: 0).coerceAtLeast(1)
    val chartBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val chartFillTop = MaterialTheme.colorScheme.secondary.copy(alpha = 0.32f)
    val chartFillBottom = MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
    val chartLine = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val padding = 22.dp.toPx()
        val width = size.width - (padding * 2)
        val height = size.height - (padding * 2)
        val stepX = if (stats.size == 1) 0f else width / (stats.lastIndex.coerceAtLeast(1))

        drawRoundRect(
            color = chartBackground,
            topLeft = Offset(padding, padding),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
        )

        val linePath = Path()
        val fillPath = Path()

        stats.forEachIndexed { index, stat ->
            val x = padding + (index * stepX)
            val y = padding + height - ((stat.completionCount / maxValue.toFloat()) * height)
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, padding + height)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        val finalX = padding + (stats.lastIndex * stepX)
        fillPath.lineTo(finalX, padding + height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    chartFillTop,
                    chartFillBottom
                )
            )
        )

        drawPath(
            path = linePath,
            color = chartLine,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun HarvestStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
