package com.habitseed.app.data.auth

import android.content.Context

interface AuthRepository {
    fun currentUser(): AuthUser?
    fun isSignedInWithGoogle(): Boolean
    suspend fun signInWithGoogle(context: Context): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
}
