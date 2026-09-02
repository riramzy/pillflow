package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.UserType
import com.riramzy.pillfllow.utils.currentTimeMillis
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class AuthRepoImpl(
    private val firebaseAuth: FirebaseAuth,
    private val userRepo: UserRepo
): AuthRepo {
    override val isUserLoggedIn: Flow<Boolean> =
        firebaseAuth.authStateChanged.map { it != null }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<UserEntity?> =
        firebaseAuth.authStateChanged.flatMapLatest { firebaseUser ->
            if (firebaseUser != null) {
                userRepo.getUserById(firebaseUser.uid)
            } else {
                flowOf(null)
            }
        }

    override suspend fun signUp(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        role: UserType
    ): Result<UserEntity> = runCatching {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass)
        val firebaseUser = authResult.user ?: throw Exception("User ID not found")
        val uid = firebaseUser.uid

        try {
            firebaseUser.updateProfile(displayName = "$firstName $lastName|${role.name}")
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to update user profile")
        }

        val userEntity = UserEntity(
            id = uid,
            firstName = firstName,
            lastName = lastName,
            email = email,
            userType = role.toString(),
            createdAt = currentTimeMillis(),
            avatarRes = "avatar1"
        )

        userRepo.insertUser(userEntity)
        userEntity
    }

    override suspend fun signIn(
        email: String,
        pass: String,
    ): Result<UserEntity> = runCatching {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass)
        val firebaseUser = authResult.user ?: throw Exception("User ID not found")
        val uid = firebaseUser.uid

        val existingUser = userRepo.getUserByIdOnce(uid)
        if (existingUser != null && existingUser.firstName.isNotBlank()) {
            return@runCatching existingUser
        }

        val displayName = firebaseUser.displayName ?: ""
        val nameParts = displayName.substringBefore("|").trim().split(" ")
        val roleStr = displayName.substringAfter("|", "PATIENT")

        val fallbackFirstName = firebaseUser.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
            ?: "User"

        val firstName = nameParts.firstOrNull()?.ifBlank { null } ?: fallbackFirstName
        val lastName = nameParts.drop(1).joinToString(" ").ifBlank { "" }

        val userEntity = UserEntity(
            id = uid,
            firstName = firstName,
            lastName = lastName,
            email = firebaseUser.email ?: email,
            userType = roleStr,
            createdAt = currentTimeMillis(),
            avatarRes = "avatar1"
        )

        userRepo.insertUser(userEntity)
        userEntity
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        firebaseAuth.signOut()
    }
}