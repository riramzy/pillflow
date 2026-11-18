package com.riramzy.pillfllow.data

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.riramzy.pillfllow.domain.auth.NativeAuthResult
import com.riramzy.pillfllow.domain.auth.SocialAuthBridge
import com.riramzy.pillfllow.domain.auth.SocialAuthPayload
import com.riramzy.pillfllow.domain.auth.SocialProvider
import com.riramzy.pillfllow.utils.platform.AndroidActivityTracker

class AndroidSocialAuthBridge(
    private val context: Context,
    private val activityTracker: AndroidActivityTracker
): SocialAuthBridge {
    override val supportsAppleSignIn: Boolean = false
    override val supportsGoogleSignIn: Boolean = true

    private val credentialManager = CredentialManager.create(context)

    override suspend fun signInWithGoogle(): NativeAuthResult {
        val activity = activityTracker.currentActivity
            ?: return NativeAuthResult.Error(IllegalStateException("No active screen available"))

        val webClientIdRes = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )

        if (webClientIdRes == 0)
            return NativeAuthResult.Error(IllegalStateException("Missing default_web_client_id"))

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(webClientIdRes))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response = credentialManager.getCredential(request = request, context = activity)
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)

                NativeAuthResult.Success(
                    SocialAuthPayload(
                        provider = SocialProvider.GOOGLE,
                        idToken = googleCredential.idToken,
                        firstName = googleCredential.givenName,
                        lastName = googleCredential.familyName,
                        email = googleCredential.email
                    )
                )
            } else {
                NativeAuthResult.Error(IllegalStateException("Unexpected credential type"))
            }
        } catch (_: GetCredentialCancellationException) {
            NativeAuthResult.Cancelled
        } catch (e: Exception) {
            NativeAuthResult.Error(e)
        }
    }

    override suspend fun signInWithApple(): NativeAuthResult =
        NativeAuthResult.Error(UnsupportedOperationException("Apple Sign-In is only supported on iOS"))

    override suspend fun signOut() {
        runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
    }
}