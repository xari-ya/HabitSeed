package com.habitseed.app.data.repository

import com.habitseed.app.data.auth.AuthUser
import com.habitseed.app.data.local.SessionDataCleaner
import com.habitseed.app.data.local.dao.UserDao
import com.habitseed.app.data.local.dao.UserSettingsDao
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryImplTest {

    @Test
    fun replacesSeededPlaceholderWithNameDerivedFromEmail() = runBlocking {
        val userDao = FakeUserDao(
            initialUser = UserEntity(
                name = "Alex",
                email = "alex@example.com"
            )
        )
        val repository = UserRepositoryImpl(
            userDao = userDao,
            userSettingsDao = FakeUserSettingsDao(),
            sessionDataCleaner = FakeSessionDataCleaner()
        )

        val updatedUser = repository.upsertGoogleUser(
            AuthUser(
                uid = "firebase-uid",
                displayName = null,
                email = "john.doe@example.com",
                photoUrl = "https://example.com/john.png",
                isEmailVerified = true
            )
        )

        assertEquals("John Doe", updatedUser.name)
        assertEquals("John Doe", userDao.currentUser?.name)
        assertEquals("https://example.com/john.png", userDao.currentUser?.avatarUrl)
        assertEquals("google", userDao.currentUser?.authProvider)
    }

    @Test
    fun preservesExistingCustomNameOnLaterSignIn() = runBlocking {
        val userDao = FakeUserDao(
            initialUser = UserEntity(
                name = "Kaveesha",
                email = "old@example.com",
                firebaseUid = "firebase-uid",
                authProvider = "google"
            )
        )
        val repository = UserRepositoryImpl(
            userDao = userDao,
            userSettingsDao = FakeUserSettingsDao(),
            sessionDataCleaner = FakeSessionDataCleaner()
        )

        val updatedUser = repository.upsertGoogleUser(
            AuthUser(
                uid = "firebase-uid",
                displayName = null,
                email = "john.doe@example.com",
                photoUrl = null,
                isEmailVerified = true
            )
        )

        assertEquals("Kaveesha", updatedUser.name)
        assertEquals("john.doe@example.com", updatedUser.email)
        assertTrue(updatedUser.onboardingComplete)
    }

    @Test
    fun clearAllUserData_delegatesToSessionCleaner() = runBlocking {
        val cleaner = FakeSessionDataCleaner()
        val repository = UserRepositoryImpl(
            userDao = FakeUserDao(),
            userSettingsDao = FakeUserSettingsDao(),
            sessionDataCleaner = cleaner
        )

        repository.clearAllUserData()

        assertTrue(cleaner.wasCleared)
    }
}

private class FakeUserDao(initialUser: UserEntity? = null) : UserDao {
    private val userFlow = MutableStateFlow(initialUser)
    var currentUser: UserEntity? = initialUser
        private set

    override fun getUser(): Flow<UserEntity?> = userFlow

    override fun observeCurrentUser(): Flow<UserEntity?> = userFlow

    override suspend fun getUserById(userId: String): UserEntity? = currentUser

    override suspend fun insertUser(user: UserEntity) {
        currentUser = user
        userFlow.value = user
    }

    override suspend fun updateUser(user: UserEntity) {
        currentUser = user
        userFlow.value = user
    }

    override suspend fun addWaterDrops(userId: String, amount: Int, updatedAt: Long): Int = 1

    override suspend fun addGardenXp(userId: String, amount: Int, updatedAt: Long): Int = 1

    override suspend fun addWaterDropsAndLifetime(
        userId: String,
        drops: Int,
        updatedAt: Long
    ): Int = 1

    override suspend fun updateLastPerfectDayBonusDate(
        userId: String,
        dateKey: String,
        updatedAt: Long
    ): Int = 1

    override suspend fun markOnboardingComplete(userId: String, updatedAt: Long) = Unit

    override suspend fun updateStreaks(
        userId: String,
        currentStreak: Int,
        bestStreak: Int,
        updatedAt: Long
    ) = Unit

    override suspend fun updateLastCloudSyncAt(
        syncedAt: Long,
        publicProfileSyncHash: String,
        userId: String
    ): Int = 1
}

private class FakeUserSettingsDao : UserSettingsDao {
    override fun getSettings(userId: String): Flow<UserSettingsEntity?> = flowOf(null)

    override suspend fun getSettingsSync(userId: String): UserSettingsEntity? = null

    override suspend fun insertSettings(settings: UserSettingsEntity) = Unit

    override suspend fun updateSettings(settings: UserSettingsEntity) = Unit
}

private class FakeSessionDataCleaner : SessionDataCleaner {
    var wasCleared: Boolean = false
        private set

    override suspend fun clearSessionData() {
        wasCleared = true
    }
}
