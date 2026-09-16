package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.Result

class SignInUseCase(
    private val authRepo: AuthRepo,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String, password: String): Result<UserEntity> {
        val result = authRepo.signIn(email, password)

        if (result is Result.Success) {
            sessionManager.setUser(result.data)
        }

        return result
    }
}