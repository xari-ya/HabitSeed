package com.habitseed.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.habitseed.app.data.social.dto.FollowingDto

@Entity(tableName = "cached_following_profiles")
data class CachedFollowingProfileEntity(
    @PrimaryKey val targetUid: String,
    val displayNameSnapshot: String,
    val photoUrlSnapshot: String?,
    val followedAt: Long,
    val cachedAt: Long
) {
    fun toDto(): FollowingDto {
        return FollowingDto(
            targetUid = targetUid,
            displayNameSnapshot = displayNameSnapshot,
            photoUrlSnapshot = photoUrlSnapshot,
            followedAt = followedAt
        )
    }

    companion object {
        fun fromDto(following: FollowingDto, cachedAt: Long): CachedFollowingProfileEntity {
            return CachedFollowingProfileEntity(
                targetUid = following.targetUid,
                displayNameSnapshot = following.displayNameSnapshot,
                photoUrlSnapshot = following.photoUrlSnapshot,
                followedAt = following.followedAt,
                cachedAt = cachedAt
            )
        }
    }
}
