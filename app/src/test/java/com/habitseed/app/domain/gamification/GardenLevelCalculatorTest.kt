package com.habitseed.app.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Test

class GardenLevelCalculatorTest {
    @Test
    fun levelForXp_zero_returnsNewGardener() {
        val info = GardenLevelCalculator.levelForXp(0)

        assertEquals(1, info.level)
        assertEquals("New Gardener", info.title)
        assertEquals(0, info.currentLevelXp)
        assertEquals(100, info.nextLevelXp)
        assertEquals(0, info.progressPercent)
    }

    @Test
    fun levelForXp_100_returnsSproutKeeper() {
        val info = GardenLevelCalculator.levelForXp(100)

        assertEquals(2, info.level)
        assertEquals("Sprout Keeper", info.title)
        assertEquals(100, info.currentLevelXp)
        assertEquals(250, info.nextLevelXp)
        assertEquals(0, info.progressPercent)
    }

    @Test
    fun levelForXp_250_returnsGreenThumb() {
        val info = GardenLevelCalculator.levelForXp(250)

        assertEquals(3, info.level)
        assertEquals("Green Thumb", info.title)
        assertEquals(250, info.currentLevelXp)
        assertEquals(450, info.nextLevelXp)
    }

    @Test
    fun levelForXp_3200_returnsHabitSage() {
        val info = GardenLevelCalculator.levelForXp(3200)

        assertEquals(10, info.level)
        assertEquals("Habit Sage", info.title)
        assertEquals(null, info.nextLevelXp)
        assertEquals(100, info.progressPercent)
    }

    @Test
    fun levelProgressPercent_isClampedBetweenZeroAndHundred() {
        assertEquals(0, GardenLevelCalculator.levelForXp(-10).progressPercent)
        assertEquals(50, GardenLevelCalculator.levelForXp(50).progressPercent)
        assertEquals(100, GardenLevelCalculator.levelForXp(999_999).progressPercent)
    }
}
