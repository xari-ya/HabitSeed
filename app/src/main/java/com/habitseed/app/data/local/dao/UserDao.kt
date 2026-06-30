package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitseed.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String = "local_user"): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query(
        """
        UPDATE users
        SET waterDrops = waterDrops + :amount,
            updatedAt = :updatedAt
        WHERE id = :userId AND waterDrops + :amount >= 0
        """
    )
    suspend fun addWaterDrops(userId: String, amount: Int, updatedAt: Long): Int

    @Query(
        """
        UPDATE users
        SET gardenXp = gardenXp + :amount,
            updatedAt = :updatedAt
        WHERE id = :userId AND :amount >= 0
        """
    )
    suspend fun addGardenXp(userId: String, amount: Int, updatedAt: Long): Int

    @Query(
        """
        UPDATE users
        SET waterDrops = waterDrops + :drops,
            lifetimeDropsEarned = lifetimeDropsEarned + :drops,
            updatedAt = :updatedAt
        WHERE id = :userId AND :drops >= 0
        """
    )
    suspend fun addWaterDropsAndLifetime(
        userId: String,
        drops: Int,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE users
        SET lastPerfectDayBonusDateKey = :dateKey,
            updatedAt = :updatedAt
        WHERE id = :userId
        """
    )
    suspend fun updateLastPerfectDayBonusDate(
        userId: String,
        dateKey: String,
        updatedAt: Long
    ): Int

    @Query(
        """
        UPDATE users
        SET onboardingComplete = 1,
            updatedAt = :updatedAt
        WHERE id = :userId
        """
    )
    suspend fun markOnboardingComplete(userId: String = "local_user", updatedAt: Long = System.currentTimeMillis())

    @Query(
        """
        UPDATE users
        SET currentStreak = :currentStreak,
            bestStreak = :bestStreak,
            updatedAt = :updatedAt
        WHERE id = :userId
        """
    )
    suspend fun updateStreaks(
        userId: String,
        currentStreak: Int,
        bestStreak: Int,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE users
        SET lastCloudSyncAt = :syncedAt,
            publicProfileSyncHash = :publicProfileSyncHash
        WHERE id = :userId
        """
    )
    suspend fun updateLastCloudSyncAt(
        syncedAt: Long,
        publicProfileSyncHash: String,
        userId: String = "local_user"
    ): Int
}
