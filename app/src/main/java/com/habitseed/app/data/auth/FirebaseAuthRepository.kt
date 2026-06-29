package com.habitseed.app.data.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleAuthClient: GoogleAuthClient
) : AuthRepository {
    override fun currentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toAuthUser()
    }

    override fun isSignedInWithGoogle(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> {
        return googleAuthClient.signIn(context)
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            firebaseAuth.signOut()
            googleAuthClient.clearCredentialState()
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
