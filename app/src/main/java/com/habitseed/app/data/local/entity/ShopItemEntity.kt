package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shop_items",
    indices = [Index("linkedPlantTypeId")],
    foreignKeys = [
        ForeignKey(
            entity = PlantTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedPlantTypeId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class ShopItemEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String,
    val description: String? = null,
    val priceDrops: Int,
    val itemType: String,
    val assetName: String,
    val linkedPlantTypeId: String? = null,
    val linkedThemeKey: String? = null,
    val isActive: Boolean = true
) {
    val price: Int get() = priceDrops
    val type: String get() = itemType
}
