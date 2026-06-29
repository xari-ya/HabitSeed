package com.habitseed.app.data.repository

import com.habitseed.app.data.local.dao.FriendDao
import com.habitseed.app.data.local.dao.FriendNudgeDao
import com.habitseed.app.data.local.entity.FriendEntity
import com.habitseed.app.data.local.entity.FriendNudgeEntity
import com.habitseed.app.domain.repository.SocialRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SocialRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao,
    private val friendNudgeDao: FriendNudgeDao
) : SocialRepository {

    override fun getFriends(): Flow<List<FriendEntity>> = friendDao.getFriends()

    override suspend fun sendNudge(friendId: Long, message: String?): Boolean {
        return friendNudgeDao.insertNudge(
            FriendNudgeEntity(
                friendId = friendId,
                sentAt = System.currentTimeMillis(),
                message = message
            )
        ) > 0
    }
}
