package com.habitseed.app.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitseed.app.domain.model.DailyCompletionStat
import com.habitseed.app.ui.theme.ForestGreen
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
                        icon = Icons.Filled.Park,
                        iconTint = Color(0xFF2D6A4F),
                        iconBackground = Color(0xFF2D6A4F).copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                    HarvestStatCard(
                        label = "XP Progress",
                        value = gardenXpText(uiState.gardenLevelInfo.currentXp, uiState.gardenLevelInfo.nextLevelXp),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = Color(0xFF5B8A72),
                        iconBackground = Color(0xFF5B8A72).copy(alpha = 0.12f),
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
                        icon = Icons.Filled.LocalFireDepartment,
                        iconTint = Color(0xFFE8913A),
                        iconBackground = Color(0xFFE8913A).copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                    HarvestStatCard(
                        label = "Plants Fully Grown",
                        value = uiState.plantsFullyGrown.toString(),
                        icon = Icons.Filled.FilterVintage,
                        iconTint = Color(0xFF7BAE8E),
                        iconBackground = Color(0xFF7BAE8E).copy(alpha = 0.12f),
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
                        icon = Icons.Filled.EmojiEvents,
                        iconTint = Color(0xFFD4A843),
                        iconBackground = Color(0xFFD4A843).copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                    HarvestStatCard(
                        label = "30-Day Consistency",
                        value = "${uiState.monthlyConsistencyPercent}%",
                        icon = Icons.Filled.CheckCircle,
                        iconTint = Color(0xFF4C8A68),
                        iconBackground = Color(0xFF4C8A68).copy(alpha = 0.12f),
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
                        icon = Icons.Filled.WaterDrop,
                        iconTint = Color(0xFF4A90B8),
                        iconBackground = Color(0xFF4A90B8).copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    )
                    HarvestStatCard(
                        label = "Garden Title",
                        value = uiState.gardenLevelInfo.title,
                        icon = Icons.Filled.Spa,
                        iconTint = Color(0xFF6B9E7E),
                        iconBackground = Color(0xFF6B9E7E).copy(alpha = 0.12f),
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
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(HabitSeedDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = iconBackground,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
