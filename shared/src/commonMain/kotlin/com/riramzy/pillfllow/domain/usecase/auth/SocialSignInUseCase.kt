package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.domain.auth.NativeAuthResult
import com.riramzy.pillfllow.domain.auth.SocialAuthBridge
import com.riramzy.pillfllow.domain.auth.SocialProvider
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.SocialSignInOutcome
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.app.ErrorMapper
import com.riramzy.pillfllow.utils.app.Result

class SocialSignInUseCase(
    private val socialAuthBridge: SocialAuthBridge,
    private val authRepo: AuthRepo,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(provider: SocialProvider): Result<SocialSignInOutcome> {
        val nativeResult = when (provider) {
            SocialProvider.GOOGLE -> socialAuthBridge.signInWithGoogle()
            SocialProvider.APPLE -> socialAuthBridge.signInWithApple()
        }

        return when (nativeResult) {
            is NativeAuthResult.Cancelled -> Result.Success(SocialSignInOutcome.Cancelled)
            is NativeAuthResult.Error -> Result.Error(
                exception = nativeResult.throwable,
                message = ErrorMapper.mapAuthError(nativeResult.throwable)
            )
            is NativeAuthResult.Success -> {
                val repoResult = authRepo.signInWithSocial(nativeResult.payload)
                if (repoResult is Result.Success && repoResult.data is SocialSignInOutcome.ExistingUser) {
                    sessionManager.setUser(repoResult.data.user)
                }
                repoResult
            }
        }
    }
}