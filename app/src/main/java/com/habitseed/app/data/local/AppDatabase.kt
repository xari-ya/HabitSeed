package com.habitseed.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.habitseed.app.data.local.entity.CachedFollowingProfileEntity
import com.habitseed.app.data.local.entity.CachedLeaderboardProfileEntity
import com.habitseed.app.data.local.entity.FriendEntity
import com.habitseed.app.data.local.entity.FriendNudgeEntity
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.HabitLogEntity
import com.habitseed.app.data.local.entity.PlantTypeEntity
import com.habitseed.app.data.local.entity.PurchaseEntity
import com.habitseed.app.data.local.entity.ShopItemEntity
import com.habitseed.app.data.local.entity.SocialCacheMetadataEntity
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
        FriendNudgeEntity::class,
        CachedLeaderboardProfileEntity::class,
        CachedFollowingProfileEntity::class,
        SocialCacheMetadataEntity::class
    ],
    version = 4,
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
    abstract fun cachedLeaderboardProfileDao(): CachedLeaderboardProfileDao
    abstract fun cachedFollowingProfileDao(): CachedFollowingProfileDao
    abstract fun socialCacheMetadataDao(): SocialCacheMetadataDao
    
    companion object {
        const val DATABASE_NAME = "habitseed_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN firebaseUid TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN authProvider TEXT NOT NULL DEFAULT 'local'")
                db.execSQL("ALTER TABLE users ADD COLUMN avatarUrl TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN emailVerified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE users ADD COLUMN lastLoginAt INTEGER")
                db.execSQL("ALTER TABLE users ADD COLUMN lastCloudSyncAt INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN publicProfileSyncHash TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cached_leaderboard_profiles (
                        uid TEXT NOT NULL PRIMARY KEY,
                        displayName TEXT NOT NULL,
                        photoUrl TEXT,
                        currentStreak INTEGER NOT NULL,
                        bestStreak INTEGER NOT NULL,
                        fullyGrownPlants INTEGER NOT NULL,
                        weeklyCompletionRate INTEGER NOT NULL,
                        totalCompletions INTEGER NOT NULL,
                        lastActiveDateKey TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        rank INTEGER NOT NULL,
                        cachedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cached_following_profiles (
                        targetUid TEXT NOT NULL PRIMARY KEY,
                        displayNameSnapshot TEXT NOT NULL,
                        photoUrlSnapshot TEXT,
                        followedAt INTEGER NOT NULL,
                        cachedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS social_cache_metadata (
                        cacheKey TEXT NOT NULL PRIMARY KEY,
                        refreshedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
