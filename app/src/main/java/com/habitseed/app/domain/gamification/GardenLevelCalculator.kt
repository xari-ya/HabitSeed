package com.habitseed.app.domain.gamification

data class GardenLevelInfo(
    val level: Int,
    val title: String,
    val currentXp: Int,
    val currentLevelXp: Int,
    val nextLevelXp: Int?,
    val progressPercent: Int
)

object GardenLevelCalculator {
    fun levelForXp(xp: Int): GardenLevelInfo {
        val safeXp = xp.coerceAtLeast(0)
        val currentLevel = levels.last { safeXp >= it.requiredXp }
        val nextLevel = levels.firstOrNull { safeXp < it.requiredXp }
        val progressPercent = if (nextLevel == null) {
            100
        } else {
            val earnedInLevel = safeXp - currentLevel.requiredXp
            val requiredForLevel = nextLevel.requiredXp - currentLevel.requiredXp
            ((earnedInLevel * 100f) / requiredForLevel).toInt().coerceIn(0, 100)
        }

        return GardenLevelInfo(
            level = currentLevel.level,
            title = currentLevel.title,
            currentXp = safeXp,
            currentLevelXp = currentLevel.requiredXp,
            nextLevelXp = nextLevel?.requiredXp,
            progressPercent = progressPercent
        )
    }

    private data class LevelThreshold(
        val level: Int,
        val title: String,
        val requiredXp: Int
    )

    private val levels = listOf(
        LevelThreshold(1, "New Gardener", 0),
        LevelThreshold(2, "Sprout Keeper", 100),
        LevelThreshold(3, "Green Thumb", 250),
        LevelThreshold(4, "Bloom Builder", 450),
        LevelThreshold(5, "Garden Guardian", 700),
        LevelThreshold(6, "Harvest Hero", 1000),
        LevelThreshold(7, "Forest Maker", 1400),
        LevelThreshold(8, "Botanical Master", 1900),
        LevelThreshold(9, "Nature Sage", 2500),
        LevelThreshold(10, "Habit Sage", 3200)
    )
}
