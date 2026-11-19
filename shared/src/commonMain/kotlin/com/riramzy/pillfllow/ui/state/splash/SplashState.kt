package com.riramzy.pillfllow.ui.state.splash

data class SplashState(
    val isLoading: Boolean = true
)

sealed interface SplashAction {
    data object CheckSession : SplashAction
}

sealed interface SplashNavEvent {
    data object NavigateToHome : SplashNavEvent
    data object NavigateToSignIn : SplashNavEvent
    data object NavigateToOnboarding : SplashNavEvent
    data object NavigateToCompleteProfile : SplashNavEvent
}