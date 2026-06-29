package com.habitseed.app.domain.util

import kotlin.math.min

object HabitProgressCalculator {
    fun calculateNextStreak(
        lastCompletedDateKey: String?,
        currentDateKey: String,
        currentStreak: Int
    ): Int {
        if (lastCompletedDateKey == null) return 1
        if (lastCompletedDateKey == currentDateKey) return currentStreak
        return if (DateUtils.isPreviousDateKey(lastCompletedDateKey, currentDateKey)) {
            currentStreak + 1
        } else {
            1
        }
    }

    fun calculateGrowthLevel(totalCompletions: Int): Int {
        return min(4, totalCompletions / 5)
    }
}
