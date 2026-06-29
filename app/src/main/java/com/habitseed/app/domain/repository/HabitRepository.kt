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
    
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>
    suspend fun completeHabit(habitId: Long, dateKey: String = com.habitseed.app.domain.util.DateUtils.todayDateKey()): Boolean
    fun getLogsForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<HabitLogEntity>>
    fun getStatsForLast30Days(): Flow<List<DailyCompletionStat>>
}
