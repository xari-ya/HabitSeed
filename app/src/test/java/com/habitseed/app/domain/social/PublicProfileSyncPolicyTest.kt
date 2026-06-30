package com.habitseed.app.domain.social

import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.dto.PublicProfileDto
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicProfileSyncPolicyTest {
    @Test
    fun firstSignInWritesWhenNoPriorSyncExists() {
        assertTrue(
            PublicProfileSyncPolicy.shouldSync(
                reason = PublicProfileSyncReason.SIGN_IN,
                previousHash = null,
                lastSyncedAt = null,
                nextHash = "hash",
                now = dayStart("2026-06-29")
            )
        )
    }

    @Test
    fun appStartWithUnchangedHashSkips() {
        assertFalse(
            PublicProfileSyncPolicy.shouldSync(
                reason = PublicProfileSyncReason.APP_START,
                previousHash = "hash",
                lastSyncedAt = dayStart("2026-06-28"),
                nextHash = "hash",
                now = dayStart("2026-06-30")
            )
        )
    }

    @Test
    fun habitCompletionWritesAtMostOncePerLocalDate() {
        val previousHash = "old-hash"
        val nextHash = "new-hash"
        val syncedThisMorning = dayStart("2026-06-29")
        val laterSameDay = syncedThisMorning + 12L * 60L * 60L * 1000L
        val nextDay = dayStart("2026-06-30")

        assertFalse(
            PublicProfileSyncPolicy.shouldSync(
                reason = PublicProfileSyncReason.HABIT_COMPLETED,
                previousHash = previousHash,
                lastSyncedAt = syncedThisMorning,
                nextHash = nextHash,
                now = laterSameDay
            )
        )
        assertTrue(
            PublicProfileSyncPolicy.shouldSync(
                reason = PublicProfileSyncReason.HABIT_COMPLETED,
                previousHash = previousHash,
                lastSyncedAt = syncedThisMorning,
                nextHash = nextHash,
                now = nextDay
            )
        )
    }

    @Test
    fun profileEditWritesOnlyWhenPublicHashChanges() {
        assertFalse(
            PublicProfileSyncPolicy.shouldSync(
                reason = PublicProfileSyncReason.PROFILE_EDIT,
                previousHash = "same-hash",
                lastSyncedAt = dayStart("2026-06-29"),
                nextHash = "same-hash",
                now = dayStart("2026-06-29") + 1_000L
            )
        )
        assertTrue(
            PublicProfileSyncPolicy.shouldSync(
                reason = PublicProfileSyncReason.PROFILE_EDIT,
                previousHash = "old-hash",
                lastSyncedAt = dayStart("2026-06-29"),
                nextHash = "new-hash",
                now = dayStart("2026-06-29") + 1_000L
            )
        )
    }

    @Test
    fun syncHashIgnoresUpdatedAtButIncludesPublicFields() {
        val base = PublicProfileDto(
            uid = "uid",
            displayName = "Gardener",
            photoUrl = "https://example.com/a.png",
            currentStreak = 3,
            bestStreak = 6,
            gardenLevel = 2,
            gardenLevelTitle = "Sprout Keeper",
            gardenLevelProgressPercent = 25,
            fullyGrownPlants = 1,
            totalPlants = 3,
            highestPlantTypeId = "cactus",
            highestPlantGrowthStage = 4,
            weeklyCompletionRate = 71.0,
            totalCompletions = 12,
            perfectDays = 1,
            lastActiveDateKey = "2026-06-29",
            updatedAt = 100L
        )

        assertEquals(
            PublicProfileSyncPolicy.syncHash(base),
            PublicProfileSyncPolicy.syncHash(base.copy(updatedAt = 200L))
        )
        assertNotEquals(
            PublicProfileSyncPolicy.syncHash(base),
            PublicProfileSyncPolicy.syncHash(base.copy(displayName = "New Name"))
        )
        assertNotEquals(
            PublicProfileSyncPolicy.syncHash(base),
            PublicProfileSyncPolicy.syncHash(base.copy(gardenLevel = 3))
        )
    }

    private fun dayStart(dateKey: String): Long {
        return LocalDate.parse(dateKey)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
