package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.Flow

interface EmailLinkAuthClient {
    val isConfigured: Boolean

    fun observeCurrentUser(): Flow<AuthUser?>

    suspend fun sendSignInLink(email: String)

    fun isSignInLink(link: String): Boolean

    suspend fun signInWithEmailLink(
        email: String,
        link: String,
    ): AuthUser

    suspend fun signOut()
}
