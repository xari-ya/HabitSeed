package com.habitseed.app.ui.screens.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.data.local.entity.UserSettingsEntity
import com.habitseed.app.domain.repository.HabitRepository
import com.habitseed.app.domain.repository.ShopRepository
import com.habitseed.app.domain.repository.UserRepository
import com.habitseed.app.notifications.HabitReminderScheduler
import com.habitseed.app.ui.components.PlantAssetMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository,
    private val reminderScheduler: HabitReminderScheduler,
    shopRepository: ShopRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _frequency = MutableStateFlow("Daily")
    val frequency: StateFlow<String> = _frequency.asStateFlow()

    private val _selectedPlant = MutableStateFlow("sunflower")
    val selectedPlant: StateFlow<String> = _selectedPlant.asStateFlow()

    val plantChoices: StateFlow<List<PlantChoiceUi>> = shopRepository.getAllShopItems()
        .map { items ->
            items
                .filter { it.item.itemType == "PLANT" && it.item.linkedPlantTypeId != null }
                .map { item ->
                    val plantTypeId = item.item.linkedPlantTypeId ?: "sunflower"
                    PlantChoiceUi(
                        plantTypeId = plantTypeId,
                        name = item.item.name,
                        previewAsset = PlantAssetMapper.imageFor(plantTypeId, 5),
                        isUnlocked = item.isPurchased,
                        priceDrops = item.item.priceDrops.takeUnless { item.isPurchased }
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _selectedIcon = MutableStateFlow("sprout")
    val selectedIcon: StateFlow<String> = _selectedIcon.asStateFlow()

    private val _selectedColor = MutableStateFlow("#2D6A4F")
    val selectedColor: StateFlow<String> = _selectedColor.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(false)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    fun updateFrequency(newFrequency: String) {
        _frequency.value = newFrequency
    }

    fun updateSelectedPlant(newPlant: String) {
        val choice = plantChoices.value.firstOrNull { it.plantTypeId == newPlant }
        if (choice == null || choice.isUnlocked) {
            _selectedPlant.value = newPlant
        } else {
            viewModelScope.launch {
                _messages.emit("Unlock this plant in the Seed Store.")
            }
        }
    }

    fun updateSelectedIcon(iconName: String) {
        _selectedIcon.value = iconName
    }

    fun updateSelectedColor(colorHex: String) {
        _selectedColor.value = colorHex
    }

    fun updateReminderEnabled(enabled: Boolean) {
        _reminderEnabled.value = enabled
    }

    fun showMessage(message: String) {
        viewModelScope.launch {
            _messages.emit(message)
        }
    }

    fun resetForm() {
        _title.value = ""
        _description.value = ""
        _frequency.value = "Daily"
        _selectedPlant.value = "sunflower"
        _selectedIcon.value = "sprout"
        _selectedColor.value = "#2D6A4F"
        _reminderEnabled.value = false
    }

    fun saveHabit(onSuccess: () -> Unit) {
        if (_title.value.isBlank()) return

        viewModelScope.launch {
            val frequencyType = _frequency.value.uppercase()
            val settings = userRepository.getSettings().first() ?: UserSettingsEntity()
            val newHabit = HabitEntity(
                name = _title.value.trim(),
                description = _description.value.trim().ifBlank { null },
                iconName = _selectedIcon.value,
                colorHex = _selectedColor.value,
                frequencyType = frequencyType,
                weeklyDaysMask = when (frequencyType) {
                    "DAILY" -> 127
                    "WEEKLY" -> 62
                    else -> null
                },
                plantTypeId = _selectedPlant.value,
                reminderEnabled = _reminderEnabled.value,
                reminderHour = settings.reminderHour.takeIf { _reminderEnabled.value },
                reminderMinute = settings.reminderMinute.takeIf { _reminderEnabled.value }
            )
            habitRepository.insertHabit(newHabit)
            val habits = habitRepository.getAllHabits().first()
            reminderScheduler.syncReminders(settings = settings, habits = habits)
            resetForm()
            onSuccess()
        }
    }
}

data class PlantChoiceUi(
    val plantTypeId: String,
    val name: String,
    val previewAsset: Int,
    val isUnlocked: Boolean,
    val priceDrops: Int?
)
