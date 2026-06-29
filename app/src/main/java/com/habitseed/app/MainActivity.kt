package com.habitseed.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.habitseed.app.ui.navigation.MainScreen
import com.habitseed.app.ui.theme.HabitSeedTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            HabitSeedTheme {
                MainScreen()
            }
        }
    }
}
