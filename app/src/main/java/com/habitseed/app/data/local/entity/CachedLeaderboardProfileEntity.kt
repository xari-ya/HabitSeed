package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.habitseed.app.data.social.dto.PublicProfileDto

@Entity(tableName = "cached_leaderboard_profiles")
data class CachedLeaderboardProfileEntity(
    @PrimaryKey val uid: String,
    val displayName: String,
    val photoUrl: String?,
    val currentStreak: Int,
    val bestStreak: Int,
    val gardenLevel: Int,
    val gardenLevelTitle: String,
    val gardenLevelProgressPercent: Int,
    val fullyGrownPlants: Int,
    val totalPlants: Int,
    val highestPlantTypeId: String?,
    val highestPlantGrowthStage: Int,
    val weeklyCompletionRate: Double,
    val totalCompletions: Int,
    val perfectDays: Int,
    val lastActiveDateKey: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val rank: Int,
    val cachedAt: Long
) {
    fun toDto(): PublicProfileDto {
        return PublicProfileDto(
            uid = uid,
            displayName = displayName,
            photoUrl = photoUrl,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            gardenLevel = gardenLevel,
            gardenLevelTitle = gardenLevelTitle,
            gardenLevelProgressPercent = gardenLevelProgressPercent,
            fullyGrownPlants = fullyGrownPlants,
            totalPlants = totalPlants,
            highestPlantTypeId = highestPlantTypeId,
            highestPlantGrowthStage = highestPlantGrowthStage,
            weeklyCompletionRate = weeklyCompletionRate,
            totalCompletions = totalCompletions,
            perfectDays = perfectDays,
            lastActiveDateKey = lastActiveDateKey,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDto(profile: PublicProfileDto, rank: Int, cachedAt: Long): CachedLeaderboardProfileEntity {
            return CachedLeaderboardProfileEntity(
                uid = profile.uid,
                displayName = profile.displayName,
                photoUrl = profile.photoUrl,
                currentStreak = profile.currentStreak,
                bestStreak = profile.bestStreak,
                gardenLevel = profile.gardenLevel,
                gardenLevelTitle = profile.gardenLevelTitle,
                gardenLevelProgressPercent = profile.gardenLevelProgressPercent,
                fullyGrownPlants = profile.fullyGrownPlants,
                totalPlants = profile.totalPlants,
                highestPlantTypeId = profile.highestPlantTypeId,
                highestPlantGrowthStage = profile.highestPlantGrowthStage,
                weeklyCompletionRate = profile.weeklyCompletionRate,
                totalCompletions = profile.totalCompletions,
                perfectDays = profile.perfectDays,
                lastActiveDateKey = profile.lastActiveDateKey,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt,
                rank = rank,
                cachedAt = cachedAt
            )
        }
    }
}
