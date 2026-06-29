package com.habitseed.app.ui.screens.splash

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.habitseed.app.R

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val destination by viewModel.destination.collectAsState()
    val view = LocalView.current
    val appBackgroundColor = MaterialTheme.colorScheme.background.toArgb()
    val splashColor = MaterialTheme.colorScheme.primary
    val splashBarColor = splashColor.toArgb()
    val useLightSplashBars = splashColor.luminance() > 0.5f

    DisposableEffect(view, appBackgroundColor, splashBarColor, useLightSplashBars) {
        val window = (view.context as? Activity)?.window
        val originalStatusBarColor = window?.statusBarColor
        val originalNavigationBarColor = window?.navigationBarColor
        val originalAppearance = window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars
        }

        window?.statusBarColor = splashBarColor
        window?.navigationBarColor = splashBarColor
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = useLightSplashBars
            WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = useLightSplashBars
        }

        onDispose {
            if (window != null) {
                window.statusBarColor = originalStatusBarColor ?: appBackgroundColor
                window.navigationBarColor = originalNavigationBarColor ?: appBackgroundColor
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = originalAppearance ?: useLightSplashBars
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = originalAppearance ?: useLightSplashBars
            }
        }
    }

    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Onboarding -> onNavigateToOnboarding()
            SplashDestination.Home -> onNavigateToHome()
            null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.seed_logo_transparent),
                contentDescription = "HabitSeed logo",
                modifier = Modifier.size(160.dp)
            )
            Text(
                text = "HabitSeed",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
