package com.habitseed.app.domain.repository

import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.data.auth.AuthUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<UserEntity?>
    fun getSettings(): Flow<UserSettingsEntity?>
    suspend fun insertUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity)
    suspend fun addWaterDrops(amount: Int)
    suspend fun markOnboardingComplete()
    suspend fun updateSettings(settings: UserSettingsEntity)
    suspend fun upsertGoogleUser(authUser: AuthUser): UserEntity
    suspend fun updateLastCloudSyncAt(timestamp: Long, publicProfileSyncHash: String)
    suspend fun clearAllUserData()
}
