package com.riramzy.pillfllow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.ui.screens.auth.AuthRoleSelectionScreen
import com.riramzy.pillfllow.ui.screens.auth.AuthSignInScreen
import com.riramzy.pillfllow.ui.screens.auth.AuthSignUpScreen
import com.riramzy.pillfllow.ui.screens.dashboard.CaregiverDashboardScreen
import com.riramzy.pillfllow.ui.screens.dashboard.PatientDashboardScreen
import com.riramzy.pillfllow.ui.screens.history.CaregiverHistoryScreen
import com.riramzy.pillfllow.ui.screens.history.PatientHistoryScreen
import com.riramzy.pillfllow.ui.screens.prescriptions.CaregiverPrescriptionsScreen
import com.riramzy.pillfllow.ui.screens.prescriptions.PatientPrescriptionsScreen
import com.riramzy.pillfllow.ui.screens.settings.CaregiverSettingsScreen
import com.riramzy.pillfllow.ui.screens.settings.PatientSettingsScreen
import com.riramzy.pillfllow.ui.screens.splash.SplashScreen
import com.riramzy.pillfllow.utils.app.Screen
import com.riramzy.pillfllow.utils.app.UserType
import org.koin.compose.koinInject

@Composable
fun NavApp(
    navController: NavHostController = rememberNavController(),
    sessionManager: SessionManager = koinInject()
) {
    val currentUser by sessionManager.currentUser.collectAsStateWithLifecycle()
    val isCaregiver = currentUser?.userType?.equals("CAREGIVER", ignoreCase = true) == true

    var selectedRole by rememberSaveable { mutableStateOf(UserType.PATIENT) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.RoleSelection.route) {
            AuthRoleSelectionScreen(
                onRoleSelected = { role ->
                    selectedRole = role
                    navController.navigate(Screen.SignIn.route)
                }
            )
        }

        composable(Screen.SignIn.route) {
            AuthSignInScreen(
                selectedRole = selectedRole,
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            AuthSignUpScreen(
                selectedRole = selectedRole,
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            if (isCaregiver) {
                CaregiverDashboardScreen(
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            } else {
                PatientDashboardScreen(
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
        }

        composable(Screen.History.route) {
            if (isCaregiver) {
                CaregiverHistoryScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            } else {
                PatientHistoryScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
        }

        composable(Screen.Prescriptions.route) {
            if (isCaregiver) {
                CaregiverPrescriptionsScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            } else {
                PatientPrescriptionsScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
        }

        composable(Screen.Settings.route) {
            val onLogoutSuccess = {
                sessionManager.clearUser()
                navController.navigate(Screen.RoleSelection.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }

            if (isCaregiver) {
                CaregiverSettingsScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) },
                    onSignOutSuccess = onLogoutSuccess
                )
            } else {
                PatientSettingsScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) },
                    onSignOutSuccess = onLogoutSuccess
                )
            }
        }
    }
}