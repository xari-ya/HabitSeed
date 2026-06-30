package com.habitseed.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.backup.BackupRepository
import com.habitseed.app.data.backup.RestoreResult
import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.SocialSyncRepository
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.notifications.HabitReminderScheduler
import com.habitseed.app.notifications.HabitSeedNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val authRepository: AuthRepository,
    private val socialSyncRepository: SocialSyncRepository,
    private val reminderScheduler: HabitReminderScheduler,
    private val notifier: HabitSeedNotifier,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val localUser = userRepository.getUser().first()
            syncReminderSchedules()
            val authUser = authRepository.currentUser()
            var canEnterHome = authUser != null
            var forceLogin = false
            if (authUser != null) {
                val shouldMergeAuthUser = localUser == null ||
                    localUser.firebaseUid != authUser.uid ||
                    localUser.authProvider != "google"
                if (shouldMergeAuthUser) {
                    val restoreResult = backupRepository.restoreDataIfNeeded()
                    if (restoreResult is RestoreResult.Failed) {
                        authRepository.signOut()
                        canEnterHome = false
                        forceLogin = true
                    } else {
                        userRepository.upsertGoogleUser(authUser)
                    }
                } else {
                    viewModelScope.launch {
                        backupRepository.backupData()
                    }
                }
                if (canEnterHome) {
                    viewModelScope.launch {
                        socialSyncRepository.syncPublicProfile(PublicProfileSyncReason.APP_START)
                    }
                    viewModelScope.launch {
                        notifyUnreadNudgesOnce()
                    }
                }
            }
            delay(SPLASH_DELAY_MS)
            _destination.value = when {
                canEnterHome -> SplashDestination.Home
                forceLogin -> SplashDestination.Login
                localUser?.onboardingComplete == true -> SplashDestination.Login
                else -> SplashDestination.Onboarding
            }
        }
    }

    private suspend fun syncReminderSchedules() {
        val settings = userRepository.getSettings().first() ?: return
        val habits = habitRepository.getAllHabits().first()
        reminderScheduler.syncReminders(settings = settings, habits = habits)
    }

    private suspend fun notifyUnreadNudgesOnce() {
        val settings = userRepository.getSettings().first()
        if (settings?.notificationsEnabled != true) return
        val nudges = socialSyncRepository.loadUnreadNudgesForAppOpen()
            .getOrDefault(emptyList())
        if (nudges.isEmpty() || !notifier.canPostNotifications()) return
        nudges.forEach { nudge ->
            notifier.showSocialNudge(nudge)
        }
        socialSyncRepository.markNudgesRead(nudges)
    }

    companion object {
        private const val SPLASH_DELAY_MS = 900L
    }
}

enum class SplashDestination {
    Onboarding,
    Login,
    Home
}
