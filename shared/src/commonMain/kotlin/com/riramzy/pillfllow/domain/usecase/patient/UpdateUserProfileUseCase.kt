package com.riramzy.pillfllow.domain.usecase.patient

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall

class UpdateUserProfileUseCase(
    private val userRepo: UserRepo,
    private val sessionManager: SessionManager
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
        sessionManager.setUser(updated)
    }
}