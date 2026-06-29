package com.habitseed.app.data.social.dto

data class PublicProfileDto(
    val uid: String = "",
    val displayName: String = "Gardener",
    val photoUrl: String? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val fullyGrownPlants: Int = 0,
    val weeklyCompletionRate: Int = 0,
    val totalCompletions: Int = 0,
    val lastActiveDateKey: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
