package com.habitseed.app.data.repository

import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.dao.UserSettingsDao
import com.habitseed.app.data.auth.AuthUser
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userSettingsDao: UserSettingsDao
) : UserRepository {

    override fun getUser(): Flow<UserEntity?> {
        return userDao.getUser()
    }

    override fun getSettings(): Flow<UserSettingsEntity?> {
        return userSettingsDao.getSettings()
    }

    override suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    override suspend fun addWaterDrops(amount: Int) {
        userDao.addWaterDrops("local_user", amount, System.currentTimeMillis())
    }

    override suspend fun markOnboardingComplete() {
        userDao.markOnboardingComplete(updatedAt = System.currentTimeMillis())
    }

    override suspend fun updateSettings(settings: UserSettingsEntity) {
        userSettingsDao.insertSettings(settings)
    }

    override suspend fun upsertGoogleUser(authUser: AuthUser): UserEntity {
        val now = System.currentTimeMillis()
        val existingUser = userDao.getUserById("local_user")
        val authDisplayName = authUser.displayName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        val displayName = existingUser?.name
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: authDisplayName
            ?: "Gardener"
        val avatarUrl = existingUser?.avatarUrl
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: authUser.photoUrl

        val updatedUser = (existingUser ?: UserEntity(
            name = displayName,
            joinedAt = now,
            createdAt = now
        )).copy(
            name = displayName,
            email = authUser.email,
            firebaseUid = authUser.uid,
            authProvider = "google",
            avatarUrl = avatarUrl,
            emailVerified = authUser.isEmailVerified,
            lastLoginAt = now,
            onboardingComplete = true,
            updatedAt = now
        )

        if (existingUser == null) {
            userDao.insertUser(updatedUser)
        } else {
            userDao.updateUser(updatedUser)
        }
        return updatedUser
    }

    override suspend fun updateLastCloudSyncAt(timestamp: Long, publicProfileSyncHash: String) {
        userDao.updateLastCloudSyncAt(
            syncedAt = timestamp,
            publicProfileSyncHash = publicProfileSyncHash
        )
    }
}
