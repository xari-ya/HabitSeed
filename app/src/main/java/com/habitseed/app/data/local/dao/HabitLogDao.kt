package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.model.DailyCompletionCount
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY completedAt DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: HabitLogEntity): Long
    
    @Query("SELECT * FROM habit_logs WHERE completedAt >= :startOfDay AND completedAt <= :endOfDay")
    fun getLogsForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateKey = :dateKey LIMIT 1")
    suspend fun getLogForHabitAndDate(habitId: Long, dateKey: String): HabitLogEntity?

    @Query(
        """
        SELECT dateKey
        FROM habit_logs
        WHERE habitId = :habitId AND status = 'COMPLETED' AND dateKey < :beforeDateKey
        ORDER BY dateKey DESC
        LIMIT 1
        """
    )
    suspend fun getLatestCompletedDateKeyBefore(habitId: Long, beforeDateKey: String): String?

    @Query(
        """
        SELECT dateKey AS dateKey, COUNT(*) AS completionCount
        FROM habit_logs
        WHERE status = 'COMPLETED' AND dateKey >= :fromDateKey AND dateKey <= :toDateKey
        GROUP BY dateKey
        ORDER BY dateKey ASC
        """
    )
    fun getDailyCompletionCounts(fromDateKey: String, toDateKey: String): Flow<List<DailyCompletionCount>>
}
