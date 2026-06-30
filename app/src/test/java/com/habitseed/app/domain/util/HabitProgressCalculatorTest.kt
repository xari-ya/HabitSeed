package com.habitseed.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class HabitProgressCalculatorTest {

    @Test
    fun completeFirstDay_setsCurrentStreakToOne() {
        val streak = HabitProgressCalculator.calculateNextStreak(
            lastCompletedDateKey = null,
            currentDateKey = "2026-06-29",
            currentStreak = 0
        )

        assertEquals(1, streak)
    }

    @Test
    fun completeConsecutiveDays_incrementsStreak() {
        val streak = HabitProgressCalculator.calculateNextStreak(
            lastCompletedDateKey = "2026-06-28",
            currentDateKey = "2026-06-29",
            currentStreak = 4
        )

        assertEquals(5, streak)
    }

    @Test
    fun missedRequiredDay_resetsStreak() {
        val streak = HabitProgressCalculator.calculateNextStreak(
            lastCompletedDateKey = "2026-06-26",
            currentDateKey = "2026-06-29",
            currentStreak = 4
        )

        assertEquals(1, streak)
    }

    @Test
    fun plantGrowthLevel_updatesAfterCompletions() {
        assertEquals(0, HabitProgressCalculator.calculateGrowthLevel(0))
        assertEquals(1, HabitProgressCalculator.calculateGrowthLevel(1))
        assertEquals(2, HabitProgressCalculator.calculateGrowthLevel(3))
        assertEquals(3, HabitProgressCalculator.calculateGrowthLevel(7))
        assertEquals(4, HabitProgressCalculator.calculateGrowthLevel(14))
        assertEquals(5, HabitProgressCalculator.calculateGrowthLevel(30))
    }
}
