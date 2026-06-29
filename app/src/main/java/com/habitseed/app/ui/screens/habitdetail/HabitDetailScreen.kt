package com.habitseed.app.ui.screens.habitdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.habitseed.app.ui.components.PlantVisualizer
import com.habitseed.app.ui.components.StatCard
import com.habitseed.app.ui.components.SwipeToCompleteSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val habit by viewModel.habit.collectAsState()
    val isCompletedToday by viewModel.isCompletedToday.collectAsState()

    if (habit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        habit?.title ?: "",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Edit */ }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Habit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Immersive Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                PlantVisualizer(
                    plantType = habit!!.plantType,
                    growthLevel = habit!!.plantGrowthLevel,
                    modifier = Modifier.size(200.dp)
                )
            }
            
            // Bottom Content Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            label = "Streak",
                            value = "${habit!!.currentStreak}d",
                            modifier = Modifier.weight(1f),
                            isHighlighted = true
                        )
                        StatCard(
                            label = "Best",
                            value = "${habit!!.bestStreak}d",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Level",
                            value = "${habit!!.plantGrowthLevel}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Swipe to Complete
                    if (isCompletedToday) {
                        SwipeToCompleteSlider(
                            onComplete = {},
                            text = "Watered! \uD83C\uDF31",
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else {
                        SwipeToCompleteSlider(
                            onComplete = { viewModel.completeHabit() },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
