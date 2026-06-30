package com.habitseed.app.data.repository

import com.habitseed.app.data.local.dao.PurchaseDao
import com.habitseed.app.data.local.dao.ShopItemDao
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.entity.PurchaseEntity
import com.habitseed.app.data.local.entity.ShopItemEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserUnlockedPlantEntity
import com.habitseed.app.data.local.model.ShopItemWithStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopRepositoryImplTest {
    @Test
    fun purchaseAffordablePlant_unlocksPlantAndDoesNotDecreaseGardenXp() = runBlocking {
        val userDao = FakeShopUserDao(
            UserEntity(name = "Gardener", waterDrops = 500, gardenXp = 260)
        )
        val shopItemDao = FakeShopItemDao(
            shopItem("plant_lavender", priceDrops = 450, linkedPlantTypeId = "lavender")
        )
        val purchaseDao = FakePurchaseDao()

        val purchased = runShopPurchaseTransaction(
            userId = "local_user",
            shopItemId = "plant_lavender",
            now = 100L,
            shopItemDao = shopItemDao,
            purchaseDao = purchaseDao,
            userDao = userDao
        )

        assertTrue(purchased)
        assertEquals(50, userDao.currentUser?.waterDrops)
        assertEquals(260, userDao.currentUser?.gardenXp)
        assertNotNull(purchaseDao.getPurchase("local_user", "plant_lavender"))
        assertNotNull(purchaseDao.getUnlockedPlant("local_user", "lavender"))
    }

    @Test
    fun purchaseDuplicatePlant_doesNotDoubleDeduct() = runBlocking {
        val userDao = FakeShopUserDao(
            UserEntity(name = "Gardener", waterDrops = 800, gardenXp = 260)
        )
        val shopItemDao = FakeShopItemDao(
            shopItem("plant_water_lily", priceDrops = 300, linkedPlantTypeId = "water_lily")
        )
        val purchaseDao = FakePurchaseDao()

        val firstPurchase = runShopPurchaseTransaction(
            userId = "local_user",
            shopItemId = "plant_water_lily",
            now = 100L,
            shopItemDao = shopItemDao,
            purchaseDao = purchaseDao,
            userDao = userDao
        )
        val duplicatePurchase = runShopPurchaseTransaction(
            userId = "local_user",
            shopItemId = "plant_water_lily",
            now = 200L,
            shopItemDao = shopItemDao,
            purchaseDao = purchaseDao,
            userDao = userDao
        )

        assertTrue(firstPurchase)
        assertFalse(duplicatePurchase)
        assertEquals(500, userDao.currentUser?.waterDrops)
        assertEquals(260, userDao.currentUser?.gardenXp)
        assertEquals(1, purchaseDao.purchases.size)
    }

    @Test
    fun purchaseInsufficientDrops_doesNothing() = runBlocking {
        val userDao = FakeShopUserDao(
            UserEntity(name = "Gardener", waterDrops = 100, gardenXp = 260)
        )
        val shopItemDao = FakeShopItemDao(
            shopItem("plant_sakura_tree", priceDrops = 1000, linkedPlantTypeId = "sakura_tree")
        )
        val purchaseDao = FakePurchaseDao()

        val purchased = runShopPurchaseTransaction(
            userId = "local_user",
            shopItemId = "plant_sakura_tree",
            now = 100L,
            shopItemDao = shopItemDao,
            purchaseDao = purchaseDao,
            userDao = userDao
        )

        assertFalse(purchased)
        assertEquals(100, userDao.currentUser?.waterDrops)
        assertEquals(260, userDao.currentUser?.gardenXp)
        assertEquals(0, purchaseDao.purchases.size)
        assertEquals(0, purchaseDao.unlockedPlants.size)
    }

    private fun shopItem(
        id: String,
        priceDrops: Int,
        linkedPlantTypeId: String
    ): ShopItemEntity {
        return ShopItemEntity(
            id = id,
            name = linkedPlantTypeId.replace("_", " "),
            priceDrops = priceDrops,
            itemType = "PLANT",
            assetName = linkedPlantTypeId,
            linkedPlantTypeId = linkedPlantTypeId
        )
    }
}

private class FakeShopItemDao(vararg items: ShopItemEntity) : ShopItemDao {
    private val items = items.associateBy { it.id }.toMutableMap()

    override fun getAllShopItems(userId: String): Flow<List<ShopItemWithStatus>> {
        return flowOf(items.values.map { ShopItemWithStatus(item = it, isPurchased = false, rarity = "common") })
    }

    override suspend fun getShopItemById(shopItemId: String): ShopItemEntity? = items[shopItemId]

    override suspend fun insertShopItems(items: List<ShopItemEntity>) {
        items.forEach { item -> this.items.putIfAbsent(item.id, item) }
    }

    override suspend fun updateShopItem(item: ShopItemEntity) {
        items[item.id] = item
    }
}

private class FakePurchaseDao : PurchaseDao {
    val purchases = mutableListOf<PurchaseEntity>()
    val unlockedPlants = mutableListOf<UserUnlockedPlantEntity>()

    override suspend fun getPurchase(userId: String, shopItemId: String): PurchaseEntity? {
        return purchases.firstOrNull { it.userId == userId && it.shopItemId == shopItemId }
    }

    override suspend fun getAllPurchases(userId: String): List<PurchaseEntity> {
        return purchases.filter { it.userId == userId }
    }

    override suspend fun getUnlockedPlant(userId: String, plantTypeId: String): UserUnlockedPlantEntity? {
        return unlockedPlants.firstOrNull { it.userId == userId && it.plantTypeId == plantTypeId }
    }

    override suspend fun getAllUnlockedPlants(userId: String): List<UserUnlockedPlantEntity> {
        return unlockedPlants.filter { it.userId == userId }
    }

    override suspend fun insertPurchase(purchase: PurchaseEntity): Long {
        if (getPurchase(purchase.userId, purchase.shopItemId) != null) return -1L
        val id = (purchases.maxOfOrNull { it.id } ?: 0L) + 1L
        purchases += purchase.copy(id = id)
        return id
    }

    override suspend fun insertUnlockedPlant(unlockedPlant: UserUnlockedPlantEntity): Long {
        if (getUnlockedPlant(unlockedPlant.userId, unlockedPlant.plantTypeId) != null) return -1L
        unlockedPlants += unlockedPlant
        return unlockedPlants.size.toLong()
    }
}

private class FakeShopUserDao(initialUser: UserEntity?) : UserDao {
    private val userFlow = MutableStateFlow(initialUser)
    var currentUser: UserEntity? = initialUser
        private set

    override fun getUser(): Flow<UserEntity?> = userFlow

    override fun observeCurrentUser(): Flow<UserEntity?> = userFlow

    override suspend fun getUserById(userId: String): UserEntity? = currentUser?.takeIf { it.id == userId }

    override suspend fun insertUser(user: UserEntity) {
        currentUser = user
        userFlow.value = user
    }

    override suspend fun updateUser(user: UserEntity) {
        currentUser = user
        userFlow.value = user
    }

    override suspend fun addWaterDrops(userId: String, amount: Int, updatedAt: Long): Int {
        val user = currentUser ?: return 0
        if (user.id != userId || user.waterDrops + amount < 0) return 0
        currentUser = user.copy(waterDrops = user.waterDrops + amount, updatedAt = updatedAt)
        userFlow.value = currentUser
        return 1
    }

    override suspend fun addGardenXp(userId: String, amount: Int, updatedAt: Long): Int {
        val user = currentUser ?: return 0
        if (user.id != userId || amount < 0) return 0
        currentUser = user.copy(gardenXp = user.gardenXp + amount, updatedAt = updatedAt)
        userFlow.value = currentUser
        return 1
    }

    override suspend fun addWaterDropsAndLifetime(userId: String, drops: Int, updatedAt: Long): Int {
        val user = currentUser ?: return 0
        if (user.id != userId || drops < 0) return 0
        currentUser = user.copy(
            waterDrops = user.waterDrops + drops,
            lifetimeDropsEarned = user.lifetimeDropsEarned + drops,
            updatedAt = updatedAt
        )
        userFlow.value = currentUser
        return 1
    }

    override suspend fun updateLastPerfectDayBonusDate(
        userId: String,
        dateKey: String,
        updatedAt: Long
    ): Int {
        val user = currentUser ?: return 0
        if (user.id != userId) return 0
        currentUser = user.copy(lastPerfectDayBonusDateKey = dateKey, updatedAt = updatedAt)
        userFlow.value = currentUser
        return 1
    }

    override suspend fun markOnboardingComplete(userId: String, updatedAt: Long) = Unit

    override suspend fun updateStreaks(
        userId: String,
        currentStreak: Int,
        bestStreak: Int,
        updatedAt: Long
    ) = Unit

    override suspend fun updateLastCloudSyncAt(
        syncedAt: Long,
        publicProfileSyncHash: String,
        userId: String
    ): Int = 1
}
