package com.habitseed.app.ui.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.FriendEntity
import com.habitseed.app.domain.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SocialTab(val label: String) {
    Leaderboard("Leaderboard"),
    Gardens("Gardens")
}

data class SocialUiState(
    val selectedTab: SocialTab = SocialTab.Leaderboard,
    val friends: List<FriendEntity> = emptyList()
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val selectedTab = MutableStateFlow(SocialTab.Leaderboard)
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<SocialUiState> = combine(
        selectedTab,
        socialRepository.getFriends()
    ) { tab, friends ->
        SocialUiState(
            selectedTab = tab,
            friends = when (tab) {
                SocialTab.Leaderboard -> friends.sortedByDescending { it.currentStreak }
                SocialTab.Gardens -> friends.sortedBy { it.name.lowercase() }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SocialUiState()
    )

    fun selectTab(tab: SocialTab) {
        selectedTab.update { tab }
    }

    fun sendNudge(friend: FriendEntity) {
        viewModelScope.launch {
            val nudged = socialRepository.sendNudge(
                friendId = friend.id,
                message = "Keep your garden growing."
            )
            if (nudged) {
                _messages.emit("Nudge sent to ${friend.name}.")
            } else {
                _messages.emit("Could not send a nudge to ${friend.name}.")
            }
        }
    }
}
