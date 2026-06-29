package com.habitseed.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Home : Screen("home")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    object Stats : Screen("stats")
    object Store : Screen("store")
    object Social : Screen("social")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object AddHabit : Screen("add_habit")
}
