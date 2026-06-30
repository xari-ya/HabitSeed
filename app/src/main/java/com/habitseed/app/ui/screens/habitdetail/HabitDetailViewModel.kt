package com.habitseed.app.ui.screens.habitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.SocialSyncRepository
import com.habitseed.app.domain.gamification.PlantHealthCalculator
import com.habitseed.app.domain.gamification.PlantHealthInfo
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.domain.util.DateUtils
import com.habitseed.app.notifications.HabitSeedNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository,
    private val socialSyncRepository: SocialSyncRepository,
    private val notifier: HabitSeedNotifier,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>("habitId") ?: -1L

    private val _habit = MutableStateFlow<HabitEntity?>(null)
    private val _isCompletedToday = MutableStateFlow(false)
    private val _events = MutableSharedFlow<String>()

    val events: SharedFlow<String> = _events.asSharedFlow()
    val habit: StateFlow<HabitEntity?> = _habit.asStateFlow()
    val isCompletedToday: StateFlow<Boolean> = _isCompletedToday.asStateFlow()

    private val recentLogs = habitRepository.getRecentLogsForHabit(habitId, limit = 30)
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
        val todayDateKey = DateUtils.todayDateKey()
        val lastCompletedDateKey = logs
            .firstOrNull { it.isCompleted }
            ?.dateKey
        val completionRate = if (logs.isEmpty()) {
            0
        } else {
            ((logs.count { it.isCompleted } * 100f) / logs.size).toInt()
        }
        HabitDetailUiState(
            habit = currentHabit,
            isCompletedToday = completedToday,
            plantHealthInfo = PlantHealthCalculator.healthFor(
                isCompletedToday = completedToday,
                lastCompletedDateKey = lastCompletedDateKey,
                todayDateKey = todayDateKey
            ),
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
            _isCompletedToday.value = habitRepository.isHabitCompletedOnDate(
                habitId = habitId,
                dateKey = DateUtils.todayDateKey()
            )
        }
    }

    fun completeHabit() {
        if (_isCompletedToday.value) return

        viewModelScope.launch {
            val result = habitRepository.completeHabit(
                habitId = habitId,
                dateKey = DateUtils.todayDateKey()
            )
            if (result.completed) {
                _habit.value = habitRepository.getHabitById(habitId)
                _isCompletedToday.value = true
                viewModelScope.launch {
                    socialSyncRepository.syncPublicProfile(PublicProfileSyncReason.HABIT_COMPLETED)
                }
                result.messages.forEach { message ->
                    if (message.isMilestoneReward() && notificationsEnabled()) {
                        notifier.showReward(
                            title = "HabitSeed reward",
                            message = message
                        )
                    }
                    _events.emit(message)
                }
            }
        }
    }

    private fun String.isMilestoneReward(): Boolean {
        return startsWith("Your plant reached") ||
            startsWith("Fully grown plant") ||
            startsWith("Perfect day")
    }

    private suspend fun notificationsEnabled(): Boolean {
        return userRepository.getSettings().first()?.notificationsEnabled == true
    }
}

data class HabitDetailUiState(
    val habit: HabitEntity? = null,
    val isCompletedToday: Boolean = false,
    val plantHealthInfo: PlantHealthInfo = PlantHealthCalculator.healthFor(
        isCompletedToday = false,
        lastCompletedDateKey = null,
        todayDateKey = DateUtils.todayDateKey()
    ),
    val recentLogs: List<HabitLogEntity> = emptyList(),
    val completionRate: Int = 0
)
