package com.riramzy.pillfllow.domain.auth

enum class SocialProvider {
    GOOGLE,
    APPLE
}

data class SocialAuthPayload(
    val provider: SocialProvider,
    val idToken: String,
    val accessToken: String? = null,
    val rawNonce: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
)

sealed interface NativeAuthResult {
    data class Success(val payload: SocialAuthPayload): NativeAuthResult
    data object Cancelled: NativeAuthResult
    data class Error(val throwable: Throwable): NativeAuthResult

}

interface SocialAuthBridge {
    val supportsGoogleSignIn: Boolean
    val supportsAppleSignIn: Boolean

    suspend fun signInWithGoogle(): NativeAuthResult
    suspend fun signInWithApple(): NativeAuthResult
    suspend fun signOut()
}