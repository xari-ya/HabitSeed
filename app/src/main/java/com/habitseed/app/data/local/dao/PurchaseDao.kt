package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitseed.app.data.local.entity.PurchaseEntity
import com.habitseed.app.data.local.entity.UserUnlockedPlantEntity

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases WHERE userId = :userId AND shopItemId = :shopItemId LIMIT 1")
    suspend fun getPurchase(userId: String, shopItemId: String): PurchaseEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedPlant(unlockedPlant: UserUnlockedPlantEntity): Long
}
