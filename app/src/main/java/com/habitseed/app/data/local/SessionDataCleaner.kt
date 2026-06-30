package com.habitseed.app.data.local

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SessionDataCleaner {
    suspend fun clearSessionData()
}

class RoomSessionDataCleaner @Inject constructor(
    private val db: AppDatabase
) : SessionDataCleaner {
    override suspend fun clearSessionData() {
        withContext(Dispatchers.IO) {
            db.withTransaction {
                val writableDb = db.openHelper.writableDatabase
                writableDb.execSQL("DELETE FROM friend_nudges")
                writableDb.execSQL("DELETE FROM cached_leaderboard_profiles")
                writableDb.execSQL("DELETE FROM cached_following_profiles")
                writableDb.execSQL("DELETE FROM social_cache_metadata")
                writableDb.execSQL("DELETE FROM habit_logs")
                writableDb.execSQL("DELETE FROM habits")
                writableDb.execSQL("DELETE FROM user_settings")
                writableDb.execSQL("DELETE FROM purchases")
                writableDb.execSQL("DELETE FROM user_unlocked_plants")
                writableDb.execSQL("DELETE FROM users")
            }
        }
    }
}
