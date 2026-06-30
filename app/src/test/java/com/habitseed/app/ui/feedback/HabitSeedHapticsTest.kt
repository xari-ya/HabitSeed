package com.habitseed.app.ui.feedback

import android.view.HapticFeedbackConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitSeedHapticsTest {
    @Test
    fun selection_whenEnabled_invokesClockTick() {
        val calls = mutableListOf<Int>()
        val haptics = HabitSeedHaptics(
            enabled = true,
            performer = HapticFeedbackPerformer { type ->
                calls += type
                true
            }
        )

        val performed = haptics.selection()

        assertTrue(performed)
        assertEquals(listOf(HapticFeedbackConstants.CLOCK_TICK), calls)
    }

    @Test
    fun successAndWarning_whenEnabled_invokeVirtualKey() {
        val calls = mutableListOf<Int>()
        val haptics = HabitSeedHaptics(
            enabled = true,
            performer = HapticFeedbackPerformer { type ->
                calls += type
                true
            }
        )

        assertTrue(haptics.success())
        assertTrue(haptics.warning())

        assertEquals(
            listOf(
                HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.VIRTUAL_KEY
            ),
            calls
        )
    }

    @Test
    fun feedback_whenDisabled_doesNotInvokePerformer() {
        var called = false
        val haptics = HabitSeedHaptics(
            enabled = false,
            performer = HapticFeedbackPerformer {
                called = true
                true
            }
        )

        val performed = haptics.success()

        assertFalse(performed)
        assertFalse(called)
    }

    @Test
    fun feedback_whenPlatformRejects_returnsFalse() {
        val haptics = HabitSeedHaptics(
            enabled = true,
            performer = HapticFeedbackPerformer { false }
        )

        assertFalse(haptics.warning())
    }
}
