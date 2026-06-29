package com.habitseed.app.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.UserEntity
import com.habitseed.app.data.local.model.ShopItemWithStatus
import com.habitseed.app.domain.repository.ShopRepository
import com.habitseed.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun purchaseItem(item: ShopItemWithStatus) {
        val currentUser = user.value ?: return
        if (currentUser.waterDrops >= item.item.priceDrops && !item.isPurchased) {
            viewModelScope.launch {
                shopRepository.purchaseShopItem(currentUser.id, item.item.id)
            }
        }
    }
}
