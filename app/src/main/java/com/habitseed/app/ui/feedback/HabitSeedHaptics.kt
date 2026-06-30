package com.habitseed.app.ui.feedback

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

val LocalHabitSeedHapticsEnabled = compositionLocalOf { true }

@Composable
fun rememberHabitSeedHaptics(): HabitSeedHaptics {
    val view = LocalView.current
    val enabled = LocalHabitSeedHapticsEnabled.current
    return remember(view, enabled) {
        HabitSeedHaptics(
            perform = { type ->
                if (enabled) {
                    view.performHapticFeedback(type)
                }
            }
        )
    }
}

class HabitSeedHaptics internal constructor(
    private val perform: (Int) -> Unit
) {
    fun selection() {
        perform(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun success() {
        perform(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    fun warning() {
        perform(HapticFeedbackConstants.VIRTUAL_KEY)
    }
}
