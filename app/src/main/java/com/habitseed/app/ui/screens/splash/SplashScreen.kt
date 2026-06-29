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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.habitseed.app.R
import com.habitseed.app.ui.theme.ForestGreen
import com.habitseed.app.ui.theme.Cream

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val destination by viewModel.destination.collectAsState()
    val view = LocalView.current
    val appBackgroundColor = Cream.toArgb()

    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val originalStatusBarColor = window?.statusBarColor
        val originalNavigationBarColor = window?.navigationBarColor
        val originalAppearance = window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars
        }

        window?.statusBarColor = ForestGreen.toArgb()
        window?.navigationBarColor = ForestGreen.toArgb()
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }

        onDispose {
            if (window != null) {
                window.statusBarColor = originalStatusBarColor ?: appBackgroundColor
                window.navigationBarColor = originalNavigationBarColor ?: appBackgroundColor
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = originalAppearance ?: true
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
            .background(ForestGreen)
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
                color = Color.White
            )
        }
    }
}
