package com.habitseed.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.SocialSyncRepository
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.notifications.HabitReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
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
    data object ProfileSaved : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler,
    private val authRepository: AuthRepository,
    private val socialSyncRepository: SocialSyncRepository
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
        viewModelScope.launch {
            authRepository.signOut().onFailure { error ->
                _events.emit(
                    ProfileEvent.ShowMessage(
                        error.message ?: "Sign out failed. Please try again."
                    )
                )
                return@launch
            }
            _events.emit(ProfileEvent.Logout)
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
