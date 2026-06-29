package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_types")
data class PlantTypeEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String,
    val description: String? = null,
    val rarity: String = "common",
    val assetName: String,
    val priceDrops: Int = 0,
    val isDefault: Boolean = false
)
