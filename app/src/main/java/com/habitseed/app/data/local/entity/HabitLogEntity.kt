package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "dateKey"], unique = true),
        Index(value = ["habitId", "completedAt"]),
        Index("dateKey")
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateKey: String,
    val completedAt: Long? = null,
    val status: String = "COMPLETED",
    val note: String? = null,
    val waterDropsAwarded: Int = 0
) {
    val timestamp: Long get() = completedAt ?: 0L
    val isCompleted: Boolean get() = status == "COMPLETED"
    val notes: String? get() = note
}
