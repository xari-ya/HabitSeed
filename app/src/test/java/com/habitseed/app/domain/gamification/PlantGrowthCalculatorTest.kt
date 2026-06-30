package com.habitseed.app.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Test

class PlantGrowthCalculatorTest {
    @Test
    fun stageFor_zero_returnsSeed() {
        assertEquals(0, PlantGrowthCalculator.stageFor(0))
    }

    @Test
    fun stageFor_one_returnsSprout() {
        assertEquals(1, PlantGrowthCalculator.stageFor(1))
    }

    @Test
    fun stageFor_three_returnsYoungPlant() {
        assertEquals(2, PlantGrowthCalculator.stageFor(3))
    }

    @Test
    fun stageFor_seven_returnsHealthyPlant() {
        assertEquals(3, PlantGrowthCalculator.stageFor(7))
    }

    @Test
    fun stageFor_fourteen_returnsBloomingPlant() {
        assertEquals(4, PlantGrowthCalculator.stageFor(14))
    }

    @Test
    fun stageFor_thirty_returnsFullyGrown() {
        assertEquals(5, PlantGrowthCalculator.stageFor(30))
    }

    @Test
    fun stageFor_largeValue_staysFullyGrown() {
        assertEquals(5, PlantGrowthCalculator.stageFor(300))
    }

    @Test
    fun labelFor_invalidStage_clampsToKnownLabels() {
        assertEquals("Seed", PlantGrowthCalculator.labelFor(-4))
        assertEquals("Fully Grown", PlantGrowthCalculator.labelFor(99))
    }

    @Test
    fun nextStageTarget_returnsNullAfterFullyGrown() {
        assertEquals(1, PlantGrowthCalculator.nextStageTarget(0))
        assertEquals(30, PlantGrowthCalculator.nextStageTarget(14))
        assertEquals(null, PlantGrowthCalculator.nextStageTarget(30))
    }

    @Test
    fun progressToNextStage_isClampedBetweenZeroAndOne() {
        assertEquals(0f, PlantGrowthCalculator.progressToNextStage(-5), 0.001f)
        assertEquals(0.5f, PlantGrowthCalculator.progressToNextStage(2), 0.001f)
        assertEquals(1f, PlantGrowthCalculator.progressToNextStage(30), 0.001f)
    }

    @Test
    fun completionsText_usesNextTargetUntilFullyGrown() {
        assertEquals("0 / 1", PlantGrowthCalculator.completionsText(0))
        assertEquals("14 / 30", PlantGrowthCalculator.completionsText(14))
        assertEquals("30 / 30", PlantGrowthCalculator.completionsText(30))
    }
}
