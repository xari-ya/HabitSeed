package com.habitseed.app.data.repository

import com.habitseed.app.data.local.dao.HabitDao
import com.habitseed.app.data.local.dao.HabitLogDao
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.model.DailyCompletionCount
import com.habitseed.app.data.local.model.TodayHabitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitRepositoryImplTest {
    @Test
    fun completeHabit_duplicateSameDate_awardsNothing() = runBlocking {
        val dateKey = "2026-06-30"
        val fakes = completionFakes(
            habits = listOf(HabitEntity(id = 1L, name = "Water plants")),
            logs = listOf(HabitLogEntity(habitId = 1L, dateKey = dateKey, status = "COMPLETED"))
        )

        val result = runHabitCompletionTransaction(
            habitId = 1L,
            dateKey = dateKey,
            now = 100L,
            habitDao = fakes.habitDao,
            habitLogDao = fakes.habitLogDao,
            userDao = fakes.userDao
        )

        assertFalse(result.completed)
        assertTrue(result.alreadyCompleted)
        assertEquals(0, result.waterDropsAwarded)
        assertEquals(0, result.gardenXpAwarded)
        assertEquals(0, fakes.userDao.waterDropsAwarded)
        assertEquals(0, fakes.userDao.gardenXpAwarded)
        assertEquals(1, fakes.habitLogDao.logs.size)
    }

    @Test
    fun completeHabit_stageUp_awardsStageBonusOnce() = runBlocking {
        val dateKey = "2026-06-30"
        val fakes = completionFakes(
            user = UserEntity(name = "Gardener", lastPerfectDayBonusDateKey = dateKey),
            habits = listOf(HabitEntity(id = 1L, name = "Water plants", totalCompletions = 0))
        )

        val result = runHabitCompletionTransaction(
            habitId = 1L,
            dateKey = dateKey,
            now = 100L,
            habitDao = fakes.habitDao,
            habitLogDao = fakes.habitLogDao,
            userDao = fakes.userDao
        )

        assertTrue(result.completed)
        assertEquals(0, result.oldStage)
        assertEquals(1, result.newStage)
        assertEquals(30, result.waterDropsAwarded)
        assertEquals(35, result.gardenXpAwarded)
        assertEquals(1, fakes.habitDao.habits.getValue(1L).totalCompletions)
        assertEquals(1, fakes.habitDao.habits.getValue(1L).plantGrowthLevel)
        assertTrue(result.messages.any { it.contains("Sprout") })
    }

    @Test
    fun completeHabit_reachesFullyGrown_awardsFullyGrownBonusOnce() = runBlocking {
        val dateKey = "2026-06-30"
        val fakes = completionFakes(
            user = UserEntity(name = "Gardener", lastPerfectDayBonusDateKey = dateKey),
            habits = listOf(
                HabitEntity(
                    id = 1L,
                    name = "Water plants",
                    totalCompletions = 29,
                    plantGrowthLevel = 4
                )
            )
        )

        val result = runHabitCompletionTransaction(
            habitId = 1L,
            dateKey = dateKey,
            now = 100L,
            habitDao = fakes.habitDao,
            habitLogDao = fakes.habitLogDao,
            userDao = fakes.userDao
        )

        assertTrue(result.completed)
        assertEquals(4, result.oldStage)
        assertEquals(5, result.newStage)
        assertEquals(130, result.waterDropsAwarded)
        assertEquals(135, result.gardenXpAwarded)
        assertTrue(result.messages.any { it.contains("Fully grown") })
    }

    @Test
    fun completeHabit_perfectDay_awardsPerfectDayOncePerDate() = runBlocking {
        val dateKey = "2026-06-30"
        val fakes = completionFakes(
            habits = listOf(
                HabitEntity(id = 1L, name = "Morning stretch", totalCompletions = 30, plantGrowthLevel = 5)
            )
        )

        val firstResult = runHabitCompletionTransaction(
            habitId = 1L,
            dateKey = dateKey,
            now = 100L,
            habitDao = fakes.habitDao,
            habitLogDao = fakes.habitLogDao,
            userDao = fakes.userDao
        )

        assertEquals(35, firstResult.waterDropsAwarded)
        assertEquals(30, firstResult.gardenXpAwarded)
        assertTrue(firstResult.messages.any { it.contains("Perfect day") })
        assertEquals(dateKey, fakes.userDao.currentUser?.lastPerfectDayBonusDateKey)

        fakes.habitDao.habits[2L] = HabitEntity(
            id = 2L,
            name = "Evening journal",
            totalCompletions = 30,
            plantGrowthLevel = 5
        )

        val secondResult = runHabitCompletionTransaction(
            habitId = 2L,
            dateKey = dateKey,
            now = 200L,
            habitDao = fakes.habitDao,
            habitLogDao = fakes.habitLogDao,
            userDao = fakes.userDao
        )

        assertEquals(10, secondResult.waterDropsAwarded)
        assertEquals(10, secondResult.gardenXpAwarded)
        assertFalse(secondResult.messages.any { it.contains("Perfect day") })
    }

    private fun completionFakes(
        user: UserEntity = UserEntity(name = "Gardener"),
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity> = emptyList()
    ): CompletionFakes {
        val logDao = FakeHabitLogDao(logs)
        val habitDao = FakeHabitDao(habits)
        habitDao.completedCountProvider = { userId, dateKey ->
            val activeHabitIds = habitDao.habits.values
                .filter { it.userId == userId && !it.isArchived }
                .map { it.id }
                .toSet()
            logDao.logs.count { log ->
                log.habitId in activeHabitIds &&
                    log.dateKey == dateKey &&
                    log.status == "COMPLETED"
            }
        }
        return CompletionFakes(
            habitDao = habitDao,
            habitLogDao = logDao,
            userDao = FakeCompletionUserDao(user)
        )
    }
}

private data class CompletionFakes(
    val habitDao: FakeHabitDao,
    val habitLogDao: FakeHabitLogDao,
    val userDao: FakeCompletionUserDao
)

private class FakeHabitDao(habits: List<HabitEntity>) : HabitDao {
    val habits = habits.associateBy { it.id }.toMutableMap()
    var completedCountProvider: (String, String) -> Int = { _, _ -> 0 }

    override fun getAllHabits(userId: String): Flow<List<HabitEntity>> {
        return flowOf(habits.values.filter { it.userId == userId && !it.isArchived })
    }

    override suspend fun getAllHabitsSync(userId: String): List<HabitEntity> {
        return habits.values.filter { it.userId == userId }
    }

    override suspend fun getHabitById(id: Long): HabitEntity? = habits[id]

    override suspend fun insertHabit(habit: HabitEntity): Long {
        val id = habit.id.takeIf { it != 0L } ?: ((habits.keys.maxOrNull() ?: 0L) + 1L)
        habits[id] = habit.copy(id = id)
        return id
    }

    override suspend fun updateHabit(habit: HabitEntity) {
        habits[habit.id] = habit
    }

    override suspend fun archiveHabit(habitId: Long, updatedAt: Long) {
        habits[habitId] = habits.getValue(habitId).copy(isArchived = true, updatedAt = updatedAt)
    }

    override fun observeTodayHabitsWithCompletionStatus(
        userId: String,
        dateKey: String
    ): Flow<List<TodayHabitStatus>> = flowOf(emptyList())

    override suspend fun countScheduledHabitsForDate(userId: String, dateKey: String): Int {
        return habits.values.count { it.userId == userId && !it.isArchived }
    }

    override suspend fun countCompletedScheduledHabitsForDate(userId: String, dateKey: String): Int {
        return completedCountProvider(userId, dateKey)
    }
}

private class FakeHabitLogDao(logs: List<HabitLogEntity>) : HabitLogDao {
    val logs = logs.toMutableList()

    override fun getRecentLogsForHabit(habitId: Long, limit: Int): Flow<List<HabitLogEntity>> {
        return flowOf(logs.filter { it.habitId == habitId }.take(limit))
    }

    override suspend fun getAllLogsSync(): List<HabitLogEntity> = logs

    override suspend fun insertLog(log: HabitLogEntity): Long {
        if (logs.any { it.habitId == log.habitId && it.dateKey == log.dateKey }) return -1L
        val id = (logs.maxOfOrNull { it.id } ?: 0L) + 1L
        logs += log.copy(id = id)
        return id
    }

    override suspend fun getLogForHabitAndDate(habitId: Long, dateKey: String): HabitLogEntity? {
        return logs.firstOrNull { it.habitId == habitId && it.dateKey == dateKey }
    }

    override suspend fun isHabitCompletedOnDate(habitId: Long, dateKey: String): Boolean {
        return logs.any { it.habitId == habitId && it.dateKey == dateKey && it.status == "COMPLETED" }
    }

    override suspend fun getLatestCompletedDateKeyBefore(habitId: Long, beforeDateKey: String): String? {
        return logs
            .filter { it.habitId == habitId && it.status == "COMPLETED" && it.dateKey < beforeDateKey }
            .maxOfOrNull { it.dateKey }
    }

    override fun getDailyCompletionCounts(
        fromDateKey: String,
        toDateKey: String
    ): Flow<List<DailyCompletionCount>> = flowOf(emptyList())
}

private class FakeCompletionUserDao(initialUser: UserEntity?) : UserDao {
    private val userFlow = MutableStateFlow(initialUser)
    var currentUser: UserEntity? = initialUser
        private set
    var waterDropsAwarded: Int = 0
        private set
    var gardenXpAwarded: Int = 0
        private set

    override fun getUser(): Flow<UserEntity?> = userFlow

    override fun observeCurrentUser(): Flow<UserEntity?> = userFlow

    override suspend fun getUserById(userId: String): UserEntity? = currentUser?.takeIf { it.id == userId }

    override suspend fun insertUser(user: UserEntity) {
        currentUser = user
        userFlow.value = user
    }

    override suspend fun updateUser(user: UserEntity) {
        currentUser = user
        userFlow.value = user
    }

    override suspend fun addWaterDrops(userId: String, amount: Int, updatedAt: Long): Int {
        val user = currentUser ?: return 0
        if (user.id != userId || user.waterDrops + amount < 0) return 0
        currentUser = user.copy(waterDrops = user.waterDrops + amount, updatedAt = updatedAt)
        userFlow.value = currentUser
        return 1
    }

    override suspend fun addGardenXp(userId: String, amount: Int, updatedAt: Long): Int {
        val user = currentUser ?: return 0
        if (user.id != userId || amount < 0) return 0
        gardenXpAwarded += amount
        currentUser = user.copy(gardenXp = user.gardenXp + amount, updatedAt = updatedAt)
        userFlow.value = currentUser
        return 1
    }

    override suspend fun addWaterDropsAndLifetime(userId: String, drops: Int, updatedAt: Long): Int {
        val user = currentUser ?: return 0
        if (user.id != userId || drops < 0) return 0
        waterDropsAwarded += drops
        currentUser = user.copy(
            waterDrops = user.waterDrops + drops,
            lifetimeDropsEarned = user.lifetimeDropsEarned + drops,
            updatedAt = updatedAt
        )
        userFlow.value = currentUser
        return 1
    }

    override suspend fun updateLastPerfectDayBonusDate(
        userId: String,
        dateKey: String,
        updatedAt: Long
    ): Int {
        val user = currentUser ?: return 0
        if (user.id != userId) return 0
        currentUser = user.copy(lastPerfectDayBonusDateKey = dateKey, updatedAt = updatedAt)
        userFlow.value = currentUser
        return 1
    }

    override suspend fun markOnboardingComplete(userId: String, updatedAt: Long) = Unit

    override suspend fun updateStreaks(
        userId: String,
        currentStreak: Int,
        bestStreak: Int,
        updatedAt: Long
    ) {
        val user = currentUser ?: return
        if (user.id != userId) return
        currentUser = user.copy(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            updatedAt = updatedAt
        )
        userFlow.value = currentUser
    }

    override suspend fun updateLastCloudSyncAt(
        syncedAt: Long,
        publicProfileSyncHash: String,
        userId: String
    ): Int = 1
}
