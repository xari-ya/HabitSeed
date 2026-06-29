package com.habitseed.app.domain.repository

import com.habitseed.app.data.local.entity.ShopItemEntity
import com.habitseed.app.data.local.model.ShopItemWithStatus
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    fun getAllShopItems(): Flow<List<ShopItemWithStatus>>
    suspend fun insertShopItems(items: List<ShopItemEntity>)
    suspend fun updateShopItem(item: ShopItemEntity)
    suspend fun purchaseShopItem(userId: String, shopItemId: String): Boolean
}
