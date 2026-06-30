package com.habitseed.app.domain.social

import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.social.dto.PublicProfileDto
import com.habitseed.app.domain.gamification.GardenLevelCalculator
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
        val gardenLevelInfo = GardenLevelCalculator.levelForXp(user.gardenXp)
        val highestPlant = habits.maxWithOrNull(
            compareBy<HabitEntity> { it.plantGrowthLevel }
                .thenBy { it.totalCompletions }
        )
        val weeklyCompletionRate = if (lastSevenDays.isEmpty()) {
            0.0
        } else {
            ((activeDays * 100.0) / lastSevenDays.size)
        }

        return PublicProfileDto(
            uid = firebaseUid,
            displayName = user.name.ifBlank { "Gardener" },
            photoUrl = user.avatarUrl,
            currentStreak = user.currentStreak,
            bestStreak = user.bestStreak,
            gardenLevel = gardenLevelInfo.level,
            gardenLevelTitle = gardenLevelInfo.title,
            gardenLevelProgressPercent = gardenLevelInfo.progressPercent,
            fullyGrownPlants = habits.count { it.plantGrowthLevel >= FULLY_GROWN_LEVEL },
            totalPlants = habits.size,
            highestPlantTypeId = highestPlant?.plantTypeId,
            highestPlantGrowthStage = highestPlant?.plantGrowthLevel ?: 0,
            weeklyCompletionRate = weeklyCompletionRate,
            totalCompletions = habits.sumOf { it.totalCompletions },
            perfectDays = user.lastPerfectDayBonusDateKey?.let { 1 } ?: 0,
            lastActiveDateKey = stats.lastOrNull { it.completionCount > 0 }?.dateKey,
            createdAt = existingCreatedAt ?: now,
            updatedAt = now
        )
    }

    private const val FULLY_GROWN_LEVEL = 5
}
