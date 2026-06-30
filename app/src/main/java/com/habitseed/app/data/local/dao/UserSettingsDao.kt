package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitseed.app.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    fun getSettings(userId: String = "local_user"): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    suspend fun getSettingsSync(userId: String = "local_user"): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateSettings(settings: UserSettingsEntity)
}
