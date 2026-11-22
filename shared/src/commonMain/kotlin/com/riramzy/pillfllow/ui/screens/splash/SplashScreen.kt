package com.riramzy.pillfllow.ui.screens.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.riramzy.pillfllow.ui.components.custom.PillFlowLoadingCard
import com.riramzy.pillfllow.ui.state.splash.SplashNavEvent
import com.riramzy.pillfllow.ui.viewmodel.splash.SplashViewModel
import com.riramzy.pillfllow.utils.app.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    navController: NavHostController,
    splashViewModel: SplashViewModel = koinViewModel()
) {
    val state by splashViewModel.state.collectAsStateWithLifecycle()
    val navEvent by splashViewModel.navEvent.collectAsStateWithLifecycle()

    LaunchedEffect(navEvent) {
        when(navEvent) {
            is SplashNavEvent.NavigateToHome -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            is SplashNavEvent.NavigateToSignIn -> {
                navController.navigate(Screen.SignIn.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            is SplashNavEvent.NavigateToCompleteProfile -> {
                navController.navigate(Screen.CompleteProfile.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            is SplashNavEvent.NavigateToOnboarding -> {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }

            null -> Unit
        }
    }

    if (state.isLoading) {
        PillFlowLoadingCard(message = "Loading PillFlow...")
    }
}