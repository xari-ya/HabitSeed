package com.habitseed.app.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.backup.BackupRepository
import com.habitseed.app.data.backup.RestoreResult
import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.SocialSyncRepository
import com.habitseed.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val socialSyncRepository: SocialSyncRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onGoogleLoginClicked(context: Context) {
        if (_uiState.value.authState == AuthState.Loading) return

        viewModelScope.launch {
            _uiState.value = LoginUiState(authState = AuthState.Loading)
            val authResult = authRepository.signInWithGoogle(context)

            if (authResult.isFailure) {
                val message = authResult.exceptionOrNull()?.message
                    ?: "Google sign-in failed. Please try again."
                _uiState.value = LoginUiState(
                    authState = AuthState.Error,
                    errorMessage = message
                )
                _events.emit(LoginEvent.ShowMessage(message))
                return@launch
            }

            val authUser = authResult.getOrThrow()
            val restoreResult = backupRepository.restoreDataIfNeeded()
            if (restoreResult is RestoreResult.Failed) {
                _uiState.value = LoginUiState(
                    authState = AuthState.Error,
                    errorMessage = restoreResult.message
                )
                _events.emit(LoginEvent.ShowMessage(restoreResult.message))
                return@launch
            }

            runCatching {
                userRepository.upsertGoogleUser(authUser)
            }.onSuccess {
                viewModelScope.launch {
                    socialSyncRepository.syncPublicProfile(PublicProfileSyncReason.SIGN_IN)
                }
                _uiState.value = LoginUiState(authState = AuthState.Success)
                _events.emit(LoginEvent.NavigateHome)
            }.onFailure { error ->
                val message = error.message ?: "Could not save your profile locally."
                _uiState.value = LoginUiState(
                    authState = AuthState.Error,
                    errorMessage = message
                )
                _events.emit(LoginEvent.ShowMessage(message))
            }
        }
    }
}

enum class AuthState {
    Idle,
    Loading,
    Success,
    Error
}

data class LoginUiState(
    val authState: AuthState = AuthState.Idle,
    val errorMessage: String? = null
) {
    val isLoading: Boolean get() = authState == AuthState.Loading
}

sealed interface LoginEvent {
    data object NavigateHome : LoginEvent
    data class ShowMessage(val message: String) : LoginEvent
}
