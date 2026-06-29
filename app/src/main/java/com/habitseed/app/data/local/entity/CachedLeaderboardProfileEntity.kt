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
    val fullyGrownPlants: Int,
    val weeklyCompletionRate: Int,
    val totalCompletions: Int,
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
            fullyGrownPlants = fullyGrownPlants,
            weeklyCompletionRate = weeklyCompletionRate,
            totalCompletions = totalCompletions,
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
                fullyGrownPlants = profile.fullyGrownPlants,
                weeklyCompletionRate = profile.weeklyCompletionRate,
                totalCompletions = profile.totalCompletions,
                lastActiveDateKey = profile.lastActiveDateKey,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt,
                rank = rank,
                cachedAt = cachedAt
            )
        }
    }
}
