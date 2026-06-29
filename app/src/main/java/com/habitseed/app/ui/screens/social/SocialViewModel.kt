package com.habitseed.app.ui.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.auth.AuthRepository
import com.habitseed.app.data.local.entity.FriendEntity
import com.habitseed.app.data.social.SocialSyncRepository
import com.habitseed.app.data.social.dto.FollowingDto
import com.habitseed.app.data.social.dto.NudgeMessageTypes
import com.habitseed.app.data.social.dto.PublicProfileDto
import com.habitseed.app.domain.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SocialTab(val label: String) {
    Leaderboard("Leaderboard"),
    Gardens("Gardens")
}

data class SocialUiState(
    val selectedTab: SocialTab = SocialTab.Leaderboard,
    val friends: List<FriendEntity> = emptyList(),
    val leaderboard: List<PublicProfileDto> = emptyList(),
    val following: List<FollowingDto> = emptyList(),
    val isGoogleSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val nudgingTargetUid: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val socialRepository: SocialRepository,
    private val socialSyncRepository: SocialSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()
    private var lastManualRefreshAt: Long = 0L

    init {
        viewModelScope.launch {
            socialRepository.getFriends().collect { friends ->
                _uiState.update { state ->
                    state.copy(friends = friends.sortedByDescending { it.currentStreak })
                }
            }
        }
        viewModelScope.launch {
            socialSyncRepository.observeCachedLeaderboard().collect { leaderboard ->
                _uiState.update { state -> state.copy(leaderboard = leaderboard) }
            }
        }
        viewModelScope.launch {
            socialSyncRepository.observeCachedFollowing().collect { following ->
                _uiState.update { state -> state.copy(following = following) }
            }
        }
        _uiState.update { it.copy(isGoogleSignedIn = authRepository.isSignedInWithGoogle()) }
    }

    fun selectTab(tab: SocialTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        val now = System.currentTimeMillis()
        if (now - lastManualRefreshAt < MANUAL_REFRESH_COOLDOWN_MS) {
            viewModelScope.launch {
                _messages.emit("Social data was just refreshed. Try again in a moment.")
            }
            return
        }
        val signedIn = authRepository.isSignedInWithGoogle()
        _uiState.update {
            it.copy(
                isGoogleSignedIn = signedIn,
                isLoading = signedIn,
                errorMessage = null
            )
        }
        if (!signedIn) return
        lastManualRefreshAt = now

        viewModelScope.launch {
            val result = when (_uiState.value.selectedTab) {
                SocialTab.Leaderboard -> socialSyncRepository.loadLeaderboard()
                SocialTab.Gardens -> socialSyncRepository.loadFollowing()
            }
            val errorMessage = result.exceptionOrNull()?.message

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    fun addFriendByUid(uid: String) {
        viewModelScope.launch {
            val result = socialSyncRepository.addFriendByUid(uid)
            if (result.isSuccess) {
                _messages.emit("Friend added.")
            } else {
                _messages.emit(result.exceptionOrNull()?.message ?: "Failed to add friend.")
            }
        }
    }

    fun unfollow(friend: FollowingDto) {
        viewModelScope.launch {
            val result = socialSyncRepository.unfollow(friend.targetUid)
            if (result.isSuccess) {
                _messages.emit("Friend removed.")
            } else {
                _messages.emit(result.exceptionOrNull()?.message ?: "Failed to remove friend.")
            }
        }
    }

    fun sendNudge(friend: FollowingDto) {
        if (_uiState.value.nudgingTargetUid == friend.targetUid) return
        viewModelScope.launch {
            _uiState.update { it.copy(nudgingTargetUid = friend.targetUid) }
            val result = socialSyncRepository.sendNudge(
                targetUid = friend.targetUid,
                messageType = NudgeMessageTypes.GARDEN_NUDGE,
                message = "Keep your garden growing."
            )
            if (result.isSuccess) {
                _messages.emit("Nudge sent 🌱")
            } else {
                _messages.emit(result.exceptionOrNull()?.message ?: "Could not send nudge.")
            }
            _uiState.update { it.copy(nudgingTargetUid = null) }
        }
    }

    fun sendNudge(friend: FriendEntity) {
        viewModelScope.launch {
            val nudged = socialRepository.sendNudge(
                friendId = friend.id,
                message = "Keep your garden growing."
            )
            if (nudged) {
                _messages.emit("Nudge sent 🌱")
            } else {
                _messages.emit("Could not send a nudge to ${friend.name}.")
            }
        }
    }

    private companion object {
        const val MANUAL_REFRESH_COOLDOWN_MS = 60_000L
    }
}
