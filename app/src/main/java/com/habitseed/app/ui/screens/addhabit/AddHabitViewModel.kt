package com.habitseed.app.ui.screens.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _frequency = MutableStateFlow("Daily")
    val frequency: StateFlow<String> = _frequency.asStateFlow()

    private val _selectedPlant = MutableStateFlow("Succulent")
    val selectedPlant: StateFlow<String> = _selectedPlant.asStateFlow()

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
        _selectedPlant.value = newPlant
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

    fun resetForm() {
        _title.value = ""
        _description.value = ""
        _frequency.value = "Daily"
        _selectedPlant.value = "Succulent"
        _selectedIcon.value = "sprout"
        _selectedColor.value = "#2D6A4F"
        _reminderEnabled.value = false
    }

    fun saveHabit(onSuccess: () -> Unit) {
        if (_title.value.isBlank()) return

        viewModelScope.launch {
            val frequencyType = _frequency.value.uppercase()
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
                plantTypeId = _selectedPlant.value.lowercase()
            )
            habitRepository.insertHabit(newHabit)
            resetForm()
            onSuccess()
        }
    }
}
