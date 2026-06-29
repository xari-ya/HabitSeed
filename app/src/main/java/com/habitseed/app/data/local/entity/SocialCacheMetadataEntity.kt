package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_cache_metadata")
data class SocialCacheMetadataEntity(
    @PrimaryKey val cacheKey: String,
    val refreshedAt: Long
)
