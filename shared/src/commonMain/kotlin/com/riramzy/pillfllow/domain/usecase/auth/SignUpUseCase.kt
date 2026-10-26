package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.domain.validation.AuthValidator
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.platform.Country
import com.riramzy.pillfllow.utils.user.UserType

class SignUpUseCase(
    private val authRepo: AuthRepo,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        email: String,
        pass: String,
        confirmPass: String,
        firstName: String,
        lastName: String,
        role: UserType,
        phoneNumber: String,
        country: Country,
        avatarRes: String = "avatar1"
    ): Result<UserEntity> {
        when (
            val validation = AuthValidator.validateSignUp(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = pass,
                confirmPassword = confirmPass,
                phone = phoneNumber,
                country = country
            )
        ) {
            is AuthValidator.Result.Invalid -> return Result.Error(
                exception = IllegalArgumentException(validation.reason),
                message = validation.reason
            )
            AuthValidator.Result.Valid -> Unit
        }

        val fullPhone = AuthValidator.formatE164Phone(country, phoneNumber)

        val result = authRepo.signUp(
            email = email.trim(),
            pass = pass.trim(),
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            role = role,
            phoneNumber = fullPhone,
            avatarRes = avatarRes
        )

        if (result is Result.Success) {
            sessionManager.setUser(result.data)
        }

        return result
    }
}