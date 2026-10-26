package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.ui.state.splash.SplashNavEvent

class ResolveStartDestinationUseCase(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): SplashNavEvent {
        val user = runCatching { observeCurrentUserUseCase.once() }.getOrNull()

        return when {
            user != null -> SplashNavEvent.NavigateToHome
            sessionManager.isOnboardingCompleted() -> SplashNavEvent.NavigateToRoleSelection
            else -> SplashNavEvent.NavigateToOnboarding
        }
    }
}