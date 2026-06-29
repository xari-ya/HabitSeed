package com.habitseed.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userRepository: UserRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val onboardingComplete = userRepository.getUser().first()?.onboardingComplete == true
            delay(SPLASH_DELAY_MS)
            _destination.value = if (onboardingComplete) {
                SplashDestination.Home
            } else {
                SplashDestination.Onboarding
            }
        }
    }

    companion object {
        private const val SPLASH_DELAY_MS = 900L
    }
}

enum class SplashDestination {
    Onboarding,
    Home
}
