package com.habitseed.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun PlantVisualizer(
    plantTypeId: String?,
    growthStage: Int,
    modifier: Modifier = Modifier
) {
    val normalizedGrowthStage = growthStage.coerceIn(0, 5)
    val imageRes = PlantAssetMapper.imageFor(
        plantTypeId = plantTypeId,
        growthStage = normalizedGrowthStage
    )

    val infiniteTransition = rememberInfiniteTransition(label = "float_animation")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f, // Match the -10px from CSS
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Box(
        modifier = modifier.size(192.dp), // 48 tailwind units = 192px/dp roughly
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "${plantTypeId ?: "sunflower"} plant illustration at growth stage $normalizedGrowthStage",
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, floatOffset.roundToInt()) }
        )
    }
}
