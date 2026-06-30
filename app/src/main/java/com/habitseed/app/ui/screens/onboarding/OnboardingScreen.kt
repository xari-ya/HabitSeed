package com.habitseed.app.ui.screens.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitseed.app.R
import com.habitseed.app.ui.feedback.rememberHabitSeedHaptics
import com.habitseed.app.ui.theme.HabitSeedDimens

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberHabitSeedHaptics()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
            ) {
                DecorativeLeaves()
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                )
                Image(
                    painter = painterResource(id = R.drawable.seed_logo_transparent),
                    contentDescription = "Seed illustration",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(168.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.42f),
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = HabitSeedDimens.ScreenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Grow Your Best Self",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Turn your daily routines into a thriving digital garden.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                OnboardingDots()
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        haptics.selection()
                        onGetStarted()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HabitSeedDimens.ButtonHeight),
                    shape = RoundedCornerShape(HabitSeedDimens.ButtonRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun OnboardingDots() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val isActive = index == 0
            Box(
                modifier = Modifier
                    .size(width = if (isActive) 24.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                    )
            )
        }
    }
}

@Composable
private fun DecorativeLeaves() {
    val leafBase = MaterialTheme.colorScheme.primaryContainer
    val leafSoft = MaterialTheme.colorScheme.surfaceVariant
    val vine = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawCircle(
            color = leafBase.copy(alpha = 0.4f),
            radius = size.minDimension * 0.22f,
            center = Offset(size.width * 0.22f, size.height * 0.26f)
        )
        drawCircle(
            color = leafSoft.copy(alpha = 0.9f),
            radius = size.minDimension * 0.18f,
            center = Offset(size.width * 0.78f, size.height * 0.22f)
        )
        drawLine(
            color = vine.copy(alpha = 0.18f),
            start = Offset(size.width * 0.18f, size.height * 0.88f),
            end = Offset(size.width * 0.34f, size.height * 0.64f),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = vine.copy(alpha = 0.14f),
            start = Offset(size.width * 0.82f, size.height * 0.82f),
            end = Offset(size.width * 0.68f, size.height * 0.56f),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )
    }
}
