package com.habitseed.app.ui.screens.habitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.SocialSyncRepository
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val socialSyncRepository: SocialSyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>("habitId") ?: -1L

    private val _habit = MutableStateFlow<HabitEntity?>(null)
    private val _isCompletedToday = MutableStateFlow(false)
    private val _events = MutableSharedFlow<String>()

    val events: SharedFlow<String> = _events.asSharedFlow()
    val habit: StateFlow<HabitEntity?> = _habit.asStateFlow()
    val isCompletedToday: StateFlow<Boolean> = _isCompletedToday.asStateFlow()

    private val recentLogs = habitRepository.getLogsForHabit(habitId)
        .map { logs ->
            logs.sortedByDescending { it.completedAt ?: 0L }.take(7)
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<HabitDetailUiState> = combine(
        habit,
        isCompletedToday,
        recentLogs
    ) { currentHabit, completedToday, logs ->
        val completionRate = if (logs.isEmpty()) {
            0
        } else {
            ((logs.count { it.isCompleted } * 100f) / logs.size).toInt()
        }
        HabitDetailUiState(
            habit = currentHabit,
            isCompletedToday = completedToday,
            recentLogs = logs,
            completionRate = completionRate
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = HabitDetailUiState()
    )

    init {
        loadHabit()
        checkCompletionStatus()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            _habit.value = habitRepository.getHabitById(habitId)
        }
    }

    private fun checkCompletionStatus() {
        viewModelScope.launch {
            val startOfDay = DateUtils.getStartOfDay(System.currentTimeMillis())
            val endOfDay = DateUtils.getEndOfDay(System.currentTimeMillis())
            val logs = habitRepository.getLogsForDateRange(startOfDay, endOfDay).firstOrNull()
            _isCompletedToday.value = logs?.any { it.habitId == habitId && it.isCompleted } == true
        }
    }

    fun completeHabit() {
        if (_isCompletedToday.value) return

        viewModelScope.launch {
            val wasCompleted = habitRepository.completeHabit(
                habitId = habitId,
                dateKey = DateUtils.todayDateKey()
            )
            if (wasCompleted) {
                _habit.value = habitRepository.getHabitById(habitId)
                _isCompletedToday.value = true
                viewModelScope.launch {
                    socialSyncRepository.syncPublicProfile(PublicProfileSyncReason.HABIT_COMPLETED)
                }
                _events.emit("Habit watered. +10 drops earned.")
            }
        }
    }
}

data class HabitDetailUiState(
    val habit: HabitEntity? = null,
    val isCompletedToday: Boolean = false,
    val recentLogs: List<HabitLogEntity> = emptyList(),
    val completionRate: Int = 0
)
