package com.habitseed.app.data.local.model

import androidx.room.Embedded
import com.habitseed.app.data.local.entity.ShopItemEntity

data class ShopItemWithStatus(
    @Embedded val item: ShopItemEntity,
    val isPurchased: Boolean,
    val rarity: String
)
