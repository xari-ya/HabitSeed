package com.habitseed.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.habitseed.app.data.local.dao.FriendDao
import com.habitseed.app.data.local.dao.FriendNudgeDao
import com.habitseed.app.data.local.dao.HabitDao
import com.habitseed.app.data.local.dao.HabitLogDao
import com.habitseed.app.data.local.dao.PlantTypeDao
import com.habitseed.app.data.local.dao.PurchaseDao
import com.habitseed.app.data.local.dao.ShopItemDao
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.dao.UserSettingsDao
import com.habitseed.app.data.local.entity.FriendEntity
import com.habitseed.app.data.local.entity.FriendNudgeEntity
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.entity.PlantTypeEntity
import com.habitseed.app.data.local.entity.PurchaseEntity
import com.habitseed.app.data.local.entity.ShopItemEntity
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.data.local.entity.UserUnlockedPlantEntity

@Database(
    entities = [
        UserEntity::class,
        UserSettingsEntity::class,
        PlantTypeEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        ShopItemEntity::class,
        PurchaseEntity::class,
        UserUnlockedPlantEntity::class,
        FriendEntity::class,
        FriendNudgeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun plantTypeDao(): PlantTypeDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun shopItemDao(): ShopItemDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun friendDao(): FriendDao
    abstract fun friendNudgeDao(): FriendNudgeDao
    
    companion object {
        const val DATABASE_NAME = "habitseed_db"
    }
}
