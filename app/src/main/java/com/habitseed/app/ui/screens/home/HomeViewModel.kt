package com.habitseed.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.model.TodayHabitStatus
import com.habitseed.app.domain.gamification.GardenLevelCalculator
import com.habitseed.app.domain.gamification.GardenLevelInfo
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.domain.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    userRepository: UserRepository,
    habitRepository: HabitRepository
) : ViewModel() {

    private val user: StateFlow<UserEntity?> = userRepository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val todayHabits: StateFlow<List<TodayHabitStatus>> =
        habitRepository.getTodayHabitsWithCompletionStatus(DateUtils.todayDateKey())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val uiState: StateFlow<HomeUiState> = combine(user, todayHabits) { currentUser, habits ->
        val completedCount = habits.count { it.isCompletedToday }
        val totalCount = habits.size
        HomeUiState(
            user = currentUser,
            todayHabits = habits,
            completedToday = completedCount,
            scheduledToday = totalCount,
            gardenStreak = currentUser?.currentStreak ?: 0,
            gardenLevelInfo = GardenLevelCalculator.levelForXp(currentUser?.gardenXp ?: 0),
            progressPercent = if (totalCount == 0) 0 else (completedCount * 100) / totalCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}

data class HomeUiState(
    val user: UserEntity? = null,
    val todayHabits: List<TodayHabitStatus> = emptyList(),
    val completedToday: Int = 0,
    val scheduledToday: Int = 0,
    val gardenStreak: Int = 0,
    val gardenLevelInfo: GardenLevelInfo = GardenLevelCalculator.levelForXp(0),
    val progressPercent: Int = 0
)
