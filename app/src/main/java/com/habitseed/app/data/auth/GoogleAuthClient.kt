package com.habitseed.app.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.habitseed.app.R
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class GoogleAuthClient @Inject constructor(
    private val credentialManager: CredentialManager,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun signIn(context: Context): Result<AuthUser> {
        return runCatching {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = response.credential

            if (
                credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                throw AuthException("Google sign-in returned an unsupported credential.")
            }

            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
            val firebaseUser = firebaseAuth.signInWithCredential(firebaseCredential).await().user
                ?: throw AuthException("Google sign-in did not return a Firebase user.")

            firebaseUser.toAuthUser()
        }.recoverCatching { error ->
            throw when (error) {
                is GetCredentialCancellationException -> AuthException("Google sign-in was cancelled.")
                is NoCredentialException -> AuthException("No Google account was available. Add an account and try again.")
                is GoogleIdTokenParsingException -> AuthException("Google sign-in response could not be verified.")
                is GetCredentialException -> AuthException("Google sign-in failed. Check your connection and try again.")
                is AuthException -> error
                else -> AuthException(error.message ?: "Google sign-in failed. Please try again.")
            }
        }
    }

    suspend fun clearCredentialState() {
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl?.toString(),
            isEmailVerified = isEmailVerified
        )
    }
}

class AuthException(message: String) : Exception(message)
