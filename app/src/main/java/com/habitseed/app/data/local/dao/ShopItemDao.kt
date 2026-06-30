package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitseed.app.data.local.entity.ShopItemEntity
import com.habitseed.app.data.local.model.ShopItemWithStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopItemDao {
    @Query(
        """
        SELECT shop_items.*,
               CASE
                   WHEN purchases.id IS NOT NULL OR user_unlocked_plants.userId IS NOT NULL THEN 1
                   ELSE 0
               END AS isPurchased,
               COALESCE(plant_types.rarity, 'common') AS rarity
        FROM shop_items
        LEFT JOIN plant_types
            ON plant_types.id = shop_items.linkedPlantTypeId
        LEFT JOIN purchases
            ON purchases.shopItemId = shop_items.id
           AND purchases.userId = :userId
        LEFT JOIN user_unlocked_plants
            ON user_unlocked_plants.plantTypeId = shop_items.linkedPlantTypeId
           AND user_unlocked_plants.userId = :userId
        WHERE shop_items.isActive = 1
        ORDER BY shop_items.priceDrops ASC
        """
    )
    fun getAllShopItems(userId: String = "local_user"): Flow<List<ShopItemWithStatus>>

    @Query("SELECT * FROM shop_items WHERE id = :shopItemId LIMIT 1")
    suspend fun getShopItemById(shopItemId: String): ShopItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertShopItems(items: List<ShopItemEntity>)

    @Update
    suspend fun updateShopItem(item: ShopItemEntity)
}
