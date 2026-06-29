package com.habitseed.app.domain.util

object StreakCalculator {
    // Basic logic for demonstration. In a real app, you would check all logs.
    fun calculateNewStreak(currentStreak: Int, isCompleted: Boolean, missedDays: Int = 0): Int {
        if (isCompleted) {
            return currentStreak + 1
        }
        if (missedDays > 0) {
            return 0
        }
        return currentStreak
    }
}
