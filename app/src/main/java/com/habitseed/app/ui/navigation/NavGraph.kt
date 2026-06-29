package com.habitseed.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habitseed.app.ui.screens.placeholders.FutureScreenPlaceholder
import com.habitseed.app.ui.screens.shop.ShopScreen

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
            FutureScreenPlaceholder(
                title = "Splash",
                subtitle = "Navigation shell route is ready. The branded splash screen comes in the next UI step."
            )
        }

        composable(Screen.Onboarding.route) {
            FutureScreenPlaceholder(
                title = "Onboarding",
                subtitle = "Onboarding content lands in the next dedicated screen step."
            )
        }

        composable(Screen.Login.route) {
            FutureScreenPlaceholder(
                title = "Login",
                subtitle = "Local demo login will be added in the upcoming auth step."
            )
        }

        composable(Screen.Home.route) {
            com.habitseed.app.ui.screens.home.HomeScreen(
                onNavigateToHabitDetail = { id -> navController.navigate(Screen.HabitDetail.createRoute(id)) },
                onNavigateToAddHabit = { navController.navigate(Screen.AddHabit.route) }
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
