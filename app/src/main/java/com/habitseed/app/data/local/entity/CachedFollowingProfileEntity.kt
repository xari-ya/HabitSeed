package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.habitseed.app.data.social.dto.FollowingDto

@Entity(tableName = "cached_following_profiles")
data class CachedFollowingProfileEntity(
    @PrimaryKey val targetUid: String,
    val displayNameSnapshot: String,
    val photoUrlSnapshot: String?,
    val followedAt: Long,
    val gardenLevelSnapshot: Int,
    val gardenLevelTitleSnapshot: String,
    val weeklyCompletionRateSnapshot: Double,
    val fullyGrownPlantsSnapshot: Int,
    val highestPlantTypeIdSnapshot: String?,
    val highestPlantGrowthStageSnapshot: Int,
    val currentStreakSnapshot: Int,
    val cachedAt: Long
) {
    fun toDto(): FollowingDto {
        return FollowingDto(
            targetUid = targetUid,
            displayNameSnapshot = displayNameSnapshot,
            photoUrlSnapshot = photoUrlSnapshot,
            followedAt = followedAt,
            gardenLevelSnapshot = gardenLevelSnapshot,
            gardenLevelTitleSnapshot = gardenLevelTitleSnapshot,
            weeklyCompletionRateSnapshot = weeklyCompletionRateSnapshot,
            fullyGrownPlantsSnapshot = fullyGrownPlantsSnapshot,
            highestPlantTypeIdSnapshot = highestPlantTypeIdSnapshot,
            highestPlantGrowthStageSnapshot = highestPlantGrowthStageSnapshot,
            currentStreakSnapshot = currentStreakSnapshot
        )
    }

    companion object {
        fun fromDto(following: FollowingDto, cachedAt: Long): CachedFollowingProfileEntity {
            return CachedFollowingProfileEntity(
                targetUid = following.targetUid,
                displayNameSnapshot = following.displayNameSnapshot,
                photoUrlSnapshot = following.photoUrlSnapshot,
                followedAt = following.followedAt,
                gardenLevelSnapshot = following.gardenLevelSnapshot,
                gardenLevelTitleSnapshot = following.gardenLevelTitleSnapshot,
                weeklyCompletionRateSnapshot = following.weeklyCompletionRateSnapshot,
                fullyGrownPlantsSnapshot = following.fullyGrownPlantsSnapshot,
                highestPlantTypeIdSnapshot = following.highestPlantTypeIdSnapshot,
                highestPlantGrowthStageSnapshot = following.highestPlantGrowthStageSnapshot,
                currentStreakSnapshot = following.currentStreakSnapshot,
                cachedAt = cachedAt
            )
        }
    }
}
