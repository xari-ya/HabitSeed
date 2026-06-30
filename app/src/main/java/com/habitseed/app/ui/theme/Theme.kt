package com.habitseed.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LeafGlow,
    secondary = AmberGlow,
    tertiary = SunsetOrange,
    background = NightForest,
    surface = NightSurface,
    surfaceVariant = NightSurfaceHigh,
    primaryContainer = NightSage,
    secondaryContainer = Color(0xFF4A2A12),
    error = SoftRed,
    onPrimary = NightForest,
    onSecondary = NightForest,
    onTertiary = Color.White,
    onBackground = NightText,
    onSurface = NightText,
    onSurfaceVariant = NightTextMuted,
    onPrimaryContainer = NightText,
    onSecondaryContainer = NightText,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = SunsetOrange,
    tertiary = SunsetOrange,
    background = Cream,
    surface = White,
    surfaceVariant = Mint,
    primaryContainer = Sage,
    secondaryContainer = Color(0xFFFFE7CC),
    error = SoftRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkSlate,
    onSurface = DarkSlate,
    onSurfaceVariant = LightGrey,
    onPrimaryContainer = DarkSlate,
    onSecondaryContainer = DarkSlate,
    onError = Color.White
)

@Composable
fun HabitSeedTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
