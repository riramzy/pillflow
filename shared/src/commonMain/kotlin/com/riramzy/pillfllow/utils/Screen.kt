package com.riramzy.pillfllow.utils

sealed class Screen(val route: String) {
    object RoleSelection: Screen("role_selection")
    object SignIn: Screen("sign_in")
    object SignUp: Screen("sign_up")
    object Home: Screen("home")
    object History: Screen("history")
    object Prescriptions: Screen("prescriptions")
    object Settings: Screen("settings")
}