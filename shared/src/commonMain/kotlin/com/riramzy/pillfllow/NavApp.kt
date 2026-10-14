package com.riramzy.pillfllow

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.domain.usecase.auth.LogoutUseCase
import com.riramzy.pillfllow.ui.screens.auth.AuthRoleSelectionScreen
import com.riramzy.pillfllow.ui.screens.auth.AuthSignInScreen
import com.riramzy.pillfllow.ui.screens.auth.AuthSignUpScreen
import com.riramzy.pillfllow.ui.screens.dashboard.CaregiverDashboardScreen
import com.riramzy.pillfllow.ui.screens.dashboard.PatientDashboardScreen
import com.riramzy.pillfllow.ui.screens.history.CaregiverHistoryScreen
import com.riramzy.pillfllow.ui.screens.history.PatientHistoryScreen
import com.riramzy.pillfllow.ui.screens.onboarding.OnboardingScreen
import com.riramzy.pillfllow.ui.screens.prescriptions.CaregiverPrescriptionsScreen
import com.riramzy.pillfllow.ui.screens.prescriptions.PatientPrescriptionsScreen
import com.riramzy.pillfllow.ui.screens.settings.CaregiverSettingsScreen
import com.riramzy.pillfllow.ui.screens.settings.PatientSettingsScreen
import com.riramzy.pillfllow.ui.screens.splash.SplashScreen
import com.riramzy.pillfllow.ui.sheets.NotificationsSheet
import com.riramzy.pillfllow.ui.sheets.QuickProfileSheet
import com.riramzy.pillfllow.utils.app.Screen
import com.riramzy.pillfllow.utils.user.UserType
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavApp(
    navController: NavHostController = rememberNavController(),
    sessionManager: SessionManager = koinInject(),
    logoutUseCase: LogoutUseCase = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()

    val currentUser by sessionManager.currentUser.collectAsStateWithLifecycle()
    val isCaregiver = currentUser?.userType?.equals("CAREGIVER", ignoreCase = true) == true

    var selectedRole by rememberSaveable { mutableStateOf(UserType.PATIENT) }

    var showProfileSheet by rememberSaveable { mutableStateOf(false) }
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }

    val openProfile = { showProfileSheet = true }
    val openNotifications = { showNotificationsSheet = true }

    val onLogoutSuccess = {
        sessionManager.clearUser()
        navController.navigate(Screen.RoleSelection.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Screen.Home.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    sessionManager.setOnboardingCompleted(true)
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
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
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            AuthSignUpScreen(
                selectedRole = selectedRole,
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            if (isCaregiver) {
                CaregiverDashboardScreen(
                    onNavigateToHistory = { navigateToTab(Screen.History.route) },
                    onNavigateToPrescriptions = { navigateToTab(Screen.Prescriptions.route) },
                    onNavigateToSettings = { navigateToTab(Screen.Settings.route) },
                    onProfileClick = openProfile,
                    onNotificationsClick = openNotifications
                )
            } else {
                PatientDashboardScreen(
                    onNavigateToHistory = { navigateToTab(Screen.History.route) },
                    onNavigateToPrescriptions = { navigateToTab(Screen.Prescriptions.route) },
                    onNavigateToSettings = { navigateToTab(Screen.Settings.route) },
                    onProfileClick = openProfile,
                    onNotificationsClick = openNotifications
                )
            }
        }

        composable(Screen.History.route) {
            if (isCaregiver) {
                CaregiverHistoryScreen(
                    onNavigateToHome = { navigateToTab(Screen.Home.route) },
                    onNavigateToPrescriptions = { navigateToTab(Screen.Prescriptions.route) },
                    onNavigateToSettings = { navigateToTab(Screen.Settings.route) },
                    onProfileClick = openProfile,
                    onNotificationsClick = openNotifications
                )
            } else {
                PatientHistoryScreen(
                    onNavigateToHome = { navigateToTab(Screen.Home.route) },
                    onNavigateToPrescriptions = { navigateToTab(Screen.Prescriptions.route) },
                    onNavigateToSettings = { navigateToTab(Screen.Settings.route) },
                    onProfileClick = openProfile,
                    onNotificationsClick = openNotifications
                )
            }
        }

        composable(Screen.Prescriptions.route) {
            if (isCaregiver) {
                CaregiverPrescriptionsScreen(
                    onNavigateToHome = { navigateToTab(Screen.Home.route) },
                    onNavigateToHistory = { navigateToTab(Screen.History.route) },
                    onNavigateToSettings = { navigateToTab(Screen.Settings.route) },
                    onProfileClick = openProfile,
                    onNotificationsClick = openNotifications
                )
            } else {
                PatientPrescriptionsScreen(
                    onNavigateToHome = { navigateToTab(Screen.Home.route) },
                    onNavigateToHistory = { navigateToTab(Screen.History.route) },
                    onNavigateToSettings = { navigateToTab(Screen.Settings.route) },
                    onProfileClick = openProfile,
                    onNotificationsClick = openNotifications
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
                    onNavigateToHome = { navigateToTab(Screen.Home.route) },
                    onNavigateToHistory = { navigateToTab(Screen.History.route) },
                    onNavigateToPrescriptions = { navigateToTab(Screen.Prescriptions.route) },
                    onSignOutSuccess = onLogoutSuccess,
                    onProfileClick = openProfile,
                    onNotificationsClick = openNotifications
                )
            } else {
                PatientSettingsScreen(
                    onNavigateToHome = { navigateToTab(Screen.Home.route) },
                    onNavigateToHistory = { navigateToTab(Screen.History.route) },
                    onNavigateToPrescriptions = { navigateToTab(Screen.Prescriptions.route) },
                    onSignOutSuccess = onLogoutSuccess,
                    onProfileClick = openProfile,
                    onNotificationsClick = openNotifications
                )
            }
        }
    }

    if (showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            QuickProfileSheet(
                user = currentUser,
                onOpenSettings = {
                    showProfileSheet = false
                    navController.navigate(Screen.Settings.route)
                },
                onSignOut = {
                    showProfileSheet = false
                    coroutineScope.launch {
                        logoutUseCase()
                        onLogoutSuccess()
                    }
                }
            )
        }
    }

    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            NotificationsSheet()
        }
    }
}