package com.habitseed.app.domain.gamification

import com.habitseed.app.domain.util.DateUtils
import java.time.temporal.ChronoUnit

enum class PlantHealthState {
    FRESH,
    HEALTHY,
    DRY,
    WILTING,
    DORMANT
}

data class PlantHealthInfo(
    val state: PlantHealthState,
    val label: String,
    val missedDays: Int,
    val isUrgent: Boolean
)

object PlantHealthCalculator {
    fun healthFor(
        isCompletedToday: Boolean,
        lastCompletedDateKey: String?,
        todayDateKey: String
    ): PlantHealthInfo {
        if (isCompletedToday) {
            return PlantHealthInfo(
                state = PlantHealthState.FRESH,
                label = "Watered today",
                missedDays = 0,
                isUrgent = false
            )
        }

        val missedDays = missedDaysSince(lastCompletedDateKey, todayDateKey)
        return when {
            missedDays >= 7 -> PlantHealthInfo(
                state = PlantHealthState.DORMANT,
                label = "Dormant",
                missedDays = missedDays,
                isUrgent = true
            )
            missedDays >= 2 -> PlantHealthInfo(
                state = PlantHealthState.WILTING,
                label = "Wilting",
                missedDays = missedDays,
                isUrgent = true
            )
            missedDays == 1 -> PlantHealthInfo(
                state = PlantHealthState.DRY,
                label = "Needs water",
                missedDays = missedDays,
                isUrgent = false
            )
            else -> PlantHealthInfo(
                state = PlantHealthState.HEALTHY,
                label = "Healthy",
                missedDays = missedDays,
                isUrgent = false
            )
        }
    }

    private fun missedDaysSince(
        lastCompletedDateKey: String?,
        todayDateKey: String
    ): Int {
        if (lastCompletedDateKey.isNullOrBlank()) return 1
        return runCatching {
            val daysSinceWatering = ChronoUnit.DAYS.between(
                DateUtils.parseDateKey(lastCompletedDateKey),
                DateUtils.parseDateKey(todayDateKey)
            ).toInt().coerceAtLeast(0)
            (daysSinceWatering - 1).coerceAtLeast(0)
        }.getOrDefault(1)
    }
}
