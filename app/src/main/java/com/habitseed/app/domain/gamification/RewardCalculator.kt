package com.habitseed.app.domain.gamification

data class RewardBundle(
    val waterDrops: Int = 0,
    val gardenXp: Int = 0,
    val messages: List<String> = emptyList()
)

object RewardCalculator {
    fun completionReward(): RewardBundle {
        return RewardBundle(
            waterDrops = 10,
            gardenXp = 10,
            messages = listOf("Habit watered. +10 drops and +10 XP earned.")
        )
    }

    fun stageUpReward(newStage: Int, stageLabel: String): RewardBundle {
        return RewardBundle(
            waterDrops = 20,
            gardenXp = 25,
            messages = listOf("Your plant reached stage $newStage: $stageLabel. +20 drops and +25 XP earned.")
        )
    }

    fun fullyGrownReward(): RewardBundle {
        return RewardBundle(
            waterDrops = 100,
            gardenXp = 100,
            messages = listOf("Fully grown plant. +100 drops and +100 XP earned.")
        )
    }

    fun perfectDayReward(): RewardBundle {
        return RewardBundle(
            waterDrops = 25,
            gardenXp = 20,
            messages = listOf("Perfect day. +25 drops and +20 XP earned.")
        )
    }
}
