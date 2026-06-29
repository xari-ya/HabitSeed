package com.habitseed.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import com.habitseed.app.ui.navigation.MainScreen
import com.habitseed.app.ui.theme.HabitSeedTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by viewModel.settings.collectAsState()
            HabitSeedTheme(
                darkTheme = settings?.darkModeEnabled == true
            ) {
                MainScreen()
            }
        }
    }
}
