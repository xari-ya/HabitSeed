package com.habitseed.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.SocialSyncRepository
import com.habitseed.app.domain.auth.LogoutCoordinator
import com.habitseed.app.domain.auth.LogoutResult
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.notifications.HabitReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserEntity? = null,
    val settings: UserSettingsEntity? = null,
    val isLoggingOut: Boolean = false
)

sealed interface ProfileEvent {
    data class ShowMessage(val message: String) : ProfileEvent
    data object Logout : ProfileEvent
    data object ProfileSaved : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler,
    private val socialSyncRepository: SocialSyncRepository,
    private val logoutCoordinator: LogoutCoordinator
) : ViewModel() {

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()
    private val isLoggingOut = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        userRepository.getUser(),
        userRepository.getSettings(),
        isLoggingOut
    ) { user, settings, loggingOut ->
        ProfileUiState(
            user = user,
            settings = settings,
            isLoggingOut = loggingOut
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun toggleNotifications(enabled: Boolean) = updateSettings(rescheduleReminders = true) {
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
        updateSettings(rescheduleReminders = true) {
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

    fun showMessage(message: String) {
        viewModelScope.launch {
            _events.emit(ProfileEvent.ShowMessage(message))
        }
    }

    fun saveProfile(name: String, avatarUrl: String?) {
        viewModelScope.launch {
            val currentUser = uiState.value.user
            if (currentUser == null) {
                _events.emit(ProfileEvent.ShowMessage("Profile is not ready yet."))
                return@launch
            }

            val cleanName = name.trim()
            if (cleanName.isBlank()) {
                _events.emit(ProfileEvent.ShowMessage("Enter a display name."))
                return@launch
            }

            val cleanAvatarUrl = avatarUrl
                ?.trim()
                ?.takeIf { it.isNotBlank() }

            val updatedUser = currentUser.copy(
                name = cleanName,
                avatarUrl = cleanAvatarUrl,
                updatedAt = System.currentTimeMillis()
            )

            userRepository.updateUser(updatedUser)

            socialSyncRepository.syncPublicProfile(PublicProfileSyncReason.PROFILE_EDIT)
                .onFailure { error ->
                    _events.emit(
                        ProfileEvent.ShowMessage(
                            error.message ?: "Profile saved locally. Cloud sync will retry later."
                        )
                    )
                }

            _events.emit(ProfileEvent.ProfileSaved)
        }
    }

    fun logout() {
        if (isLoggingOut.value) return
        viewModelScope.launch {
            isLoggingOut.value = true
            when (val result = logoutCoordinator.logout()) {
                LogoutResult.Success -> {
                    _events.emit(ProfileEvent.Logout)
                }

                is LogoutResult.SuccessWithWarning -> {
                    _events.emit(ProfileEvent.ShowMessage(result.message))
                    _events.emit(ProfileEvent.Logout)
                }

                is LogoutResult.Cancelled -> {
                    _events.emit(ProfileEvent.ShowMessage(result.message))
                }
            }
            isLoggingOut.value = false
        }
    }

    private fun updateSettings(
        rescheduleReminders: Boolean = false,
        transform: UserSettingsEntity.() -> UserSettingsEntity
    ) {
        viewModelScope.launch {
            val currentSettings = uiState.value.settings ?: UserSettingsEntity()
            val updatedSettings = currentSettings.transform()
            userRepository.updateSettings(updatedSettings)
            if (rescheduleReminders) {
                val habits = habitRepository.getAllHabits().first()
                reminderScheduler.syncReminders(settings = updatedSettings, habits = habits)
            }
        }
    }
}
