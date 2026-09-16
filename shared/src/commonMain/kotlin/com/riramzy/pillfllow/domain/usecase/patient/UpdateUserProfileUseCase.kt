package com.riramzy.pillfllow.domain.usecase.patient

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.Result
import com.riramzy.pillfllow.utils.safeCall

class UpdateUserProfileUseCase(
    private val userRepo: UserRepo
) {
    suspend operator fun invoke(
        currentUser: UserEntity,
        firstName: String,
        lastName: String,
        email: String,
        avatarRes: String
    ): Result<Unit> = safeCall {
        val updated = currentUser.copy(
            firstName = firstName,
            lastName = lastName,
            email = email,
            avatarRes = avatarRes
        )

        userRepo.updateUser(updated)
    }
}