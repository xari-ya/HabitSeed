package com.habitseed.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.habitseed.app.notifications.HabitSeedNotifier
import com.habitseed.app.ui.navigation.MainScreen
import com.habitseed.app.ui.feedback.LocalHabitSeedHapticsEnabled
import com.habitseed.app.ui.theme.HabitSeedTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var notifier: HabitSeedNotifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notifier.createNotificationChannels()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalHabitSeedHapticsEnabled provides (settings?.hapticsEnabled != false)
            ) {
                HabitSeedTheme(
                    darkTheme = settings?.darkModeEnabled == true
                ) {
                    MainScreen()
                }
            }
        }
    }
}
