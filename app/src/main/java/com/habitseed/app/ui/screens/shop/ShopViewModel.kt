package com.habitseed.app.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.model.ShopItemWithStatus
import com.habitseed.app.domain.repository.ShopRepository
import com.habitseed.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val user: StateFlow<UserEntity?> = userRepository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val shopItems: StateFlow<List<ShopItemWithStatus>> = shopRepository.getAllShopItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun purchaseItem(item: ShopItemWithStatus) {
        val currentUser = user.value ?: return
        viewModelScope.launch {
            when {
                item.isPurchased -> _messages.emit("${item.item.name} is already yours.")
                currentUser.waterDrops < item.item.priceDrops -> _messages.emit("Not enough drops for ${item.item.name}.")
                else -> {
                    val purchased = shopRepository.purchaseShopItem(currentUser.id, item.item.id)
                    if (purchased) {
                        _messages.emit("${item.item.name} unlocked.")
                    } else {
                        _messages.emit("Could not complete that purchase right now.")
                    }
                }
            }
        }
    }
}
