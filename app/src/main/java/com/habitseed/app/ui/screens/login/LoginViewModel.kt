package com.habitseed.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null
        )
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null
        )
    }

    fun onForgotPasswordClicked() {
        viewModelScope.launch {
            _events.emit(LoginEvent.ShowMessage("Password recovery is not connected in this demo."))
        }
    }

    fun onAppleLoginClicked() {
        viewModelScope.launch {
            _events.emit(LoginEvent.ShowMessage("Apple sign-in is not connected in this demo."))
        }
    }

    fun onGoogleLoginClicked() {
        viewModelScope.launch {
            _events.emit(LoginEvent.ShowMessage("Google sign-in is not connected in this demo."))
        }
    }

    fun submitLogin() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val emailError = when {
            email.isBlank() -> "Enter your email."
            !isValidEmail(email) -> "Enter a valid email."
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Enter your password."
            password.length < 4 -> "Password must be at least 4 characters."
            else -> null
        }

        if (emailError != null || passwordError != null) {
            _uiState.value = _uiState.value.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val now = System.currentTimeMillis()
            val existingUser = userRepository.getUser().first()
            val displayName = deriveDisplayName(email)
            val updatedUser = (existingUser ?: UserEntity(name = displayName)).copy(
                name = displayName,
                email = email,
                onboardingComplete = true,
                updatedAt = now
            )

            if (existingUser == null) {
                userRepository.insertUser(updatedUser.copy(createdAt = now, joinedAt = now))
            } else {
                userRepository.updateUser(updatedUser)
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
            _events.emit(LoginEvent.NavigateHome)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.substringAfter("@").contains(".")
    }

    private fun deriveDisplayName(email: String): String {
        val rawName = email.substringBefore("@").ifBlank { "Alex" }
        return rawName
            .split('.', '_', '-')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { character ->
                    if (character.isLowerCase()) character.titlecase() else character.toString()
                }
            }
            .ifBlank { "Alex" }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
)

sealed interface LoginEvent {
    data object NavigateHome : LoginEvent
    data class ShowMessage(val message: String) : LoginEvent
}
