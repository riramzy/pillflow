package com.riramzy.pillfllow.data

import com.riramzy.pillfllow.domain.auth.NativeAuthResult
import com.riramzy.pillfllow.domain.auth.SocialAuthBridge
import com.riramzy.pillfllow.domain.auth.SocialAuthPayload
import com.riramzy.pillfllow.domain.auth.SocialProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.concurrent.Volatile
import kotlin.coroutines.resume

interface IosSocialAuthDelegate {
    val supportsApple: Boolean

    fun startGoogleSignIn(
        onSuccess: (idToken: String, accessToken: String?, firstName: String?, lastName: String?, email: String?) -> Unit,
        onCancel: () -> Unit,
        onError: (String) -> Unit
    )

    fun startAppleSignIn(
        onSuccess: (idToken: String, rawNonce: String, firstName: String?, lastName: String?, email: String?) -> Unit,
        onCancel: () -> Unit,
        onError: (String) -> Unit
    )

    fun startGoogleSignOut()
}

class IosSocialAuthBridge: SocialAuthBridge {
    @Volatile
    private var delegate: IosSocialAuthDelegate? = null

    fun attachDelegate(delegate: IosSocialAuthDelegate) {
        this.delegate = delegate
    }

    override val supportsGoogleSignIn: Boolean
        get() = delegate != null

    override val supportsAppleSignIn: Boolean
        get() = delegate?.supportsApple == true

    override suspend fun signInWithGoogle(): NativeAuthResult {
        val activeDelegate = delegate
            ?: return NativeAuthResult.Error(UnsupportedOperationException("Google Sign-In is not configured on iOS yet"))

        return suspendCancellableCoroutine { cont ->
            activeDelegate.startGoogleSignIn(
                onSuccess = { idToken, accessToken, firstName, lastName, email ->
                    if (cont.isActive) {
                        cont.resume(
                            NativeAuthResult.Success(
                                SocialAuthPayload(
                                    provider = SocialProvider.GOOGLE,
                                    idToken = idToken,
                                    accessToken = accessToken,
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email
                                )
                            )
                        )
                    }
                },
                onCancel = { if (cont.isActive) cont.resume(NativeAuthResult.Cancelled) },
                onError = { msg ->
                    if (cont.isActive)
                        cont.resume(NativeAuthResult.Error(IllegalStateException(msg)))
                }
            )
        }
    }

    override suspend fun signInWithApple(): NativeAuthResult {
        val activeDelegate = delegate
            ?: return NativeAuthResult.Error(UnsupportedOperationException("Apple Sign-In is not configured on iOS yet"))

        return suspendCancellableCoroutine { cont ->
            activeDelegate.startAppleSignIn(
                onSuccess = { idToken, rawNonce, firstName, lastName, email ->
                    if (cont.isActive) {
                        cont.resume(
                            NativeAuthResult.Success(
                                SocialAuthPayload(
                                    provider = SocialProvider.APPLE,
                                    idToken = idToken,
                                    rawNonce = rawNonce,
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email
                                )
                            )
                        )
                    }
                },
                onCancel = { if (cont.isActive) cont.resume(NativeAuthResult.Cancelled) },
                onError = { msg ->
                    if (cont.isActive)
                        cont.resume(NativeAuthResult.Error(IllegalStateException(msg)))
                }
            )
        }
    }

    override suspend fun signOut() {
        delegate?.startGoogleSignOut()
    }
}