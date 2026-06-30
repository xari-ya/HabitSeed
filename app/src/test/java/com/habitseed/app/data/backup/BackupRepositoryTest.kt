package com.habitseed.app.data.backup

import android.content.Context
import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.auth.AuthUser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRepositoryTest {
    @Test
    fun restoreDataIfNeeded_whenBackupExists_restoresBackup() = runBlocking {
        val backup = sampleBackup()
        val local = FakeBackupLocalDataSource()
        val remote = FakeBackupRemoteDataSource(backup = backup)
        val repository = FirestoreBackupRepository(
            authRepository = FakeAuthRepository(authUser()),
            localDataSource = local,
            remoteDataSource = remote
        )

        val result = repository.restoreDataIfNeeded()

        assertTrue(result is RestoreResult.Restored)
        assertEquals(123L, (result as RestoreResult.Restored).backedUpAt)
        assertSame(backup, local.restoredBackup)
        assertEquals("firebase-uid", remote.loadedUid)
    }

    @Test
    fun restoreDataIfNeeded_whenNoBackupFound_reportsNoBackup() = runBlocking {
        val repository = FirestoreBackupRepository(
            authRepository = FakeAuthRepository(authUser()),
            localDataSource = FakeBackupLocalDataSource(),
            remoteDataSource = FakeBackupRemoteDataSource(backup = null)
        )

        val result = repository.restoreDataIfNeeded()

        assertEquals(RestoreResult.NoBackupFound, result)
    }

    @Test
    fun restoreDataIfNeeded_whenLocalHabitsExist_skipsWithoutRemoteRead() = runBlocking {
        val local = FakeBackupLocalDataSource(hasLocalHabitData = true)
        val remote = FakeBackupRemoteDataSource(backup = sampleBackup())
        val repository = FirestoreBackupRepository(
            authRepository = FakeAuthRepository(authUser()),
            localDataSource = local,
            remoteDataSource = remote
        )

        val result = repository.restoreDataIfNeeded()

        assertEquals(RestoreResult.SkippedLocalDataPresent, result)
        assertEquals(null, remote.loadedUid)
    }

    @Test
    fun restoreDataIfNeeded_whenRemoteReadFails_reportsFailure() = runBlocking {
        val repository = FirestoreBackupRepository(
            authRepository = FakeAuthRepository(authUser()),
            localDataSource = FakeBackupLocalDataSource(),
            remoteDataSource = FakeBackupRemoteDataSource(
                failure = IllegalStateException("Permission denied")
            )
        )

        val result = repository.restoreDataIfNeeded()

        assertTrue(result is RestoreResult.Failed)
        assertTrue((result as RestoreResult.Failed).message.contains("Permission denied"))
    }

    @Test
    fun restoreDataIfNeeded_whenBackupIsEmpty_reportsFailure() = runBlocking {
        val repository = FirestoreBackupRepository(
            authRepository = FakeAuthRepository(authUser()),
            localDataSource = FakeBackupLocalDataSource(),
            remoteDataSource = FakeBackupRemoteDataSource(backup = FullBackupDto())
        )

        val result = repository.restoreDataIfNeeded()

        assertTrue(result is RestoreResult.Failed)
        assertTrue((result as RestoreResult.Failed).message.contains("empty"))
    }

    @Test
    fun backupData_whenLocalBackupExists_writesRemoteBackup() = runBlocking {
        val backup = sampleBackup()
        val remote = FakeBackupRemoteDataSource()
        val repository = FirestoreBackupRepository(
            authRepository = FakeAuthRepository(authUser()),
            localDataSource = FakeBackupLocalDataSource(backupToCreate = backup),
            remoteDataSource = remote
        )

        val result = repository.backupData()

        assertTrue(result.isSuccess)
        assertEquals("firebase-uid", remote.savedUid)
        assertSame(backup, remote.savedBackup)
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

    private fun sampleBackup(): FullBackupDto {
        return FullBackupDto(
            user = UserBackupDto(
                name = "Gardener",
                joinedAt = 100L,
                createdAt = 100L
            ),
            habits = listOf(
                HabitBackupDto(
                    localId = 1L,
                    name = "Water plants"
                )
            ),
            backedUpAt = 123L
        )
    }
}

private class FakeAuthRepository(
    private val user: AuthUser?
) : AuthRepository {
    override fun currentUser(): AuthUser? = user

    override fun isSignedInWithGoogle(): Boolean = user != null

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> {
        return user?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("No user"))
    }

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
}

private class FakeBackupLocalDataSource(
    private val hasLocalHabitData: Boolean = false,
    private val backupToCreate: FullBackupDto? = null
) : BackupLocalDataSource {
    var restoredBackup: FullBackupDto? = null
        private set

    override suspend fun createBackup(now: Long): FullBackupDto? = backupToCreate

    override suspend fun hasLocalHabitData(): Boolean = hasLocalHabitData

    override suspend fun restoreBackup(backup: FullBackupDto) {
        restoredBackup = backup
    }
}

private class FakeBackupRemoteDataSource(
    private val backup: FullBackupDto? = null,
    private val failure: Throwable? = null
) : BackupRemoteDataSource {
    var loadedUid: String? = null
        private set
    var savedUid: String? = null
        private set
    var savedBackup: FullBackupDto? = null
        private set

    override suspend fun saveBackup(uid: String, backup: FullBackupDto) {
        savedUid = uid
        savedBackup = backup
    }

    override suspend fun loadBackup(uid: String): FullBackupDto? {
        loadedUid = uid
        failure?.let { throw it }
        return backup
    }
}
