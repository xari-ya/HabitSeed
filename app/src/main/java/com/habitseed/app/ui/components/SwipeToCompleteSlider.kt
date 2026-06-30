package com.habitseed.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.habitseed.app.ui.feedback.rememberHabitSeedHaptics
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun SwipeToCompleteSlider(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Swipe to Water",
    completedText: String = "Watered Today",
    isCompleted: Boolean = false,
    enabled: Boolean = true
) {
    val height = 68.dp
    val thumbSize = 58.dp
    val padding = 5.dp

    var componentWidth by remember { mutableFloatStateOf(0f) }
    val thumbPx = with(LocalDensity.current) { thumbSize.toPx() }
    val paddingPx = with(LocalDensity.current) { (padding * 2).toPx() }
    val maxDragX = (componentWidth - thumbPx - paddingPx).coerceAtLeast(0f)

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val haptics = rememberHabitSeedHaptics()

    LaunchedEffect(isCompleted, maxDragX) {
        if (isCompleted && maxDragX > 0f) {
            offsetX.snapTo(maxDragX)
        } else if (!isCompleted && offsetX.value > maxDragX) {
            offsetX.snapTo(0f)
        }
    }

    val draggableState = rememberDraggableState { delta ->
        if (!enabled || isCompleted || maxDragX <= 0f) return@rememberDraggableState
        val newOffset = (offsetX.value + delta).coerceIn(0f, maxDragX)
        coroutineScope.launch { offsetX.snapTo(newOffset) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(padding)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                componentWidth = placeable.width.toFloat()
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (componentWidth <= 0f) 0f else ((offsetX.value + thumbPx) / componentWidth).coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(
                    if (isCompleted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    }
                )
        )

        Text(
            text = if (isCompleted) completedText else text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    enabled = enabled && !isCompleted,
                    onDragStopped = {
                        if (!enabled || isCompleted) return@draggable
                        if (offsetX.value >= maxDragX * 0.9f) {
                            coroutineScope.launch {
                                offsetX.animateTo(maxDragX, tween(220))
                                haptics.success()
                                onComplete()
                            }
                        } else {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, tween(220))
                                haptics.selection()
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.WaterDrop,
                contentDescription = if (isCompleted) "Completed" else "Swipe to complete",
                tint = if (isCompleted) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}
