package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.ui.state.splash.SplashNavEvent

class ResolveStartDestinationUseCase(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val sessionManager: SessionManager,
    private val authRepo: AuthRepo
) {
    suspend operator fun invoke(): SplashNavEvent {
        val user = runCatching { observeCurrentUserUseCase.once() }.getOrNull()
        if (user != null) return SplashNavEvent.NavigateToHome

        val hasIncomplete = runCatching {
            authRepo.hasIncompleteAuthSession()
        }.getOrDefault(false)

        return when {
            hasIncomplete -> SplashNavEvent.NavigateToCompleteProfile
            sessionManager.isOnboardingCompleted() -> SplashNavEvent.NavigateToSignIn
            else -> SplashNavEvent.NavigateToOnboarding
        }
    }
}