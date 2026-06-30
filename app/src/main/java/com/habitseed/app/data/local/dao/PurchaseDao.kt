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

    @Query("SELECT * FROM purchases WHERE userId = :userId")
    suspend fun getAllPurchases(userId: String = "local_user"): List<PurchaseEntity>

    @Query(
        """
        SELECT * FROM user_unlocked_plants
        WHERE userId = :userId AND plantTypeId = :plantTypeId
        LIMIT 1
        """
    )
    suspend fun getUnlockedPlant(userId: String, plantTypeId: String): UserUnlockedPlantEntity?

    @Query("SELECT * FROM user_unlocked_plants WHERE userId = :userId")
    suspend fun getAllUnlockedPlants(userId: String = "local_user"): List<UserUnlockedPlantEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedPlant(unlockedPlant: UserUnlockedPlantEntity): Long
}
