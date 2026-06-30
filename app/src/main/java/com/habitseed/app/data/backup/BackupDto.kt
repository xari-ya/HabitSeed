package com.habitseed.app.data.backup

import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.entity.PurchaseEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.data.local.entity.UserUnlockedPlantEntity

data class UserBackupDto(
    val name: String = "",
    val waterDrops: Int = 0,
    val gardenXp: Int = 0,
    val lifetimeDropsEarned: Int = 0,
    val lastPerfectDayBonusDateKey: String? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val selectedTheme: String = "forest",
    val onboardingComplete: Boolean = false,
    val joinedAt: Long = 0,
    val createdAt: Long = 0
)

data class HabitBackupDto(
    val localId: Long = 0,
    val name: String = "",
    val description: String? = null,
    val iconName: String = "sprout",
    val colorHex: String = "#2D6A4F",
    val frequencyType: String = "DAILY",
    val weeklyDaysMask: Int? = 127,
    val targetCount: Int = 1,
    val plantTypeId: String = "sunflower",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val plantGrowthLevel: Int = 0
)

data class HabitLogBackupDto(
    val habitLocalId: Long = 0,
    val dateKey: String = "",
    val completedAt: Long? = null,
    val status: String = "COMPLETED",
    val note: String? = null,
    val waterDropsAwarded: Int = 0
)

data class PurchaseBackupDto(
    val shopItemId: String = "",
    val pricePaidDrops: Int = 0,
    val purchasedAt: Long = 0
)

data class UnlockedPlantBackupDto(
    val plantTypeId: String = "",
    val unlockedAt: Long = 0
)

data class UserSettingsBackupDto(
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val darkModeEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

data class FullBackupDto(
    val user: UserBackupDto = UserBackupDto(),
    val settings: UserSettingsBackupDto = UserSettingsBackupDto(),
    val habits: List<HabitBackupDto> = emptyList(),
    val habitLogs: List<HabitLogBackupDto> = emptyList(),
    val purchases: List<PurchaseBackupDto> = emptyList(),
    val unlockedPlants: List<UnlockedPlantBackupDto> = emptyList(),
    val backedUpAt: Long = 0
)

// Mapping extensions

fun UserEntity.toBackupDto() = UserBackupDto(
    name = name,
    waterDrops = waterDrops,
    gardenXp = gardenXp,
    lifetimeDropsEarned = lifetimeDropsEarned,
    lastPerfectDayBonusDateKey = lastPerfectDayBonusDateKey,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    selectedTheme = selectedTheme,
    onboardingComplete = onboardingComplete,
    joinedAt = joinedAt,
    createdAt = createdAt
)

fun HabitEntity.toBackupDto() = HabitBackupDto(
    localId = id,
    name = name,
    description = description,
    iconName = iconName,
    colorHex = colorHex,
    frequencyType = frequencyType,
    weeklyDaysMask = weeklyDaysMask,
    targetCount = targetCount,
    plantTypeId = plantTypeId,
    currentStreak = currentStreak,
    bestStreak = bestStreak,
    totalCompletions = totalCompletions,
    reminderEnabled = reminderEnabled,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    plantGrowthLevel = plantGrowthLevel
)

fun HabitLogEntity.toBackupDto() = HabitLogBackupDto(
    habitLocalId = habitId,
    dateKey = dateKey,
    completedAt = completedAt,
    status = status,
    note = note,
    waterDropsAwarded = waterDropsAwarded
)

fun PurchaseEntity.toBackupDto() = PurchaseBackupDto(
    shopItemId = shopItemId,
    pricePaidDrops = pricePaidDrops,
    purchasedAt = purchasedAt
)

fun UserUnlockedPlantEntity.toBackupDto() = UnlockedPlantBackupDto(
    plantTypeId = plantTypeId,
    unlockedAt = unlockedAt
)

fun UserSettingsEntity.toBackupDto() = UserSettingsBackupDto(
    notificationsEnabled = notificationsEnabled,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    darkModeEnabled = darkModeEnabled,
    soundEnabled = soundEnabled,
    hapticsEnabled = hapticsEnabled
)
