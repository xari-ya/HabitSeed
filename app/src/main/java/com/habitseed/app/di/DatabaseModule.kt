package com.habitseed.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habitseed.app.data.local.AppDatabase
import com.habitseed.app.data.local.dao.CachedFollowingProfileDao
import com.habitseed.app.data.local.dao.CachedLeaderboardProfileDao
import com.habitseed.app.data.local.dao.FriendDao
import com.habitseed.app.data.local.dao.FriendNudgeDao
import com.habitseed.app.data.local.dao.HabitDao
import com.habitseed.app.data.local.dao.HabitLogDao
import com.habitseed.app.data.local.dao.PlantTypeDao
import com.habitseed.app.data.local.dao.PurchaseDao
import com.habitseed.app.data.local.dao.ShopItemDao
import com.habitseed.app.data.local.dao.SocialCacheMetadataDao
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
        .addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                seedStoreCatalog(db)
            }

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

                db.execSQL("INSERT INTO user_unlocked_plants (userId, plantTypeId, unlockedAt) VALUES ('local_user', 'succulent', $now)")

                seedStoreCatalog(db)

                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Sam', NULL, 8, 'plant_succulent', '2026-06-29', 1)")
                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Maya', NULL, 13, 'plant_succulent', '2026-06-29', 1)")
                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Noah', NULL, 5, 'plant_succulent', '2026-06-28', 1)")
            }
        })
        .build()
    }

    private fun seedStoreCatalog(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE shop_items SET isActive = 0 WHERE id IN ('plant_bonsai', 'plant_venus_flytrap', 'plant_sakura_bonsai', 'plant_desert_cactus')")
        db.execSQL("INSERT OR IGNORE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('starter_fern', 'Starter Fern', 'A simple first step for your garden.', 'basic', 'plant_starter_fern', 100, 0)")
        db.execSQL("INSERT OR IGNORE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('desert_cactus', 'Desert Cactus', 'Tough, steady, and perfect for consistency.', 'basic', 'plant_desert_cactus', 150, 0)")
        db.execSQL("INSERT OR IGNORE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('monstera_deliciosa', 'Monstera Deliciosa', 'A lush reward for stronger routines.', 'rare', 'plant_monstera', 350, 0)")
        db.execSQL("INSERT OR IGNORE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('water_lily', 'Water Lily', 'Calm focus floating above the noise.', 'rare', 'plant_water_lily', 500, 0)")
        db.execSQL("INSERT OR IGNORE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('golden_bonsai', 'Golden Bonsai', 'A rare masterpiece for your most disciplined season.', 'epic', 'plant_golden_bonsai', 1000, 0)")

        db.execSQL("INSERT OR IGNORE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_starter_fern', 'Starter Fern', 'A calm beginning for new habits.', 100, 'PLANT', 'plant_starter_fern', 'starter_fern', NULL, 1)")
        db.execSQL("INSERT OR IGNORE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_desert_cactus_new', 'Desert Cactus', 'Hardy progress with low-drama energy.', 150, 'PLANT', 'plant_desert_cactus', 'desert_cactus', NULL, 1)")
        db.execSQL("INSERT OR IGNORE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_monstera_deliciosa', 'Monstera Deliciosa', 'Big leaves for bold routines.', 350, 'PLANT', 'plant_monstera', 'monstera_deliciosa', NULL, 1)")
        db.execSQL("INSERT OR IGNORE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_water_lily', 'Water Lily', 'A serene bloom for balanced days.', 500, 'PLANT', 'plant_water_lily', 'water_lily', NULL, 1)")
        db.execSQL("INSERT OR IGNORE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_golden_bonsai', 'Golden Bonsai', 'An epic tree for your strongest streaks.', 1000, 'PLANT', 'plant_golden_bonsai', 'golden_bonsai', NULL, 1)")
        db.execSQL("UPDATE shop_items SET isActive = 1 WHERE id IN ('plant_starter_fern', 'plant_desert_cactus_new', 'plant_monstera_deliciosa', 'plant_water_lily', 'plant_golden_bonsai')")
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

    @Provides
    fun provideCachedLeaderboardProfileDao(db: AppDatabase): CachedLeaderboardProfileDao =
        db.cachedLeaderboardProfileDao()

    @Provides
    fun provideCachedFollowingProfileDao(db: AppDatabase): CachedFollowingProfileDao =
        db.cachedFollowingProfileDao()

    @Provides
    fun provideSocialCacheMetadataDao(db: AppDatabase): SocialCacheMetadataDao =
        db.socialCacheMetadataDao()
}
