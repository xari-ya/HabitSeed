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
        .addMigrations(
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                seedStoreCatalogIfNeeded(db)
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
                seedStoreCatalog(db)

                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Sam', NULL, 8, 'plant_succulent', '2026-06-29', 1)")
                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Maya', NULL, 13, 'plant_succulent', '2026-06-29', 1)")
                db.execSQL("INSERT INTO friends (name, avatarAssetName, currentStreak, highestPlantAssetName, lastActiveDateKey, isMock) VALUES ('Noah', NULL, 5, 'plant_succulent', '2026-06-28', 1)")
            }
        })
        .build()
    }

    private fun seedStoreCatalogIfNeeded(db: SupportSQLiteDatabase) {
        if (isStoreCatalogCurrent(db)) return
        seedStoreCatalog(db)
    }

    private fun isStoreCatalogCurrent(db: SupportSQLiteDatabase): Boolean {
        db.query(
            """
            SELECT
                (SELECT COUNT(*) FROM shop_items
                 WHERE id IN ($ACTIVE_SHOP_ITEM_IDS_SQL) AND isActive = 1) = $ACTIVE_SHOP_ITEM_COUNT
                AND
                (SELECT COUNT(*) FROM plant_types
                 WHERE id IN ($ACTIVE_PLANT_TYPE_IDS_SQL)) = $ACTIVE_PLANT_TYPE_COUNT
                AND NOT EXISTS (
                    SELECT 1 FROM shop_items
                    WHERE id IN ($OBSOLETE_SHOP_ITEM_IDS_SQL) AND isActive = 1
                )
                AND
                (SELECT COUNT(*) FROM user_unlocked_plants
                 WHERE userId = 'local_user'
                   AND plantTypeId IN ($DEFAULT_UNLOCKED_PLANT_TYPE_IDS_SQL)) = $DEFAULT_UNLOCKED_PLANT_TYPE_COUNT
            """.trimIndent()
        ).use { cursor ->
            return cursor.moveToFirst() && cursor.getInt(0) == 1
        }
    }

    private fun seedStoreCatalog(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE shop_items SET isActive = 0 WHERE linkedPlantTypeId IS NOT NULL")
        db.execSQL("UPDATE shop_items SET isActive = 0 WHERE id IN ($OBSOLETE_SHOP_ITEM_IDS_SQL)")

        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('sunflower', 'Sunflower', 'A bright daily starter for steady routines.', 'default', 'sunflower_fully_grown', 0, 1)")
        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('cactus', 'Cactus', 'Steady, resilient growth for consistent habits.', 'default', 'cactus_fully_grown', 0, 1)")
        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('lotus', 'Lotus', 'Calm focus that grows with daily care.', 'default', 'lotus_fully_grown', 0, 1)")
        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('water_lily', 'Water Lily', 'A floating bloom for calm and balanced progress.', 'common', 'water_lily_fully_grown', 300, 0)")
        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('bonsai', 'Bonsai', 'A patient tree for careful, disciplined growth.', 'common', 'bonsai_fully_grown', 350, 0)")
        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('lavender', 'Lavender', 'A soothing bloom for habits that bring calm.', 'rare', 'lavender_fully_grown', 450, 0)")
        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('mushroom_garden', 'Mushroom Garden', 'A whimsical patch for routines with quiet magic.', 'rare', 'mushroom_garden_fully_grown', 500, 0)")
        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('venus_flytrap', 'Venus Flytrap', 'A bold carnivorous plant for high-focus streaks.', 'epic', 'venus_flytrap_fully_grown', 700, 0)")
        db.execSQL("INSERT OR REPLACE INTO plant_types (id, name, description, rarity, assetName, priceDrops, isDefault) VALUES ('sakura_tree', 'Sakura Tree', 'A legendary blossom for your strongest seasons.', 'legendary', 'sakura_tree_fully_grown', 1000, 0)")

        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_sunflower', 'Sunflower', 'A bright default bloom for your garden.', 0, 'PLANT', 'sunflower_fully_grown', 'sunflower', NULL, 1)")
        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_cactus', 'Cactus', 'A hardy default plant for dependable routines.', 0, 'PLANT', 'cactus_fully_grown', 'cactus', NULL, 1)")
        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_lotus', 'Lotus', 'A calm default plant for mindful habits.', 0, 'PLANT', 'lotus_fully_grown', 'lotus', NULL, 1)")
        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_water_lily', 'Water Lily', 'A serene bloom for balanced days.', 300, 'PLANT', 'water_lily_fully_grown', 'water_lily', NULL, 1)")
        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_bonsai', 'Bonsai', 'A patient tree that rewards consistency.', 350, 'PLANT', 'bonsai_fully_grown', 'bonsai', NULL, 1)")
        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_lavender', 'Lavender', 'A fragrant reward for calm, steady habits.', 450, 'PLANT', 'lavender_fully_grown', 'lavender', NULL, 1)")
        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_mushroom_garden', 'Mushroom Garden', 'A rare patch with playful forest energy.', 500, 'PLANT', 'mushroom_garden_fully_grown', 'mushroom_garden', NULL, 1)")
        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_venus_flytrap', 'Venus Flytrap', 'An epic plant for focused, ambitious growth.', 700, 'PLANT', 'venus_flytrap_fully_grown', 'venus_flytrap', NULL, 1)")
        db.execSQL("INSERT OR REPLACE INTO shop_items (id, name, description, priceDrops, itemType, assetName, linkedPlantTypeId, linkedThemeKey, isActive) VALUES ('plant_sakura_tree', 'Sakura Tree', 'A legendary blossom for your finest streaks.', 1000, 'PLANT', 'sakura_tree_fully_grown', 'sakura_tree', NULL, 1)")

        val now = System.currentTimeMillis()
        db.execSQL("INSERT OR IGNORE INTO user_unlocked_plants (userId, plantTypeId, unlockedAt) VALUES ('local_user', 'sunflower', $now)")
        db.execSQL("INSERT OR IGNORE INTO user_unlocked_plants (userId, plantTypeId, unlockedAt) VALUES ('local_user', 'cactus', $now)")
        db.execSQL("INSERT OR IGNORE INTO user_unlocked_plants (userId, plantTypeId, unlockedAt) VALUES ('local_user', 'lotus', $now)")
    }

    private const val ACTIVE_SHOP_ITEM_IDS_SQL =
        "'plant_sunflower', 'plant_cactus', 'plant_lotus', 'plant_water_lily', 'plant_bonsai', 'plant_lavender', 'plant_mushroom_garden', 'plant_venus_flytrap', 'plant_sakura_tree'"
    private const val ACTIVE_SHOP_ITEM_COUNT = 9
    private const val ACTIVE_PLANT_TYPE_IDS_SQL =
        "'sunflower', 'cactus', 'lotus', 'water_lily', 'bonsai', 'lavender', 'mushroom_garden', 'venus_flytrap', 'sakura_tree'"
    private const val ACTIVE_PLANT_TYPE_COUNT = 9
    private const val DEFAULT_UNLOCKED_PLANT_TYPE_IDS_SQL =
        "'sunflower', 'cactus', 'lotus'"
    private const val DEFAULT_UNLOCKED_PLANT_TYPE_COUNT = 3
    private const val OBSOLETE_SHOP_ITEM_IDS_SQL =
        "'plant_starter_fern', 'plant_desert_cactus_new', 'plant_monstera_deliciosa', 'plant_golden_bonsai', 'plant_desert_cactus', 'plant_sakura_bonsai'"

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
