package com.habitseed.app.data.social.dto

data class FollowingDto(
    val targetUid: String = "",
    val displayNameSnapshot: String = "Gardener",
    val photoUrlSnapshot: String? = null,
    val followedAt: Long = 0
)
