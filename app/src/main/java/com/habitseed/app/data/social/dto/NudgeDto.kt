package com.habitseed.app.data.social.dto

data class NudgeDto(
    val fromUid: String = "",
    val toUid: String = "",
    val fromName: String = "",
    val fromPhotoUrl: String? = null,
    val messageType: String = NudgeMessageTypes.KEEP_GOING,
    val message: String = "Keep your garden growing.",
    val createdAt: Long = 0,
    val readAt: Long? = null
)

object NudgeMessageTypes {
    const val WATER_REMINDER = "WATER_REMINDER"
    const val KEEP_GOING = "KEEP_GOING"
    const val STREAK_CHEER = "STREAK_CHEER"
    const val GARDEN_NUDGE = "GARDEN_NUDGE"

    val allowedTypes = setOf(
        WATER_REMINDER,
        KEEP_GOING,
        STREAK_CHEER,
        GARDEN_NUDGE
    )
}
