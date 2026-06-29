package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarAssetName: String? = null,
    val currentStreak: Int = 0,
    val highestPlantAssetName: String? = null,
    val lastActiveDateKey: String? = null,
    val isMock: Boolean = true
)
