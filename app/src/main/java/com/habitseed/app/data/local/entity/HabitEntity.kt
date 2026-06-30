package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habits",
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
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index("userId"),
        Index("plantTypeId")
    ]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "local_user",
    val name: String,
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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val plantGrowthLevel: Int = 0
) {
    val title: String get() = name
    val frequency: String get() = frequencyType.lowercase().replaceFirstChar { it.uppercase() }
    val plantType: String get() = plantTypeId
}
