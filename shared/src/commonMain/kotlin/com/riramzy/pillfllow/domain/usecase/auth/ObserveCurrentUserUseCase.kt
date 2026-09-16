package com.riramzy.pillfllow.domain.usecase.auth

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.session.SessionManager
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase(
    private val sessionManager: SessionManager,
    private val authRepo: AuthRepo
) {
    operator fun invoke(): Flow<UserEntity?> = sessionManager.currentUser

    suspend fun once(): UserEntity? {
        val memoryUser = sessionManager.currentUser.value
        if (memoryUser != null) return memoryUser

        val user = authRepo.getCurrentUser()
        user?.let { sessionManager.setUser(it) }

        return user
    }
}