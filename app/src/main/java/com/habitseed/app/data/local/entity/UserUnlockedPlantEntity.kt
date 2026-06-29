package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_unlocked_plants",
    primaryKeys = ["userId", "plantTypeId"],
    indices = [Index("plantTypeId")],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlantTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserUnlockedPlantEntity(
    val userId: String,
    val plantTypeId: String,
    val unlockedAt: Long
)
