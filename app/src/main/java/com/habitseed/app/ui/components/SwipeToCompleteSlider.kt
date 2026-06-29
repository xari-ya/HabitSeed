package com.habitseed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.PlayArrow // Use a water drop or check in real app

@Composable
fun SwipeToCompleteSlider(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Slide to Water \uD83D\uDCA7"
) {
    val height = 64.dp
    val thumbSize = 56.dp
    val padding = 4.dp
    
    var componentWidth by remember { mutableStateOf(0f) }
    val maxDragX = componentWidth - with(LocalDensity.current) { thumbSize.toPx() } - with(LocalDensity.current) { (padding * 2).toPx() }
    
    val offsetX = remember { Animatable(0f) }
    var isCompleted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val draggableState = rememberDraggableState { delta ->
        if (isCompleted || maxDragX <= 0) return@rememberDraggableState
        
        val newOffset = (offsetX.value + delta).coerceIn(0f, maxDragX)
        coroutineScope.launch {
            offsetX.snapTo(newOffset)
        }
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
        // Progress background
        val progressWidth = with(LocalDensity.current) { offsetX.value.toDp() } + (thumbSize / 2)
        Box(
            modifier = Modifier
                .width(progressWidth.coerceAtLeast(0.dp))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        
        // Text
        val textOpacity = 1f - (offsetX.value / maxDragX.coerceAtLeast(1f))
        Text(
            text = if (isCompleted) "Watered! \uD83C\uDF31" else text,
            color = Color.White.copy(alpha = textOpacity.coerceIn(0f, 1f)),
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (isCompleted) return@draggable
                        if (offsetX.value >= maxDragX * 0.95f) {
                            isCompleted = true
                            coroutineScope.launch {
                                offsetX.animateTo(maxDragX, tween(300))
                                onComplete()
                                // Reset after delay if needed
                            }
                        } else {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, tween(300))
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                contentDescription = "Swipe",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
