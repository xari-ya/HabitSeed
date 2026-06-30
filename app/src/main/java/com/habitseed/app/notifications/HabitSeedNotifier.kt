package com.habitseed.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.habitseed.app.MainActivity
import com.habitseed.app.R
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.social.dto.NudgeDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class HabitSeedNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(
                CHANNEL_HABIT_REMINDERS,
                "Habit reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for habits that still need watering."
            },
            NotificationChannel(
                CHANNEL_PLANT_HEALTH,
                "Plant health",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle alerts when plants are wilting or dormant."
            },
            NotificationChannel(
                CHANNEL_REWARDS,
                "Rewards",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Milestone and reward updates."
            },
            NotificationChannel(
                CHANNEL_SOCIAL,
                "Social nudges",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when friends send nudges."
            }
        )
        notificationManager.createNotificationChannels(channels)
    }

    fun canPostNotifications(): Boolean {
        val hasRuntimePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        return hasRuntimePermission && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showDailyReminder(incompleteCount: Int) {
        if (!canPostNotifications() || incompleteCount <= 0) return
        val habitText = if (incompleteCount == 1) "habit" else "habits"
        showNotification(
            id = DAILY_REMINDER_NOTIFICATION_ID,
            channelId = CHANNEL_HABIT_REMINDERS,
            title = "Your garden needs water",
            text = "You have $incompleteCount $habitText ready to complete today."
        )
    }

    fun showHabitReminder(habit: HabitEntity) {
        if (!canPostNotifications()) return
        showNotification(
            id = habitNotificationId(HABIT_REMINDER_ID_BASE, habit.id),
            channelId = CHANNEL_HABIT_REMINDERS,
            title = "Time to water ${habit.title}",
            text = habit.description?.takeIf { it.isNotBlank() }
                ?: "A small check-in keeps this habit growing."
        )
    }

    fun showPlantHealthReminder(habit: HabitEntity, healthLabel: String) {
        if (!canPostNotifications()) return
        showNotification(
            id = habitNotificationId(PLANT_HEALTH_ID_BASE, habit.id),
            channelId = CHANNEL_PLANT_HEALTH,
            title = "${habit.title} is $healthLabel",
            text = "Water this habit today to bring your plant back."
        )
    }

    fun showReward(title: String, message: String) {
        if (!canPostNotifications()) return
        showNotification(
            id = REWARD_NOTIFICATION_ID,
            channelId = CHANNEL_REWARDS,
            title = title,
            text = message
        )
    }

    fun showSocialNudge(nudge: NudgeDto) {
        if (!canPostNotifications()) return
        val sender = nudge.fromName.ifBlank { "A friend" }
        showNotification(
            id = nudgeNotificationId(nudge),
            channelId = CHANNEL_SOCIAL,
            title = "$sender sent a nudge",
            text = nudge.message.ifBlank { "Keep your garden growing." }
        )
    }

    private fun showNotification(
        id: Int,
        channelId: String,
        title: String,
        text: String
    ) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(mainActivityPendingIntent())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun mainActivityPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            MAIN_ACTIVITY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun habitNotificationId(base: Int, habitId: Long): Int {
        return base + habitId.hashCode().absoluteValue
    }

    private fun nudgeNotificationId(nudge: NudgeDto): Int {
        val source = nudge.id.ifBlank { "${nudge.fromUid}-${nudge.createdAt}" }
        return SOCIAL_NUDGE_ID_BASE + source.hashCode().absoluteValue
    }

    companion object {
        const val CHANNEL_HABIT_REMINDERS = "habit_reminders"
        const val CHANNEL_PLANT_HEALTH = "plant_health"
        const val CHANNEL_REWARDS = "rewards"
        const val CHANNEL_SOCIAL = "social_nudges"

        private const val MAIN_ACTIVITY_REQUEST_CODE = 10
        private const val DAILY_REMINDER_NOTIFICATION_ID = 1_000
        private const val REWARD_NOTIFICATION_ID = 1_001
        private const val HABIT_REMINDER_ID_BASE = 2_000
        private const val PLANT_HEALTH_ID_BASE = 20_000
        private const val SOCIAL_NUDGE_ID_BASE = 40_000
    }
}
