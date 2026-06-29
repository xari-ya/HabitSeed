package com.habitseed.app.domain.social

import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.social.dto.PublicProfileDto
import com.habitseed.app.domain.model.DailyCompletionStat

object SocialSummaryMapper {
    fun toPublicProfile(
        firebaseUid: String,
        user: UserEntity,
        habits: List<HabitEntity>,
        stats: List<DailyCompletionStat>,
        existingCreatedAt: Long?,
        now: Long = System.currentTimeMillis()
    ): PublicProfileDto {
        val lastSevenDays = stats.takeLast(7)
        val activeDays = lastSevenDays.count { it.completionCount > 0 }
        val weeklyCompletionRate = if (lastSevenDays.isEmpty()) {
            0
        } else {
            ((activeDays * 100f) / lastSevenDays.size).toInt()
        }

        return PublicProfileDto(
            uid = firebaseUid,
            displayName = user.name.ifBlank { "Gardener" },
            photoUrl = user.avatarUrl,
            currentStreak = user.currentStreak,
            bestStreak = user.bestStreak,
            fullyGrownPlants = habits.count { it.plantGrowthLevel >= FULLY_GROWN_LEVEL },
            weeklyCompletionRate = weeklyCompletionRate,
            totalCompletions = habits.sumOf { it.totalCompletions },
            lastActiveDateKey = stats.lastOrNull { it.completionCount > 0 }?.dateKey,
            createdAt = existingCreatedAt ?: now,
            updatedAt = now
        )
    }

    private const val FULLY_GROWN_LEVEL = 4
}
