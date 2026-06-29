package com.habitseed.app.ui.screens.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitseed.app.data.local.entity.HabitEntity
import com.habitseed.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun saveHabit(onSuccess: () -> Unit) {
        if (_title.value.isBlank()) return
        
        viewModelScope.launch {
            val newHabit = HabitEntity(
                name = _title.value,
                description = _description.value,
                frequencyType = _frequency.value.uppercase(),
                weeklyDaysMask = if (_frequency.value.equals("Daily", ignoreCase = true)) 127 else null,
                plantTypeId = _selectedPlant.value.lowercase()
            )
            habitRepository.insertHabit(newHabit)
            onSuccess()
        }
    }
}
