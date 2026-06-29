package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.habitseed.app.data.local.entity.CachedFollowingProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedFollowingProfileDao {
    @Query("SELECT * FROM cached_following_profiles ORDER BY followedAt DESC")
    fun observeProfiles(): Flow<List<CachedFollowingProfileEntity>>

    @Query("DELETE FROM cached_following_profiles")
    suspend fun clear()

    @Query("DELETE FROM cached_following_profiles WHERE targetUid = :targetUid")
    suspend fun deleteByTargetUid(targetUid: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CachedFollowingProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<CachedFollowingProfileEntity>)

    @Transaction
    suspend fun replaceAll(profiles: List<CachedFollowingProfileEntity>) {
        clear()
        insertProfiles(profiles)
    }
}
