package com.riramzy.pillfllow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.riramzy.pillfllow.domain.repo.AuthRepo
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
import com.riramzy.pillfllow.utils.Screen
import org.koin.compose.koinInject

@Composable
fun NavApp(
    navController: NavHostController = rememberNavController(),
    authRepo: AuthRepo = koinInject()
) {
    val currentUser by authRepo.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val isCaregiver = currentUser?.userType?.equals("CAREGIVER", ignoreCase = true) == true

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(Screen.RoleSelection.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) Screen.RoleSelection.route else Screen.Home.route
    ) {
        composable(Screen.RoleSelection.route) {
            AuthRoleSelectionScreen(
                onRoleSelected = { navController.navigate(Screen.SignIn.route) }
            )
        }

        composable(Screen.SignIn.route) {
            AuthSignInScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            AuthSignUpScreen(
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
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
            if (isCaregiver) {
                CaregiverSettingsScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) }
                )
            } else {
                PatientSettingsScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToPrescriptions = { navController.navigate(Screen.Prescriptions.route) }
                )
            }
        }
    }
}