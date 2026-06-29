package com.habitseed.app.ui.screens.habitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>("habitId") ?: -1L

    private val _habit = MutableStateFlow<HabitEntity?>(null)
    val habit: StateFlow<HabitEntity?> = _habit.asStateFlow()

    private val _isCompletedToday = MutableStateFlow(false)
    val isCompletedToday: StateFlow<Boolean> = _isCompletedToday.asStateFlow()

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
            }
        }
    }
}
