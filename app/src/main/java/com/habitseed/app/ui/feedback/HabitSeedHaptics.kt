package com.habitseed.app.ui.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
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
            enabled = enabled,
            performer = ViewHapticFeedbackPerformer(view)
        )
    }
}

internal fun interface HapticFeedbackPerformer {
    fun perform(type: Int): Boolean
}

private class ViewHapticFeedbackPerformer(
    private val view: View
) : HapticFeedbackPerformer {
    override fun perform(type: Int): Boolean {
        if (view.performHapticFeedback(type)) return true
        if (!view.isHapticFeedbackEnabled || !isSystemHapticsEnabled(view.context)) return false
        return vibrateFallback(view.context, type)
    }

    private fun vibrateFallback(context: Context, type: Int): Boolean {
        val vibrator = context.vibrator() ?: return false
        if (!vibrator.hasVibrator()) return false

        val durationMillis = when (type) {
            HapticFeedbackConstants.CLOCK_TICK -> SELECTION_DURATION_MS
            else -> ACTION_DURATION_MS
        }
        val amplitude = when (type) {
            HapticFeedbackConstants.CLOCK_TICK -> SELECTION_AMPLITUDE
            else -> VibrationEffect.DEFAULT_AMPLITUDE
        }
        vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, amplitude))
        return true
    }

    private fun Context.vibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    @Suppress("DEPRECATION")
    private fun isSystemHapticsEnabled(context: Context): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.HAPTIC_FEEDBACK_ENABLED,
            SYSTEM_HAPTICS_ENABLED
        ) == SYSTEM_HAPTICS_ENABLED
    }

    private companion object {
        const val SELECTION_DURATION_MS = 12L
        const val ACTION_DURATION_MS = 24L
        const val SELECTION_AMPLITUDE = 80
        const val SYSTEM_HAPTICS_ENABLED = 1
    }
}

class HabitSeedHaptics internal constructor(
    private val enabled: Boolean,
    private val performer: HapticFeedbackPerformer
) {
    fun selection(): Boolean {
        return perform(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun success(): Boolean {
        return perform(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    fun warning(): Boolean {
        return perform(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    private fun perform(type: Int): Boolean {
        return enabled && performer.perform(type)
    }
}
