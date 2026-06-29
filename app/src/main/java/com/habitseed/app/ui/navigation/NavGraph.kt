package com.habitseed.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habitseed.app.ui.screens.login.LoginScreen
import com.habitseed.app.ui.screens.onboarding.OnboardingScreen
import com.habitseed.app.ui.screens.placeholders.FutureScreenPlaceholder
import com.habitseed.app.ui.screens.shop.ShopScreen
import com.habitseed.app.ui.screens.splash.SplashScreen
import com.habitseed.app.ui.screens.splash.SplashViewModel

@Composable
fun HabitSeedNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            SplashScreen(
                viewModel = viewModel,
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            com.habitseed.app.ui.screens.home.HomeScreen(
                onNavigateToHabitDetail = { id -> navController.navigate(Screen.HabitDetail.createRoute(id)) }
            )
        }
        
        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) {
            com.habitseed.app.ui.screens.habitdetail.HabitDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            FutureScreenPlaceholder(
                title = "Stats",
                subtitle = "Statistics and harvest insights will be built in the dedicated stats step."
            )
        }
        
        composable(Screen.AddHabit.route) {
            com.habitseed.app.ui.screens.addhabit.AddHabitScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Store.route) {
            ShopScreen()
        }

        composable(Screen.Social.route) {
            FutureScreenPlaceholder(
                title = "Social",
                subtitle = "Friends and accountability features will be added in the social step."
            )
        }

        composable(Screen.Profile.route) {
            FutureScreenPlaceholder(
                title = "Profile",
                subtitle = "Profile and settings will be added in the dedicated profile step."
            )
        }
    }
}
