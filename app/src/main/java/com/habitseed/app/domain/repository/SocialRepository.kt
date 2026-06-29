package com.habitseed.app.domain.repository

import com.habitseed.app.data.local.entity.FriendEntity
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun getFriends(): Flow<List<FriendEntity>>
    suspend fun sendNudge(friendId: Long, message: String? = null): Boolean
}
