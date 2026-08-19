package com.riramzy.pillfllow.domain.repo

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.utils.UserType
import kotlinx.coroutines.flow.Flow

interface AuthRepo {
    val currentUser: Flow<UserEntity?>

    val isUserLoggedIn: Flow<Boolean>

    suspend fun signUp(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        role: UserType
    ): Result<UserEntity>

    suspend fun signIn(
        email: String,
        pass: String
    ): Result<UserEntity>

    suspend fun signOut(): Result<Unit>
}