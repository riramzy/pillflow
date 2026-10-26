package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.domain.validation.AuthValidator
import com.riramzy.pillfllow.utils.app.Result

class SignInUseCase(
    private val authRepo: AuthRepo,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String, password: String): Result<UserEntity> {
        when (val validation = AuthValidator.validateSignIn(email, password)) {
            is AuthValidator.Result.Invalid -> return Result.Error(
                exception = IllegalArgumentException(validation.reason),
                message = validation.reason
            )
            AuthValidator.Result.Valid -> Unit
        }

        val result = authRepo.signIn(email.trim(), password.trim())

        if (result is Result.Success) {
            sessionManager.setUser(result.data)
        }

        return result
    }
}