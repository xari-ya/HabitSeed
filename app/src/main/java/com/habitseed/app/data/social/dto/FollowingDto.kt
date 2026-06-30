package com.habitseed.app.data.social.dto

data class FollowingDto(
    val targetUid: String = "",
    val displayNameSnapshot: String = "Gardener",
    val photoUrlSnapshot: String? = null,
    val followedAt: Long = 0,

    val gardenLevelSnapshot: Int = 1,
    val gardenLevelTitleSnapshot: String = "New Gardener",
    val weeklyCompletionRateSnapshot: Double = 0.0,
    val fullyGrownPlantsSnapshot: Int = 0,
    val highestPlantTypeIdSnapshot: String? = null,
    val highestPlantGrowthStageSnapshot: Int = 0,
    val currentStreakSnapshot: Int = 0
)
