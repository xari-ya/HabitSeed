package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.habitseed.app.data.local.entity.FriendNudgeEntity

@Dao
interface FriendNudgeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNudge(nudge: FriendNudgeEntity): Long
}
