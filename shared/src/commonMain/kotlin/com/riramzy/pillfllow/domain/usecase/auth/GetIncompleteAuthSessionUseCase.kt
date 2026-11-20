package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.SocialSignInOutcome

class GetIncompleteAuthSessionUseCase(
    private val authRepo: AuthRepo
) {
    suspend operator fun invoke(): SocialSignInOutcome.NeedsProfileCompletion? =
        authRepo.getIncompleteAuthSession()
}