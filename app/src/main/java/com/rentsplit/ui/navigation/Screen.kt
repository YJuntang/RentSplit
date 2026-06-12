package com.rentsplit.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Balances : Screen("balances")
    object History : Screen("history")
    object Settings : Screen("settings")
    object Categories : Screen("categories")
    object MonthDetail : Screen("monthDetail/{month}/{year}") {
        fun createRoute(month: Int, year: Int) = "monthDetail/$month/$year"
    }
}
