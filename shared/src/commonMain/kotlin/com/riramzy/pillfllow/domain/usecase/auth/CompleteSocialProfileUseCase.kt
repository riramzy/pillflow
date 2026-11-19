package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.domain.validation.AuthValidator
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.platform.Country
import com.riramzy.pillfllow.utils.user.UserType

class CompleteSocialProfileUseCase(
    private val authRepo: AuthRepo,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        role: UserType,
        phoneNumber: String,
        country: Country,
        avatarRes: String = "avatar1"
    ): Result<UserEntity> {
        when (
            val validation = AuthValidator.validateProfileCompletion(
                firstName = firstName,
                lastName = lastName,
                email = email,
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

        val result = authRepo.completeSocialProfile(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
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