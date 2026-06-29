package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.habitseed.app.data.local.entity.CachedLeaderboardProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedLeaderboardProfileDao {
    @Query("SELECT * FROM cached_leaderboard_profiles ORDER BY rank ASC")
    fun observeProfiles(): Flow<List<CachedLeaderboardProfileEntity>>

    @Query("DELETE FROM cached_leaderboard_profiles")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<CachedLeaderboardProfileEntity>)

    @Transaction
    suspend fun replaceAll(profiles: List<CachedLeaderboardProfileEntity>) {
        clear()
        insertProfiles(profiles)
    }
}
