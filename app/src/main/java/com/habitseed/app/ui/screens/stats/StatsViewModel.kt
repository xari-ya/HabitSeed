package com.habitseed.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.domain.model.DailyCompletionStat
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class StatsViewModel @Inject constructor(
    userRepository: UserRepository,
    habitRepository: HabitRepository
) : ViewModel() {

    private val user = userRepository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val habits = habitRepository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val dailyStats = habitRepository.getStatsForLast30Days()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<StatsUiState> = combine(user, habits, dailyStats) { currentUser, allHabits, stats ->
        val currentWeek = currentWeekDates()
        val plantsFullyGrown = allHabits.count { it.plantGrowthLevel >= 4 }
        val totalCompletions = stats.sumOf { it.completionCount }
        val averageRate = if (stats.isEmpty()) 0 else (totalCompletions / stats.size.toFloat()).toInt()

        StatsUiState(
            user = currentUser,
            habits = allHabits,
            dailyStats = stats,
            currentWeek = currentWeek,
            currentStreak = currentUser?.currentStreak ?: 0,
            plantsFullyGrown = plantsFullyGrown,
            averageDailyCompletions = averageRate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    private fun currentWeekDates(): List<LocalDate> {
        val today = LocalDate.now()
        val start = today.minusDays(today.dayOfWeek.ordinal.toLong())
        return (0L..6L).map { offset -> start.plusDays(offset) }
    }
}

data class StatsUiState(
    val user: UserEntity? = null,
    val habits: List<HabitEntity> = emptyList(),
    val dailyStats: List<DailyCompletionStat> = emptyList(),
    val currentWeek: List<LocalDate> = emptyList(),
    val currentStreak: Int = 0,
    val plantsFullyGrown: Int = 0,
    val averageDailyCompletions: Int = 0
)
