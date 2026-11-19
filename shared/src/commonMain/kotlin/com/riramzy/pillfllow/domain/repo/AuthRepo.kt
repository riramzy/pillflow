package com.riramzy.pillfllow.domain.repo

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.auth.SocialAuthPayload
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.user.UserType
import kotlinx.coroutines.flow.Flow

interface AuthRepo {
    val currentUser: Flow<UserEntity?>

    val isUserLoggedIn: Flow<Boolean>

    suspend fun getCurrentUser(): UserEntity?

    suspend fun signUp(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        role: UserType,
        phoneNumber: String,
        avatarRes: String
    ): Result<UserEntity>

    suspend fun signIn(
        email: String,
        pass: String
    ): Result<UserEntity>

    suspend fun signOut(): Result<Unit>

    suspend fun signInWithSocial(payload: SocialAuthPayload): Result<SocialSignInOutcome>

    suspend fun completeSocialProfile(
        firstName: String,
        lastName: String,
        email: String,
        role: UserType,
        phoneNumber: String,
        avatarRes: String
    ): Result<UserEntity>

    suspend fun hasIncompleteAuthSession(): Boolean
}

sealed interface SocialSignInOutcome {
    data class ExistingUser(val user: UserEntity): SocialSignInOutcome
    data class NeedsProfileCompletion(
        val uid: String,
        val email: String,
        val suggestedFirstName: String,
        val suggestedLastName: String
    ): SocialSignInOutcome
    data object Cancelled: SocialSignInOutcome
}