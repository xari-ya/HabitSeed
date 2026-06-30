package com.habitseed.app.domain.auth

import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.backup.BackupRepository
import com.habitseed.app.domain.repository.UserRepository
import javax.inject.Inject

class LogoutCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val backupRepository: BackupRepository,
    private val userRepository: UserRepository
) {
    suspend fun logout(): LogoutResult {
        if (authRepository.currentUser() != null) {
            val backupResult = backupRepository.backupData()
            if (backupResult.isFailure) {
                return LogoutResult.Cancelled(
                    backupResult.exceptionOrNull()?.message
                        ?.takeIf { it.isNotBlank() }
                        ?.let { "Backup failed. Logout cancelled. $it" }
                        ?: "Backup failed. Logout cancelled."
                )
            }
        }

        val signOutResult = authRepository.signOut()
        if (signOutResult.isFailure) {
            return LogoutResult.Cancelled(
                signOutResult.exceptionOrNull()?.message
                    ?: "Sign out failed. Please try again."
            )
        }

        return runCatching {
            userRepository.clearAllUserData()
        }.fold(
            onSuccess = { LogoutResult.Success },
            onFailure = { error ->
                LogoutResult.SuccessWithWarning(
                    error.message ?: "Signed out, but local cleanup failed."
                )
            }
        )
    }
}

sealed interface LogoutResult {
    data object Success : LogoutResult
    data class SuccessWithWarning(val message: String) : LogoutResult
    data class Cancelled(val message: String) : LogoutResult
}
