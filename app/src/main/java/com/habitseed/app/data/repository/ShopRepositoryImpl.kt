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
                purchaseShopItemInTransaction(userId = userId, shopItemId = shopItemId)
            }
        } catch (_: PurchaseRollbackException) {
            false
        }
    }

    internal suspend fun purchaseShopItemInTransaction(
        userId: String,
        shopItemId: String,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        return runShopPurchaseTransaction(
            userId = userId,
            shopItemId = shopItemId,
            now = now,
            shopItemDao = shopItemDao,
            purchaseDao = purchaseDao,
            userDao = userDao
        )
    }
}

private class PurchaseRollbackException : RuntimeException()

internal suspend fun runShopPurchaseTransaction(
    userId: String,
    shopItemId: String,
    now: Long,
    shopItemDao: ShopItemDao,
    purchaseDao: PurchaseDao,
    userDao: UserDao
): Boolean {
    val user = userDao.getUserById(userId) ?: return false
    val item = shopItemDao.getShopItemById(shopItemId) ?: return false
    if (!item.isActive) return false
    if (purchaseDao.getPurchase(userId, shopItemId) != null) return false
    if (
        item.linkedPlantTypeId != null &&
        purchaseDao.getUnlockedPlant(userId, item.linkedPlantTypeId) != null
    ) {
        return false
    }
    if (user.waterDrops < item.priceDrops) return false

    val updatedRows = userDao.addWaterDrops(userId, -item.priceDrops, now)
    if (updatedRows == 0) return false

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
    return true
}
