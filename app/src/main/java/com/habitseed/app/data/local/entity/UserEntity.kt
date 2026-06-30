package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = "local_user",
    val name: String,
    val email: String? = null,
    val avatarAssetName: String? = null,
    val firebaseUid: String? = null,
    @ColumnInfo(defaultValue = "'local'")
    val authProvider: String = "local",
    val avatarUrl: String? = null,
    @ColumnInfo(defaultValue = "0")
    val emailVerified: Boolean = false,
    val lastLoginAt: Long? = null,
    val lastCloudSyncAt: Long? = null,
    val publicProfileSyncHash: String? = null,
    val joinedAt: Long = System.currentTimeMillis(),
    val waterDrops: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val gardenXp: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val lifetimeDropsEarned: Int = 0,
    val lastPerfectDayBonusDateKey: String? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val selectedTheme: String = "forest",
    val onboardingComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val username: String get() = name
    val seeds: Int get() = waterDrops
    val streak: Int get() = currentStreak
}
