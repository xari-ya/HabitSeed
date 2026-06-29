package com.habitseed.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habitseed.app.data.local.AppDatabase
import com.habitseed.app.data.local.dao.FriendDao
import com.habitseed.app.data.local.dao.FriendNudgeDao
import com.habitseed.app.data.local.dao.HabitDao
import com.habitseed.app.data.local.dao.HabitLogDao
import com.habitseed.app.data.local.dao.PlantTypeDao
import com.habitseed.app.data.local.dao.PurchaseDao
import com.habitseed.app.data.local.dao.ShopItemDao
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.dao.UserSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT INTO users (
                        id, name, email, avatarAssetName, joinedAt, waterDrops,
                        currentStreak, bestStreak, selectedTheme, onboardingComplete, createdAt, updatedAt
                    ) VALUES (
                        'local_user', 'Alex', 'alex@example.com', NULL, $now, 1250,
                        0, 0, 'forest', 0, $now, $now
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO user_settings (
                        userId, notificationsEnabled, reminderHour, reminderMinute,
                        darkModeEnabled, soundEnabled, hapticsEnabled
                    ) VALUES (
                        'local_user', 1, 8, 0, 0, 1, 1
                    )
                    """.trimIndent()
                )
                db.execSQL("INSERT INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('succulent', 'Succulent', 'Easygoing starter plant.', 'common', 'plant_succulent', 0, 1)")
                db.execSQL("INSERT INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('bonsai', 'Bonsai Tree', 'Calm and patient.', 'rare', 'plant_succulent', 300, 0)")
                db.execSQL("INSERT INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('venus_flytrap', 'Venus Flytrap', 'Sharp focus energy.', 'rare', 'plant_succulent', 450, 0)")
                db.execSQL("INSERT INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('sakura_bonsai', 'Sakura Bonsai', 'A delicate bloom.', 'epic', 'plant_succulent', 600, 0)")
                db.execSQL("INSERT INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('desert_cactus', 'Desert Cactus', 'Built for consistency.', 'common', 'plant_succulent', 250, 0)")

                db.execSQL("INSERT INTO user_unlocked_plants (userId, plantTypeId, unlockedAt) VALUES ('local_user', 'succulent', $now)")

                db.execSQL("INSERT INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_bonsai', 'Bonsai Tree', 'Shape calm habits over time.', 300, 'PLANT', 'plant_succulent', 'bonsai', NULL, 1)")
                db.execSQL("INSERT INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_venus_flytrap', 'Venus Flytrap', 'A bold reward for tough routines.', 450, 'PLANT', 'plant_succulent', 'venus_flytrap', NULL, 1)")
                db.execSQL("INSERT INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_sakura_bonsai', 'Sakura Bonsai', 'Bloom slowly, beautifully.', 600, 'PLANT', 'plant_succulent', 'sakura_bonsai', NULL, 1)")
                db.execSQL("INSERT INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_desert_cactus', 'Desert Cactus', 'Low drama, high resilience.', 250, 'PLANT', 'plant_succulent', 'desert_cactus', NULL, 1)")

                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Sam', NULL, 8, 'plant_succulent', '2026-06-29', 1)")
                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Maya', NULL, 13, 'plant_succulent', '2026-06-29', 1)")
                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Noah', NULL, 5, 'plant_succulent', '2026-06-28', 1)")
            }
        })
        .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideUserSettingsDao(db: AppDatabase): UserSettingsDao = db.userSettingsDao()

    @Provides
    fun providePlantTypeDao(db: AppDatabase): PlantTypeDao = db.plantTypeDao()

    @Provides
    fun provideHabitDao(db: AppDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitLogDao(db: AppDatabase): HabitLogDao = db.habitLogDao()

    @Provides
    fun provideShopItemDao(db: AppDatabase): ShopItemDao = db.shopItemDao()

    @Provides
    fun providePurchaseDao(db: AppDatabase): PurchaseDao = db.purchaseDao()

    @Provides
    fun provideFriendDao(db: AppDatabase): FriendDao = db.friendDao()

    @Provides
    fun provideFriendNudgeDao(db: AppDatabase): FriendNudgeDao = db.friendNudgeDao()
}
