package com.habitseed.app.data.repository

import androidx.room.withTransaction
import com.habitseed.app.data.local.AppDatabase
import com.habitseed.app.data.local.dao.HabitDao
import com.habitseed.app.data.local.dao.HabitLogDao
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.model.TodayHabitStatus
import com.habitseed.app.domain.model.DailyCompletionStat
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.util.DateUtils
import com.habitseed.app.domain.util.HabitProgressCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val userDao: UserDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<HabitEntity>> {
        return habitDao.getAllHabits()
    }

    override fun getTodayHabitsWithCompletionStatus(dateKey: String): Flow<List<TodayHabitStatus>> {
        return habitDao.observeTodayHabitsWithCompletionStatus(dateKey = dateKey)
    }

    override suspend fun getHabitById(id: Long): HabitEntity? {
        return habitDao.getHabitById(id)
    }

    override suspend fun insertHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    override suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    override suspend fun archiveHabit(habitId: Long) {
        habitDao.archiveHabit(habitId, System.currentTimeMillis())
    }

    override fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> {
        return habitLogDao.getLogsForHabit(habitId)
    }

    override fun getLogsForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<HabitLogEntity>> {
        return habitLogDao.getLogsForDateRange(startOfDay, endOfDay)
    }

    override suspend fun completeHabit(habitId: Long, dateKey: String): Boolean {
        return db.withTransaction {
            val habit = habitDao.getHabitById(habitId) ?: return@withTransaction false
            val existingLog = habitLogDao.getLogForHabitAndDate(habitId, dateKey)
            if (existingLog?.status == "COMPLETED") return@withTransaction false

            val now = System.currentTimeMillis()
            val insertId = habitLogDao.insertLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateKey = dateKey,
                    completedAt = now,
                    status = "COMPLETED",
                    waterDropsAwarded = 10
                )
            )
            if (insertId == -1L) return@withTransaction false

            val previousDateKey = habitLogDao.getLatestCompletedDateKeyBefore(habitId, dateKey)
            val newStreak = HabitProgressCalculator.calculateNextStreak(
                lastCompletedDateKey = previousDateKey,
                currentDateKey = dateKey,
                currentStreak = habit.currentStreak
            )
            val newTotalCompletions = habit.totalCompletions + 1
            val updatedHabit = habit.copy(
                currentStreak = newStreak,
                bestStreak = maxOf(habit.bestStreak, newStreak),
                totalCompletions = newTotalCompletions,
                plantGrowthLevel = HabitProgressCalculator.calculateGrowthLevel(newTotalCompletions),
                updatedAt = now
            )
            habitDao.updateHabit(updatedHabit)

            userDao.addWaterDrops(habit.userId, 10, now)
            val user = userDao.getUserById(habit.userId)
            if (user != null) {
                userDao.updateStreaks(
                    userId = habit.userId,
                    currentStreak = maxOf(user.currentStreak, newStreak),
                    bestStreak = maxOf(user.bestStreak, newStreak),
                    updatedAt = now
                )
            }
            true
        }
    }

    override fun getStatsForLast30Days(): Flow<List<DailyCompletionStat>> {
        val endDateKey = DateUtils.todayDateKey()
        val startDateKey = DateUtils.parseDateKey(endDateKey).minusDays(29).toString()
        return habitLogDao.getDailyCompletionCounts(startDateKey, endDateKey)
            .map { counts ->
                val byDate = counts.associateBy({ it.dateKey }, { it.completionCount })
                (0L..29L).map { offset ->
                    val dateKey = DateUtils.parseDateKey(startDateKey).plusDays(offset).toString()
                    DailyCompletionStat(
                        dateKey = dateKey,
                        completionCount = byDate[dateKey] ?: 0
                    )
                }
            }
    }
}
