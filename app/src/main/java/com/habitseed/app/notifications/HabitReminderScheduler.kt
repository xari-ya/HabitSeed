package com.habitseed.app.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitReminderScheduler @Inject constructor(
    @ApplicationContext context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun syncReminders(settings: UserSettingsEntity, habits: List<HabitEntity>) {
        workManager.cancelAllWorkByTag(REMINDER_WORK_TAG)
        if (!settings.notificationsEnabled) return

        scheduleGlobalReminder(settings)
        habits
            .filter { !it.isArchived && it.reminderEnabled }
            .forEach { habit ->
                scheduleHabitReminder(
                    habit = habit,
                    fallbackHour = settings.reminderHour,
                    fallbackMinute = settings.reminderMinute
                )
            }
    }

    private fun scheduleGlobalReminder(settings: UserSettingsEntity) {
        val request = reminderWorkRequest(
            workName = GLOBAL_REMINDER_WORK_NAME,
            hour = settings.reminderHour,
            minute = settings.reminderMinute,
            data = Data.Builder()
                .putString(HabitReminderWorker.KEY_MODE, HabitReminderWorker.MODE_GLOBAL)
                .build()
        )
        workManager.enqueueUniquePeriodicWork(
            GLOBAL_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun scheduleHabitReminder(
        habit: HabitEntity,
        fallbackHour: Int,
        fallbackMinute: Int
    ) {
        val hour = habit.reminderHour ?: fallbackHour
        val minute = habit.reminderMinute ?: fallbackMinute
        val workName = habitReminderWorkName(habit.id)
        val request = reminderWorkRequest(
            workName = workName,
            hour = hour,
            minute = minute,
            data = Data.Builder()
                .putString(HabitReminderWorker.KEY_MODE, HabitReminderWorker.MODE_HABIT)
                .putLong(HabitReminderWorker.KEY_HABIT_ID, habit.id)
                .build()
        )
        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun reminderWorkRequest(
        workName: String,
        hour: Int,
        minute: Int,
        data: Data
    ) = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(delayUntil(hour, minute).toMillis(), TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag(REMINDER_WORK_TAG)
        .addTag(workName)
        .build()

    private fun delayUntil(
        hour: Int,
        minute: Int,
        now: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Duration {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        val currentDateTime = now.atZone(zoneId).toLocalDateTime()
        var nextRun = LocalDate.from(currentDateTime).atTime(safeHour, safeMinute)
        if (!nextRun.isAfter(currentDateTime)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(currentDateTime, nextRun)
    }

    companion object {
        private const val REMINDER_WORK_TAG = "habitseed_reminders"
        private const val GLOBAL_REMINDER_WORK_NAME = "habitseed_global_daily_reminder"

        private fun habitReminderWorkName(habitId: Long): String {
            return "habitseed_habit_reminder_$habitId"
        }
    }
}
