package com.habitseed.app.data.repository

import androidx.room.withTransaction
import com.habitseed.app.data.local.AppDatabase
import com.habitseed.app.data.local.dao.HabitDao
import com.habitseed.app.data.local.dao.HabitLogDao
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.model.TodayHabitStatus
import com.habitseed.app.domain.gamification.PlantGrowthCalculator
import com.habitseed.app.domain.gamification.RewardCalculator
import com.habitseed.app.domain.model.DailyCompletionStat
import com.habitseed.app.domain.gamification.RewardBundle
import com.habitseed.app.domain.repository.HabitCompletionResult
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

    override fun getRecentLogsForHabit(habitId: Long, limit: Int): Flow<List<HabitLogEntity>> {
        return habitLogDao.getRecentLogsForHabit(habitId, limit)
    }

    override suspend fun isHabitCompletedOnDate(habitId: Long, dateKey: String): Boolean {
        return habitLogDao.isHabitCompletedOnDate(habitId, dateKey)
    }

    override suspend fun completeHabit(habitId: Long, dateKey: String): HabitCompletionResult {
        return db.withTransaction {
            completeHabitInTransaction(habitId = habitId, dateKey = dateKey)
        }
    }

    internal suspend fun completeHabitInTransaction(
        habitId: Long,
        dateKey: String,
        now: Long = System.currentTimeMillis()
    ): HabitCompletionResult {
        return runHabitCompletionTransaction(
            habitId = habitId,
            dateKey = dateKey,
            now = now,
            habitDao = habitDao,
            habitLogDao = habitLogDao,
            userDao = userDao
        )
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

internal suspend fun runHabitCompletionTransaction(
    habitId: Long,
    dateKey: String,
    now: Long,
    habitDao: HabitDao,
    habitLogDao: HabitLogDao,
    userDao: UserDao
): HabitCompletionResult {
    val habit = habitDao.getHabitById(habitId)
        ?: return HabitCompletionResult(completed = false)
    val existingLog = habitLogDao.getLogForHabitAndDate(habitId, dateKey)
    val oldStage = PlantGrowthCalculator.stageFor(habit.totalCompletions)
    if (existingLog?.status == "COMPLETED") {
        return HabitCompletionResult(
            completed = false,
            alreadyCompleted = true,
            oldStage = oldStage,
            newStage = oldStage
        )
    }

    val completionReward = RewardCalculator.completionReward()
    val insertId = habitLogDao.insertLog(
        HabitLogEntity(
            habitId = habitId,
            dateKey = dateKey,
            completedAt = now,
            status = "COMPLETED",
            waterDropsAwarded = completionReward.waterDrops
        )
    )
    if (insertId == -1L) {
        return HabitCompletionResult(
            completed = false,
            alreadyCompleted = true,
            oldStage = oldStage,
            newStage = oldStage
        )
    }

    val previousDateKey = habitLogDao.getLatestCompletedDateKeyBefore(habitId, dateKey)
    val newStreak = HabitProgressCalculator.calculateNextStreak(
        lastCompletedDateKey = previousDateKey,
        currentDateKey = dateKey,
        currentStreak = habit.currentStreak
    )
    val newTotalCompletions = habit.totalCompletions + 1
    val newStage = PlantGrowthCalculator.stageFor(newTotalCompletions)
    val updatedHabit = habit.copy(
        currentStreak = newStreak,
        bestStreak = maxOf(habit.bestStreak, newStreak),
        totalCompletions = newTotalCompletions,
        plantGrowthLevel = newStage,
        updatedAt = now
    )
    habitDao.updateHabit(updatedHabit)

    val rewards = mutableListOf(completionReward)
    if (newStage > oldStage) {
        rewards += RewardCalculator.stageUpReward(
            newStage = newStage,
            stageLabel = PlantGrowthCalculator.labelFor(newStage)
        )
    }
    if (newStage == 5 && oldStage < 5) {
        rewards += RewardCalculator.fullyGrownReward()
    }
    val user = userDao.getUserById(habit.userId)
    val scheduledToday = habitDao.countScheduledHabitsForDate(
        userId = habit.userId,
        dateKey = dateKey
    )
    val completedToday = habitDao.countCompletedScheduledHabitsForDate(
        userId = habit.userId,
        dateKey = dateKey
    )
    if (
        user != null &&
        scheduledToday > 0 &&
        completedToday == scheduledToday &&
        user.lastPerfectDayBonusDateKey != dateKey
    ) {
        rewards += RewardCalculator.perfectDayReward()
        userDao.updateLastPerfectDayBonusDate(
            userId = habit.userId,
            dateKey = dateKey,
            updatedAt = now
        )
    }

    val totalReward = rewards.fold(RewardBundle()) { total, reward ->
        RewardBundle(
            waterDrops = total.waterDrops + reward.waterDrops,
            gardenXp = total.gardenXp + reward.gardenXp,
            messages = total.messages + reward.messages
        )
    }
    userDao.addWaterDropsAndLifetime(habit.userId, totalReward.waterDrops, now)
    userDao.addGardenXp(habit.userId, totalReward.gardenXp, now)
    if (user != null) {
        userDao.updateStreaks(
            userId = habit.userId,
            currentStreak = maxOf(user.currentStreak, newStreak),
            bestStreak = maxOf(user.bestStreak, newStreak),
            updatedAt = now
        )
    }
    return HabitCompletionResult(
        completed = true,
        waterDropsAwarded = totalReward.waterDrops,
        gardenXpAwarded = totalReward.gardenXp,
        oldStage = oldStage,
        newStage = newStage,
        messages = totalReward.messages
    )
}
