package com.habitseed.app.data.social

import com.habitseed.app.data.social.dto.FollowingDto
import com.habitseed.app.data.social.dto.NudgeDto
import com.habitseed.app.data.social.dto.PublicProfileDto

interface SocialRemoteDataSource {
    suspend fun upsertPublicProfile(profile: PublicProfileDto)
    suspend fun getPublicProfile(uid: String): PublicProfileDto?
    suspend fun getLeaderboard(limit: Long = 20): List<PublicProfileDto>
    suspend fun getFollowing(currentUid: String): List<FollowingDto>
    suspend fun followUser(currentUid: String, following: FollowingDto)
    suspend fun unfollowUser(currentUid: String, targetUid: String)
    suspend fun sendNudge(nudge: NudgeDto)
    suspend fun getUnreadNudges(toUid: String, limit: Long = 20): List<NudgeDto>
    suspend fun markNudgeRead(nudgeId: String, readAt: Long)
}
