package com.habitseed.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserEntity? = null,
    val settings: UserSettingsEntity? = null
)

sealed interface ProfileEvent {
    data class ShowMessage(val message: String) : ProfileEvent
    data object Logout : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    val uiState: StateFlow<ProfileUiState> = combine(
        userRepository.getUser(),
        userRepository.getSettings()
    ) { user, settings ->
        ProfileUiState(
            user = user,
            settings = settings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun toggleNotifications(enabled: Boolean) = updateSettings {
        copy(notificationsEnabled = enabled)
    }

    fun toggleDarkMode(enabled: Boolean) = updateSettings {
        copy(darkModeEnabled = enabled)
    }

    fun toggleSound(enabled: Boolean) = updateSettings {
        copy(soundEnabled = enabled)
    }

    fun toggleHaptics(enabled: Boolean) = updateSettings {
        copy(hapticsEnabled = enabled)
    }

    fun cycleReminderTime() {
        updateSettings {
            val nextHour = when (reminderHour) {
                8 -> 12
                12 -> 18
                else -> 8
            }
            copy(reminderHour = nextHour, reminderMinute = 0)
        }
    }

    fun onMockAction(label: String) {
        viewModelScope.launch {
            _events.emit(ProfileEvent.ShowMessage("$label is available in demo mode."))
        }
    }

    fun logout() {
        viewModelScope.launch {
            _events.emit(ProfileEvent.Logout)
        }
    }

    private fun updateSettings(transform: UserSettingsEntity.() -> UserSettingsEntity) {
        viewModelScope.launch {
            val currentSettings = uiState.value.settings ?: UserSettingsEntity()
            userRepository.updateSettings(currentSettings.transform())
        }
    }
}
