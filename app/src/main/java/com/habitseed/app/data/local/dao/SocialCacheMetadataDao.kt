package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitseed.app.data.local.entity.SocialCacheMetadataEntity

@Dao
interface SocialCacheMetadataDao {
    @Query("SELECT * FROM social_cache_metadata WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getMetadata(cacheKey: String): SocialCacheMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SocialCacheMetadataEntity)
}
