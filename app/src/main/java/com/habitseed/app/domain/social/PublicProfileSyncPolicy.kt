package com.habitseed.app.domain.social

import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.dto.PublicProfileDto
import com.habitseed.app.domain.util.DateUtils
import java.security.MessageDigest

object PublicProfileSyncPolicy {
    private const val APP_START_SYNC_INTERVAL_MS = 24L * 60L * 60L * 1000L

    fun shouldSync(
        reason: PublicProfileSyncReason,
        previousHash: String?,
        lastSyncedAt: Long?,
        nextHash: String,
        now: Long
    ): Boolean {
        if (previousHash.isNullOrBlank() || lastSyncedAt == null) return true
        if (previousHash == nextHash) return false

        return when (reason) {
            PublicProfileSyncReason.SIGN_IN,
            PublicProfileSyncReason.PROFILE_EDIT -> true
            PublicProfileSyncReason.APP_START -> now - lastSyncedAt >= APP_START_SYNC_INTERVAL_MS
            PublicProfileSyncReason.HABIT_COMPLETED -> {
                DateUtils.getDateKey(lastSyncedAt) != DateUtils.getDateKey(now)
            }
        }
    }

    fun syncHash(profile: PublicProfileDto): String {
        val canonicalPayload = listOf(
            profile.displayName,
            profile.photoUrl.orEmpty(),
            profile.currentStreak.toString(),
            profile.bestStreak.toString(),
            profile.gardenLevel.toString(),
            profile.gardenLevelTitle,
            profile.gardenLevelProgressPercent.toString(),
            profile.fullyGrownPlants.toString(),
            profile.totalPlants.toString(),
            profile.highestPlantTypeId.orEmpty(),
            profile.highestPlantGrowthStage.toString(),
            profile.weeklyCompletionRate.toString(),
            profile.totalCompletions.toString(),
            profile.perfectDays.toString(),
            profile.lastActiveDateKey.orEmpty()
        ).joinToString(separator = "|")
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonicalPayload.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
