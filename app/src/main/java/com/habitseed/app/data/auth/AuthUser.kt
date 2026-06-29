package com.habitseed.app.data.auth

data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean
)
