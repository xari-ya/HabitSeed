package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.model.TodayHabitStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getAllHabits(userId: String = "local_user"): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isArchived = 1, updatedAt = :updatedAt WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long, updatedAt: Long)

    @Query(
        """
        SELECT habits.*, 
               CASE WHEN habit_logs.id IS NOT NULL THEN 1 ELSE 0 END AS isCompletedToday,
               habit_logs.completedAt AS completedAt
        FROM habits
        LEFT JOIN habit_logs 
            ON habits.id = habit_logs.habitId
           AND habit_logs.dateKey = :dateKey
           AND habit_logs.status = 'COMPLETED'
        WHERE habits.userId = :userId AND habits.isArchived = 0
        ORDER BY habits.createdAt DESC
        """
    )
    fun observeTodayHabitsWithCompletionStatus(
        userId: String = "local_user",
        dateKey: String
    ): Flow<List<TodayHabitStatus>>
}
