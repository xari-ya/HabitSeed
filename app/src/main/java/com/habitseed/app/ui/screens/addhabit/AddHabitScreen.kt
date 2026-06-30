package com.habitseed.app.ui.screens.addhabit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitseed.app.ui.theme.ForestGreen
import com.habitseed.app.ui.theme.HabitSeedDimens
import com.habitseed.app.ui.theme.SunsetOrange
import com.habitseed.app.ui.feedback.rememberHabitSeedHaptics
import com.habitseed.app.ui.feedback.rememberNotificationPermissionRequester

private data class HabitIconOption(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val tint: Color
)

private data class HabitColorOption(
    val label: String,
    val colorHex: String,
    val color: Color
)

private val habitIconOptions = listOf(
    HabitIconOption("sprout", "Grow", Icons.Filled.Spa, ForestGreen),
    HabitIconOption("water", "Hydrate", Icons.Filled.LocalDrink, Color(0xFF3FA7D6)),
    HabitIconOption("mind", "Mindful", Icons.Filled.SelfImprovement, Color(0xFFFF7D00)),
    HabitIconOption("heart", "Health", Icons.Filled.Favorite, Color(0xFFD76A6A)),
    HabitIconOption("book", "Learn", Icons.Filled.Book, Color(0xFF5C7AEA)),
    HabitIconOption("spark", "Routine", Icons.Filled.AutoAwesome, Color(0xFFE09F3E))
)

private val habitColorOptions = listOf(
    HabitColorOption("Forest", "#2D6A4F", ForestGreen),
    HabitColorOption("Sunset", "#FF7D00", SunsetOrange),
    HabitColorOption("Sky", "#3FA7D6", Color(0xFF3FA7D6)),
    HabitColorOption("Berry", "#D76A6A", Color(0xFFD76A6A))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val frequency by viewModel.frequency.collectAsStateWithLifecycle()
    val selectedPlant by viewModel.selectedPlant.collectAsStateWithLifecycle()
    val selectedIcon by viewModel.selectedIcon.collectAsStateWithLifecycle()
    val selectedColor by viewModel.selectedColor.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val plantChoices by viewModel.plantChoices.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = rememberHabitSeedHaptics()
    val requestNotificationsForReminder = rememberNotificationPermissionRequester(
        onGranted = {
            haptics.selection()
            viewModel.updateReminderEnabled(true)
        },
        onDenied = {
            viewModel.showMessage("Allow notifications to use habit reminders.")
        }
    )

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            haptics.warning()
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New Seed") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        AddHabitForm(
            title = title,
            description = description,
            frequency = frequency,
            selectedPlant = selectedPlant,
            plantChoices = plantChoices,
            selectedIcon = selectedIcon,
            selectedColor = selectedColor,
            reminderEnabled = reminderEnabled,
            onTitleChanged = viewModel::updateTitle,
            onDescriptionChanged = viewModel::updateDescription,
            onFrequencyChanged = {
                haptics.selection()
                viewModel.updateFrequency(it)
            },
            onSelectedPlantChanged = {
                haptics.selection()
                viewModel.updateSelectedPlant(it)
            },
            onSelectedIconChanged = {
                haptics.selection()
                viewModel.updateSelectedIcon(it)
            },
            onSelectedColorChanged = {
                haptics.selection()
                viewModel.updateSelectedColor(it)
            },
            onReminderChanged = { enabled ->
                if (enabled) {
                    requestNotificationsForReminder()
                } else {
                    haptics.selection()
                    viewModel.updateReminderEnabled(false)
                }
            },
            onSave = {
                haptics.success()
                viewModel.saveHabit(onNavigateBack)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSheet(
    onDismiss: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val frequency by viewModel.frequency.collectAsStateWithLifecycle()
    val selectedPlant by viewModel.selectedPlant.collectAsStateWithLifecycle()
    val selectedIcon by viewModel.selectedIcon.collectAsStateWithLifecycle()
    val selectedColor by viewModel.selectedColor.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val plantChoices by viewModel.plantChoices.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = rememberHabitSeedHaptics()
    val requestNotificationsForReminder = rememberNotificationPermissionRequester(
        onGranted = {
            haptics.selection()
            viewModel.updateReminderEnabled(true)
        },
        onDenied = {
            viewModel.showMessage("Allow notifications to use habit reminders.")
        }
    )

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            haptics.warning()
            snackbarHostState.showSnackbar(message)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = null,
        tonalElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(640.dp)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = HabitSeedDimens.ScreenPadding)
                .padding(top = 14.dp, bottom = HabitSeedDimens.ScreenPadding)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 52.dp, height = 5.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(999.dp)
                    )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Plant a new habit",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose a small routine and let it grow into something beautiful.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            AddHabitForm(
                title = title,
                description = description,
                frequency = frequency,
                selectedPlant = selectedPlant,
                plantChoices = plantChoices,
                selectedIcon = selectedIcon,
                selectedColor = selectedColor,
                reminderEnabled = reminderEnabled,
                onTitleChanged = viewModel::updateTitle,
                onDescriptionChanged = viewModel::updateDescription,
                onFrequencyChanged = {
                    haptics.selection()
                    viewModel.updateFrequency(it)
                },
                onSelectedPlantChanged = {
                    haptics.selection()
                    viewModel.updateSelectedPlant(it)
                },
                onSelectedIconChanged = {
                    haptics.selection()
                    viewModel.updateSelectedIcon(it)
                },
                onSelectedColorChanged = {
                    haptics.selection()
                    viewModel.updateSelectedColor(it)
                },
                onReminderChanged = { enabled ->
                    if (enabled) {
                        requestNotificationsForReminder()
                    } else {
                        haptics.selection()
                        viewModel.updateReminderEnabled(false)
                    }
                },
                onSave = {
                    haptics.success()
                    viewModel.saveHabit(onDismiss)
                }
            )
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddHabitForm(
    title: String,
    description: String,
    frequency: String,
    selectedPlant: String,
    plantChoices: List<PlantChoiceUi>,
    selectedIcon: String,
    selectedColor: String,
    reminderEnabled: Boolean,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onFrequencyChanged: (String) -> Unit,
    onSelectedPlantChanged: (String) -> Unit,
    onSelectedIconChanged: (String) -> Unit,
    onSelectedColorChanged: (String) -> Unit,
    onReminderChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            placeholder = { Text("Name your habit...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            placeholder = { Text("Why does this matter to you?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(20.dp)
        )

        SectionLabel("Pick an icon")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(208.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(habitIconOptions, key = { it.id }) { option ->
                IconChoiceCard(
                    option = option,
                    selected = selectedIcon == option.id,
                    onClick = { onSelectedIconChanged(option.id) }
                )
            }
        }

        SectionLabel("Frequency")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Daily", "Weekly", "Custom").forEach { item ->
                FilterChip(
                    selected = frequency == item,
                    onClick = { onFrequencyChanged(item) },
                    label = { Text(item) }
                )
            }
        }

        SectionLabel("Color mood")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            habitColorOptions.forEach { option ->
                ColorChoice(
                    option = option,
                    selected = selectedColor == option.colorHex,
                    onClick = { onSelectedColorChanged(option.colorHex) }
                )
            }
        }

        SectionLabel("Plant style")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val visibleChoices = plantChoices.ifEmpty {
                listOf(
                    PlantChoiceUi(
                        plantTypeId = "sunflower",
                        name = "Sunflower",
                        previewAsset = com.habitseed.app.R.drawable.sunflower_fully_grown,
                        isUnlocked = true,
                        priceDrops = null
                    )
                )
            }
            visibleChoices.forEach { plant ->
                FilterChip(
                    selected = selectedPlant == plant.plantTypeId,
                    onClick = { onSelectedPlantChanged(plant.plantTypeId) },
                    enabled = true,
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = plant.previewAsset),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (plant.isUnlocked) {
                                plant.name
                            } else {
                                "${plant.name} · ${plant.priceDrops ?: 0}"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily reminder",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Get a gentle nudge to water this habit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderChanged
                )
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(HabitSeedDimens.ButtonHeight),
            enabled = title.isNotBlank(),
            shape = RoundedCornerShape(HabitSeedDimens.ButtonRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = "Plant Seed",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun IconChoiceCard(
    option: HabitIconOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (selected) option.tint.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) option.tint else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(option.tint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.label,
                    tint = option.tint
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ColorChoice(
    option: HabitColorOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) option.color else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(option.color, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
