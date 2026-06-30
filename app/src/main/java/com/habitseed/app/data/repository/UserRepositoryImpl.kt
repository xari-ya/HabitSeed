package com.habitseed.app.data.repository

import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.dao.UserSettingsDao
import com.habitseed.app.data.auth.AuthUser
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.data.local.SessionDataCleaner
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userSettingsDao: UserSettingsDao,
    private val sessionDataCleaner: SessionDataCleaner
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
        val now = System.currentTimeMillis()
        if (amount > 0) {
            userDao.addWaterDropsAndLifetime("local_user", amount, now)
        } else {
            userDao.addWaterDrops("local_user", amount, now)
        }
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
        val preservedExistingName = existingUser?.name
            ?.trim()
            ?.takeIf(::isUserDefinedName)
        val authDisplayName = authUser.displayName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        val emailDerivedName = authUser.email?.let(::deriveNameFromEmail)
        val displayName = preservedExistingName
            ?: authDisplayName
            ?: emailDerivedName
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

    private fun isUserDefinedName(name: String): Boolean {
        return name.isNotBlank() &&
            placeholderNames.none { it.equals(name, ignoreCase = true) }
    }

    private fun deriveNameFromEmail(email: String): String? {
        val localPart = email.substringBefore('@').trim()
        if (localPart.isBlank()) return null

        val segments = localPart
            .split('.', '_', '-', '+')
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val normalizedName = (if (segments.isNotEmpty()) segments else listOf(localPart))
            .joinToString(" ") { segment ->
                segment.lowercase().replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase() else char.toString()
                }
            }
            .trim()

        return normalizedName.takeIf { it.isNotBlank() }
    }

    override suspend fun updateLastCloudSyncAt(timestamp: Long, publicProfileSyncHash: String) {
        userDao.updateLastCloudSyncAt(
            syncedAt = timestamp,
            publicProfileSyncHash = publicProfileSyncHash
        )
    }

    override suspend fun clearAllUserData() {
        sessionDataCleaner.clearSessionData()
    }

    private companion object {
        val placeholderNames = setOf("Alex", "Gardener")
    }
}
