package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.data.remote.dto.UserDto
import com.riramzy.pillfllow.data.remote.dto.toDto
import com.riramzy.pillfllow.data.remote.dto.toEntity
import com.riramzy.pillfllow.domain.auth.SocialAuthBridge
import com.riramzy.pillfllow.domain.auth.SocialAuthPayload
import com.riramzy.pillfllow.domain.auth.SocialProvider
import com.riramzy.pillfllow.domain.repo.AuthRepo
import com.riramzy.pillfllow.domain.repo.SocialSignInOutcome
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.app.ErrorMapper
import com.riramzy.pillfllow.utils.app.Result
import com.riramzy.pillfllow.utils.app.safeCall
import com.riramzy.pillfllow.utils.platform.currentTimeMillis
import com.riramzy.pillfllow.utils.user.UserType
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile

class AuthRepoImpl(
    private val firebaseAuth: FirebaseAuth,
    private val userRepo: UserRepo,
    private val firestore: FirebaseFirestore,
    private val socialAuthBridge: SocialAuthBridge
): AuthRepo {
    @Volatile private var pendingIncompleteSession: SocialSignInOutcome.NeedsProfileCompletion? = null
    @Volatile private var pendingSocialPayload: SocialAuthPayload? = null

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

    override suspend fun getCurrentUser(): UserEntity? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        return userRepo.getUserByIdOnce(uid)
    }

    override suspend fun signUp(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        role: UserType,
        phoneNumber: String,
        avatarRes: String
    ): Result<UserEntity> =
        safeCall(errorMapper = ErrorMapper::mapAuthError) {
            withContext(Dispatchers.IO) {
            val now = currentTimeMillis()
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
                phoneNumber = phoneNumber,
                userType = role.toString(),
                createdAt = currentTimeMillis(),
                avatarRes = avatarRes
            )

            val userDto = userEntity.toDto(updatedAt = now)

            try {
                firestore.collection("users")
                    .document(uid)
                    .set(userDto)
            } catch (e: Exception) {
                runCatching { firebaseUser.delete() }
                throw e
            }

            userRepo.insertUser(userEntity)
            userEntity
        }
    }

    override suspend fun signIn(
        email: String,
        pass: String,
    ): Result<UserEntity> =
        safeCall(errorMapper = ErrorMapper::mapAuthError) {
            withContext(Dispatchers.IO) {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass)
            val firebaseUser = authResult.user ?: throw Exception("User ID not found")
            val uid = firebaseUser.uid

            val remoteUser = try {
                val doc = firestore.collection("users").document(uid).get()
                if (doc.exists) doc.data<UserDto>().toEntity() else null
            } catch (e: Exception) {
                userRepo.getUserByIdOnce(uid) ?: throw e
            }

            val finalUser = remoteUser
                ?: userRepo.getUserByIdOnce(uid)
                ?: throw Exception("User profile not found in cloud or local cache")

            userRepo.insertUser(finalUser)
            finalUser
        }
    }

    override suspend fun signOut(): Result<Unit> = safeCall {
        pendingIncompleteSession = null
        pendingSocialPayload = null
        runCatching { socialAuthBridge.signOut() }
        firebaseAuth.signOut()
    }

    override suspend fun signInWithSocial(payload: SocialAuthPayload): Result<SocialSignInOutcome> =
        safeCall(errorMapper = ErrorMapper::mapAuthError) {
            withContext(Dispatchers.IO) {
                val credential = buildSocialCredential(payload)

                val firebaseUser = firebaseAuth.signInWithCredential(credential).user
                    ?: throw Exception("User ID not found")
                val uid = firebaseUser.uid

                val existingUser = try {
                    val doc = firestore
                        .collection("users")
                        .document(uid)
                        .get()
                    if (doc.exists) doc.data<UserDto>().toEntity() else null
                } catch (e: Exception) {
                    userRepo.getUserByIdOnce(uid) ?: throw e
                }

                if (existingUser != null) {
                    pendingIncompleteSession = null
                    pendingSocialPayload = null
                    userRepo.insertUser(existingUser)
                    SocialSignInOutcome.ExistingUser(existingUser)
                } else {
                    val displayParts = firebaseUser.displayName
                        ?.substringBefore("|")
                        ?.trim()
                        ?.split(" ", limit = 2)
                        .orEmpty()

                    val outcome = SocialSignInOutcome.NeedsProfileCompletion(
                        uid = "",
                        email = firebaseUser.email?.takeIf { it.isNotBlank() } ?: payload.email.orEmpty(),
                        suggestedFirstName = payload.firstName?.takeIf { it.isNotBlank() }
                            ?: displayParts.getOrNull(0).orEmpty(),
                        suggestedLastName = payload.lastName?.takeIf { it.isNotBlank() }
                            ?: displayParts.getOrNull(1).orEmpty()
                    )

                    runCatching { firebaseUser.delete() }
                    firebaseAuth.signOut()
                    pendingSocialPayload = payload
                    pendingIncompleteSession = outcome
                    outcome
                }
            }
        }

    override suspend fun completeSocialProfile(
        firstName: String,
        lastName: String,
        email: String,
        role: UserType,
        phoneNumber: String,
        avatarRes: String
    ): Result<UserEntity> =
        safeCall(errorMapper = ErrorMapper::mapAuthError) {
            withContext(Dispatchers.IO) {
                val now = currentTimeMillis()
                val firebaseUser = firebaseAuth.currentUser ?: run {
                    val payload = pendingSocialPayload ?: throw Exception("SOCIAL_SESSION_EXPIRED")
                    firebaseAuth.signInWithCredential(buildSocialCredential(payload)).user
                        ?: throw Exception("User ID not found")
                }
                val uid = firebaseUser.uid

                runCatching {
                    firebaseUser.updateProfile(displayName = "$firstName $lastName|${role.name}")
                }

                val userEntity = UserEntity(
                    id = firebaseUser.uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phoneNumber = phoneNumber,
                    userType = role.toString(),
                    createdAt = now,
                    avatarRes = avatarRes
                )

                val userDto = userEntity.toDto(updatedAt = now)

                try {
                    firestore.collection("users")
                        .document(uid)
                        .set(userDto)
                } catch (e: Exception) {
                    runCatching { firebaseUser.delete() }
                    firebaseAuth.signOut()
                    throw e
                }

                userRepo.insertUser(userEntity)
                pendingSocialPayload = null
                pendingIncompleteSession = null
                userEntity
            }
        }

    override suspend fun hasIncompleteAuthSession(): Boolean {
        val uid = firebaseAuth.currentUser?.uid ?: return false
        return userRepo.getUserByIdOnce(uid) == null
    }

    override suspend fun getIncompleteAuthSession(): SocialSignInOutcome.NeedsProfileCompletion? {
        pendingIncompleteSession?.let { return it }
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val uid = firebaseUser.uid

        if (userRepo.getUserByIdOnce(uid) != null) {
            pendingIncompleteSession = null
            return null
        }

        pendingIncompleteSession?.takeIf { it.uid == uid }?.let { return it }

        val displayParts = firebaseUser.displayName
            ?.substringBefore("|")
            ?.trim()
            ?.split(" ", limit = 2)
            .orEmpty()

        return SocialSignInOutcome.NeedsProfileCompletion(
            uid = uid,
            email = firebaseUser.email.orEmpty(),
            suggestedFirstName = displayParts.getOrNull(0).orEmpty(),
            suggestedLastName = displayParts.getOrNull(1).orEmpty()
        )
    }

    private fun buildSocialCredential(payload: SocialAuthPayload): AuthCredential =
        when(payload.provider) {
            SocialProvider.APPLE -> OAuthProvider.credential(
                providerId = "apple.com",
                idToken = payload.idToken,
                accessToken = payload.accessToken,
                rawNonce = payload.rawNonce
            )
            SocialProvider.GOOGLE -> GoogleAuthProvider.credential(
                idToken = payload.idToken,
                accessToken = payload.accessToken
            )
        }
}