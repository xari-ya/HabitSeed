package com.habitseed.app.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantHealthCalculatorTest {
    @Test
    fun completedToday_returnsFresh() {
        val health = PlantHealthCalculator.healthFor(
            isCompletedToday = true,
            lastCompletedDateKey = "2026-06-29",
            todayDateKey = "2026-06-30"
        )

        assertEquals(PlantHealthState.FRESH, health.state)
        assertEquals("Watered today", health.label)
        assertEquals(0, health.missedDays)
        assertFalse(health.isUrgent)
    }

    @Test
    fun completedYesterday_returnsHealthy() {
        val health = PlantHealthCalculator.healthFor(
            isCompletedToday = false,
            lastCompletedDateKey = "2026-06-29",
            todayDateKey = "2026-06-30"
        )

        assertEquals(PlantHealthState.HEALTHY, health.state)
        assertEquals("Healthy", health.label)
        assertEquals(0, health.missedDays)
    }

    @Test
    fun missedOneScheduledDay_returnsDryNeedsWater() {
        val health = PlantHealthCalculator.healthFor(
            isCompletedToday = false,
            lastCompletedDateKey = "2026-06-28",
            todayDateKey = "2026-06-30"
        )

        assertEquals(PlantHealthState.DRY, health.state)
        assertEquals("Needs water", health.label)
        assertEquals(1, health.missedDays)
    }

    @Test
    fun missedTwoScheduledDays_returnsWilting() {
        val health = PlantHealthCalculator.healthFor(
            isCompletedToday = false,
            lastCompletedDateKey = "2026-06-27",
            todayDateKey = "2026-06-30"
        )

        assertEquals(PlantHealthState.WILTING, health.state)
        assertEquals("Wilting", health.label)
        assertTrue(health.isUrgent)
    }

    @Test
    fun missedSevenDays_returnsDormant() {
        val health = PlantHealthCalculator.healthFor(
            isCompletedToday = false,
            lastCompletedDateKey = "2026-06-22",
            todayDateKey = "2026-06-30"
        )

        assertEquals(PlantHealthState.DORMANT, health.state)
        assertEquals("Dormant", health.label)
        assertTrue(health.isUrgent)
    }

    @Test
    fun futureOrSameDayLastCompletion_returnsHealthyWhenNotCompletedToday() {
        val health = PlantHealthCalculator.healthFor(
            isCompletedToday = false,
            lastCompletedDateKey = "2026-06-30",
            todayDateKey = "2026-06-30"
        )

        assertEquals(PlantHealthState.HEALTHY, health.state)
        assertEquals("Healthy", health.label)
        assertEquals(0, health.missedDays)
    }
}
