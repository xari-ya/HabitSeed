package com.habitseed.app.domain.gamification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardCalculatorTest {
    @Test
    fun completionReward_returns10DropsAnd10Xp() {
        val reward = RewardCalculator.completionReward()

        assertEquals(10, reward.waterDrops)
        assertEquals(10, reward.gardenXp)
        assertTrue(reward.messages.isNotEmpty())
    }

    @Test
    fun stageUpReward_returns20DropsAnd25Xp() {
        val reward = RewardCalculator.stageUpReward(
            newStage = 2,
            stageLabel = "Young Plant"
        )

        assertEquals(20, reward.waterDrops)
        assertEquals(25, reward.gardenXp)
        assertTrue(reward.messages.first().contains("Young Plant"))
    }

    @Test
    fun fullyGrownReward_returns100DropsAnd100Xp() {
        val reward = RewardCalculator.fullyGrownReward()

        assertEquals(100, reward.waterDrops)
        assertEquals(100, reward.gardenXp)
        assertTrue(reward.messages.isNotEmpty())
    }

    @Test
    fun perfectDayReward_returns25DropsAnd20Xp() {
        val reward = RewardCalculator.perfectDayReward()

        assertEquals(25, reward.waterDrops)
        assertEquals(20, reward.gardenXp)
        assertTrue(reward.messages.isNotEmpty())
    }
}
