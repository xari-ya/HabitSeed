package com.habitseed.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.social.PublicProfileSyncReason
import com.habitseed.app.data.social.SocialSyncRepository
import com.habitseed.app.domain.repository.UserRepository
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
    private val authRepository: AuthRepository,
    private val socialSyncRepository: SocialSyncRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val localUser = userRepository.getUser().first()
            val authUser = authRepository.currentUser()
            if (authUser != null) {
                val shouldMergeAuthUser = localUser == null ||
                    localUser.firebaseUid != authUser.uid ||
                    localUser.authProvider != "google"
                if (shouldMergeAuthUser) {
                    runCatching {
                        userRepository.upsertGoogleUser(authUser)
                    }
                }
                viewModelScope.launch {
                    socialSyncRepository.syncPublicProfile(PublicProfileSyncReason.APP_START)
                }
            }
            delay(SPLASH_DELAY_MS)
            _destination.value = when {
                authUser != null -> SplashDestination.Home
                localUser?.onboardingComplete == true -> SplashDestination.Login
                else -> SplashDestination.Onboarding
            }
        }
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
