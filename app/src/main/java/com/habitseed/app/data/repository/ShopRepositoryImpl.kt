package com.habitseed.app.data.repository

import androidx.room.withTransaction
import com.habitseed.app.data.local.AppDatabase
import com.habitseed.app.data.local.dao.PurchaseDao
import com.habitseed.app.data.local.dao.ShopItemDao
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.entity.PurchaseEntity
import com.habitseed.app.data.local.entity.ShopItemEntity
import com.habitseed.app.data.local.entity.UserUnlockedPlantEntity
import com.habitseed.app.data.local.model.ShopItemWithStatus
import com.habitseed.app.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShopRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val shopItemDao: ShopItemDao,
    private val purchaseDao: PurchaseDao,
    private val userDao: UserDao
) : ShopRepository {

    override fun getAllShopItems(): Flow<List<ShopItemWithStatus>> {
        return shopItemDao.getAllShopItems()
    }

    override suspend fun insertShopItems(items: List<ShopItemEntity>) {
        shopItemDao.insertShopItems(items)
    }

    override suspend fun updateShopItem(item: ShopItemEntity) {
        shopItemDao.updateShopItem(item)
    }

    override suspend fun purchaseShopItem(userId: String, shopItemId: String): Boolean {
        return try {
            db.withTransaction {
                val user = userDao.getUserById(userId) ?: return@withTransaction false
                val item = shopItemDao.getShopItemById(shopItemId) ?: return@withTransaction false
                if (!item.isActive) return@withTransaction false
                if (purchaseDao.getPurchase(userId, shopItemId) != null) return@withTransaction false
                if (user.waterDrops < item.priceDrops) return@withTransaction false

                val now = System.currentTimeMillis()
                val updatedRows = userDao.addWaterDrops(userId, -item.priceDrops, now)
                if (updatedRows == 0) return@withTransaction false

                val purchaseId = purchaseDao.insertPurchase(
                    PurchaseEntity(
                        userId = userId,
                        shopItemId = shopItemId,
                        pricePaidDrops = item.priceDrops,
                        purchasedAt = now
                    )
                )
                if (purchaseId == -1L) throw PurchaseRollbackException()

                item.linkedPlantTypeId?.let { plantTypeId ->
                    purchaseDao.insertUnlockedPlant(
                        UserUnlockedPlantEntity(
                            userId = userId,
                            plantTypeId = plantTypeId,
                            unlockedAt = now
                        )
                    )
                }
                true
            }
        } catch (_: PurchaseRollbackException) {
            false
        }
    }
}

private class PurchaseRollbackException : RuntimeException()
