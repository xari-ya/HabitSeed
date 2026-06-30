package com.habitseed.app.domain.auth

import android.content.Context
import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.auth.AuthUser
import com.habitseed.app.data.backup.BackupRepository
import com.habitseed.app.data.backup.RestoreResult
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogoutCoordinatorTest {
    @Test
    fun logout_whenBackupFails_cancelsBeforeSignOutOrCleanup() = runBlocking {
        val authRepository = FakeAuthRepository(currentUser = authUser())
        val backupRepository = FakeBackupRepository(
            backupResult = Result.failure(IllegalStateException("Backup denied"))
        )
        val userRepository = FakeLogoutUserRepository()
        val coordinator = LogoutCoordinator(authRepository, backupRepository, userRepository)

        val result = coordinator.logout()

        assertTrue(result is LogoutResult.Cancelled)
        assertEquals(
            "Backup failed. Logout cancelled. Backup denied",
            (result as LogoutResult.Cancelled).message
        )
        assertEquals(1, backupRepository.backupCalls)
        assertEquals(0, authRepository.signOutCalls)
        assertEquals(0, userRepository.clearCalls)
    }

    @Test
    fun logout_whenBackupAndSignOutSucceed_clearsSessionData() = runBlocking {
        val authRepository = FakeAuthRepository(currentUser = authUser())
        val backupRepository = FakeBackupRepository()
        val userRepository = FakeLogoutUserRepository()
        val coordinator = LogoutCoordinator(authRepository, backupRepository, userRepository)

        val result = coordinator.logout()

        assertEquals(LogoutResult.Success, result)
        assertEquals(1, backupRepository.backupCalls)
        assertEquals(1, authRepository.signOutCalls)
        assertEquals(1, userRepository.clearCalls)
    }

    @Test
    fun logout_whenSignOutFails_doesNotClearSessionData() = runBlocking {
        val authRepository = FakeAuthRepository(
            currentUser = authUser(),
            signOutResult = Result.failure(IllegalStateException("Sign out failed"))
        )
        val backupRepository = FakeBackupRepository()
        val userRepository = FakeLogoutUserRepository()
        val coordinator = LogoutCoordinator(authRepository, backupRepository, userRepository)

        val result = coordinator.logout()

        assertTrue(result is LogoutResult.Cancelled)
        assertEquals("Sign out failed", (result as LogoutResult.Cancelled).message)
        assertEquals(1, backupRepository.backupCalls)
        assertEquals(1, authRepository.signOutCalls)
        assertEquals(0, userRepository.clearCalls)
    }

    @Test
    fun logout_withoutSignedInFirebaseUser_skipsBackupAndSignsOut() = runBlocking {
        val authRepository = FakeAuthRepository(currentUser = null)
        val backupRepository = FakeBackupRepository()
        val userRepository = FakeLogoutUserRepository()
        val coordinator = LogoutCoordinator(authRepository, backupRepository, userRepository)

        val result = coordinator.logout()

        assertEquals(LogoutResult.Success, result)
        assertEquals(0, backupRepository.backupCalls)
        assertEquals(1, authRepository.signOutCalls)
        assertEquals(1, userRepository.clearCalls)
    }

    @Test
    fun logout_whenCleanupFails_stillCompletesWithWarning() = runBlocking {
        val authRepository = FakeAuthRepository(currentUser = authUser())
        val backupRepository = FakeBackupRepository()
        val userRepository = FakeLogoutUserRepository(
            clearFailure = IllegalStateException("Cleanup failed")
        )
        val coordinator = LogoutCoordinator(authRepository, backupRepository, userRepository)

        val result = coordinator.logout()

        assertTrue(result is LogoutResult.SuccessWithWarning)
        assertEquals("Cleanup failed", (result as LogoutResult.SuccessWithWarning).message)
        assertEquals(1, backupRepository.backupCalls)
        assertEquals(1, authRepository.signOutCalls)
        assertEquals(1, userRepository.clearCalls)
    }

    private fun authUser(): AuthUser {
        return AuthUser(
            uid = "firebase-uid",
            displayName = "Gardener",
            email = "gardener@example.com",
            photoUrl = null,
            isEmailVerified = true
        )
    }
}

private class FakeAuthRepository(
    private val currentUser: AuthUser?,
    private val signOutResult: Result<Unit> = Result.success(Unit)
) : AuthRepository {
    var signOutCalls: Int = 0
        private set

    override fun currentUser(): AuthUser? = currentUser

    override fun isSignedInWithGoogle(): Boolean = currentUser != null

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> {
        return currentUser?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No user"))
    }

    override suspend fun signOut(): Result<Unit> {
        signOutCalls += 1
        return signOutResult
    }
}

private class FakeBackupRepository(
    private val backupResult: Result<Unit> = Result.success(Unit)
) : BackupRepository {
    var backupCalls: Int = 0
        private set

    override suspend fun backupData(): Result<Unit> {
        backupCalls += 1
        return backupResult
    }

    override suspend fun restoreDataIfNeeded(): RestoreResult = RestoreResult.NoBackupFound
}

private class FakeLogoutUserRepository(
    private val clearFailure: Throwable? = null
) : UserRepository {
    var clearCalls: Int = 0
        private set

    override fun getUser(): Flow<UserEntity?> = flowOf(null)

    override fun getSettings(): Flow<UserSettingsEntity?> = flowOf(null)

    override suspend fun insertUser(user: UserEntity) = Unit

    override suspend fun updateUser(user: UserEntity) = Unit

    override suspend fun addWaterDrops(amount: Int) = Unit

    override suspend fun markOnboardingComplete() = Unit

    override suspend fun updateSettings(settings: UserSettingsEntity) = Unit

    override suspend fun upsertGoogleUser(authUser: AuthUser): UserEntity {
        return UserEntity(name = authUser.displayName ?: "Gardener")
    }

    override suspend fun updateLastCloudSyncAt(timestamp: Long, publicProfileSyncHash: String) = Unit

    override suspend fun clearAllUserData() {
        clearCalls += 1
        clearFailure?.let { throw it }
    }
}
