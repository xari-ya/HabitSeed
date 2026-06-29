package com.habitseed.app.domain.social

import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.domain.model.DailyCompletionStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SocialSummaryMapperTest {
    @Test
    fun mapsOnlyPublicGardenSummaryFields() {
        val user = UserEntity(
            name = "Kaveesha Perera",
            email = "private@example.com",
            avatarUrl = "https://example.com/avatar.png",
            waterDrops = 999,
            currentStreak = 4,
            bestStreak = 12,
            createdAt = 100L
        )
        val habits = listOf(
            HabitEntity(
                name = "Private medication habit",
                description = "Sensitive description",
                totalCompletions = 22,
                plantGrowthLevel = 4
            ),
            HabitEntity(
                name = "Private journal habit",
                totalCompletions = 3,
                plantGrowthLevel = 0
            )
        )
        val stats = listOf(
            DailyCompletionStat("2026-06-23", 1),
            DailyCompletionStat("2026-06-24", 1),
            DailyCompletionStat("2026-06-25", 0),
            DailyCompletionStat("2026-06-26", 2),
            DailyCompletionStat("2026-06-27", 0),
            DailyCompletionStat("2026-06-28", 1),
            DailyCompletionStat("2026-06-29", 1)
        )

        val profile = SocialSummaryMapper.toPublicProfile(
            firebaseUid = "firebase-uid",
            user = user,
            habits = habits,
            stats = stats,
            existingCreatedAt = user.createdAt,
            now = 200L
        )

        assertEquals("firebase-uid", profile.uid)
        assertEquals("Kaveesha Perera", profile.displayName)
        assertEquals("https://example.com/avatar.png", profile.photoUrl)
        assertEquals(4, profile.currentStreak)
        assertEquals(12, profile.bestStreak)
        assertEquals(1, profile.fullyGrownPlants)
        assertEquals(71, profile.weeklyCompletionRate)
        assertEquals(25, profile.totalCompletions)
        assertEquals("2026-06-29", profile.lastActiveDateKey)
        assertEquals(100L, profile.createdAt)
        assertEquals(200L, profile.updatedAt)

        val serializedShape = profile.toString()
        assertFalse(serializedShape.contains("Private medication habit"))
        assertFalse(serializedShape.contains("Sensitive description"))
        assertFalse(serializedShape.contains("private@example.com"))
        assertFalse(serializedShape.contains("999"))
    }
}
