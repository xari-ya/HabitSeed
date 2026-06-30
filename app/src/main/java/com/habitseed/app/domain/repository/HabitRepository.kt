package com.habitseed.app.domain.repository

import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.model.TodayHabitStatus
import com.habitseed.app.domain.model.DailyCompletionStat
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<HabitEntity>>
    fun getTodayHabitsWithCompletionStatus(dateKey: String): Flow<List<TodayHabitStatus>>
    suspend fun getHabitById(id: Long): HabitEntity?
    suspend fun insertHabit(habit: HabitEntity): Long
    suspend fun updateHabit(habit: HabitEntity)
    suspend fun archiveHabit(habitId: Long)
    
    fun getRecentLogsForHabit(habitId: Long, limit: Int = 7): Flow<List<HabitLogEntity>>
    suspend fun completeHabit(habitId: Long, dateKey: String = com.habitseed.app.domain.util.DateUtils.todayDateKey()): HabitCompletionResult
    suspend fun isHabitCompletedOnDate(habitId: Long, dateKey: String): Boolean
    fun getStatsForLast30Days(): Flow<List<DailyCompletionStat>>
}

data class HabitCompletionResult(
    val completed: Boolean,
    val alreadyCompleted: Boolean = false,
    val waterDropsAwarded: Int = 0,
    val gardenXpAwarded: Int = 0,
    val oldStage: Int = 0,
    val newStage: Int = 0,
    val messages: List<String> = emptyList()
)
