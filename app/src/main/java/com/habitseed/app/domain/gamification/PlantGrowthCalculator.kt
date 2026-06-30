package com.habitseed.app.domain.gamification

object PlantGrowthCalculator {
    fun stageFor(totalCompletions: Int): Int {
        return when {
            totalCompletions >= 30 -> 5
            totalCompletions >= 14 -> 4
            totalCompletions >= 7 -> 3
            totalCompletions >= 3 -> 2
            totalCompletions >= 1 -> 1
            else -> 0
        }
    }

    fun labelFor(stage: Int): String {
        return when (stage.coerceIn(0, 5)) {
            0 -> "Seed"
            1 -> "Sprout"
            2 -> "Young Plant"
            3 -> "Healthy Plant"
            4 -> "Blooming Plant"
            else -> "Fully Grown"
        }
    }

    fun nextStageTarget(totalCompletions: Int): Int? {
        return when {
            totalCompletions < 1 -> 1
            totalCompletions < 3 -> 3
            totalCompletions < 7 -> 7
            totalCompletions < 14 -> 14
            totalCompletions < 30 -> 30
            else -> null
        }
    }

    fun progressToNextStage(totalCompletions: Int): Float {
        val safeCompletions = totalCompletions.coerceAtLeast(0)
        val currentThreshold = when {
            safeCompletions >= 30 -> 30
            safeCompletions >= 14 -> 14
            safeCompletions >= 7 -> 7
            safeCompletions >= 3 -> 3
            safeCompletions >= 1 -> 1
            else -> 0
        }
        val nextThreshold = nextStageTarget(safeCompletions) ?: return 1f
        return ((safeCompletions - currentThreshold).toFloat() / (nextThreshold - currentThreshold))
            .coerceIn(0f, 1f)
    }

    fun completionsText(totalCompletions: Int): String {
        val safeCompletions = totalCompletions.coerceAtLeast(0)
        val nextTarget = nextStageTarget(safeCompletions)
        return if (nextTarget == null) {
            "$safeCompletions / 30"
        } else {
            "$safeCompletions / $nextTarget"
        }
    }
}
