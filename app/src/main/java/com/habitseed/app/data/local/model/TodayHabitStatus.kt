package com.habitseed.app.data.local.model

import androidx.room.Embedded
import com.habitseed.app.data.local.entity.HabitEntity

data class TodayHabitStatus(
    @Embedded val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val completedAt: Long? = null,
    val lastCompletedDateKey: String? = null
)
