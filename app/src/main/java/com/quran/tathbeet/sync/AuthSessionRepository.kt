package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.Flow

interface AuthSessionRepository {
    fun observeSession(): Flow<AuthSessionState>

    suspend fun requestEmailLink(email: String): EmailLinkRequestResult

    suspend fun completeEmailLinkSignIn(link: String): EmailLinkCompletionResult

    suspend fun signOut()
}
