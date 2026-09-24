package com.riramzy.pillfllow.ui.screens.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.riramzy.pillfllow.ui.components.custom.PillFlowLoadingCard
import com.riramzy.pillfllow.ui.state.splash.SplashNavEvent
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
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

            is SplashNavEvent.NavigateToRoleSelection -> {
                navController.navigate(Screen.RoleSelection.route) {
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

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    PillFlowTheme {
        SplashScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SplashScreenPreviewDark() {
    PillFlowTheme {
        SplashScreen(navController = rememberNavController())
    }
}