package com.habitseed.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habitseed.app.domain.gamification.PlantHealthCalculator
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.domain.util.DateUtils
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class HabitReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            HabitReminderWorkerEntryPoint::class.java
        )
        val userRepository = entryPoint.userRepository()
        val habitRepository = entryPoint.habitRepository()
        val notifier = entryPoint.habitSeedNotifier()

        val settings = userRepository.getSettings().first() ?: return Result.success()
        if (!settings.notificationsEnabled || !notifier.canPostNotifications()) {
            return Result.success()
        }

        val todayDateKey = DateUtils.todayDateKey()
        val todayHabits = habitRepository
            .getTodayHabitsWithCompletionStatus(todayDateKey)
            .first()
        val mode = inputData.getString(KEY_MODE) ?: MODE_GLOBAL

        if (mode == MODE_HABIT) {
            val habitId = inputData.getLong(KEY_HABIT_ID, -1L)
            val status = todayHabits.firstOrNull { it.habit.id == habitId }
                ?: return Result.success()
            if (!status.habit.reminderEnabled || status.isCompletedToday) {
                return Result.success()
            }
            notifier.showHabitReminder(status.habit)
            notifyIfPlantNeedsCare(
                notifier = notifier,
                habit = status.habit,
                isCompletedToday = status.isCompletedToday,
                lastCompletedDateKey = status.lastCompletedDateKey,
                todayDateKey = todayDateKey
            )
            return Result.success()
        }

        val incompleteHabits = todayHabits.filterNot { it.isCompletedToday }
        notifier.showDailyReminder(incompleteHabits.size)
        incompleteHabits.forEach { status ->
            notifyIfPlantNeedsCare(
                notifier = notifier,
                habit = status.habit,
                isCompletedToday = status.isCompletedToday,
                lastCompletedDateKey = status.lastCompletedDateKey,
                todayDateKey = todayDateKey
            )
        }
        return Result.success()
    }

    private fun notifyIfPlantNeedsCare(
        notifier: HabitSeedNotifier,
        habit: com.habitseed.app.data.local.entity.HabitEntity,
        isCompletedToday: Boolean,
        lastCompletedDateKey: String?,
        todayDateKey: String
    ) {
        val healthInfo = PlantHealthCalculator.healthFor(
            isCompletedToday = isCompletedToday,
            lastCompletedDateKey = lastCompletedDateKey,
            todayDateKey = todayDateKey
        )
        if (healthInfo.isUrgent) {
            notifier.showPlantHealthReminder(habit, healthInfo.label.lowercase())
        }
    }

    companion object {
        const val KEY_MODE = "mode"
        const val KEY_HABIT_ID = "habit_id"
        const val MODE_GLOBAL = "global"
        const val MODE_HABIT = "habit"
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HabitReminderWorkerEntryPoint {
    fun userRepository(): UserRepository
    fun habitRepository(): HabitRepository
    fun habitSeedNotifier(): HabitSeedNotifier
}
