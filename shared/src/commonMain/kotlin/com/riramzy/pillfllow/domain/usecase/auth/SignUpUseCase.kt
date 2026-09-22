package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.Result
import com.riramzy.pillfllow.utils.UserType

class SignUpUseCase(
    private val authRepo: AuthRepo,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        role: UserType,
        phoneNumber: String = "+1234567890",
        avatarRes: String = "avatar1"
    ): Result<UserEntity> {
        val result = authRepo.signUp(email, pass, firstName, lastName, role, phoneNumber, avatarRes)

        if (result is Result.Success) {
            sessionManager.setUser(result.data)
        }

        return result
    }
}