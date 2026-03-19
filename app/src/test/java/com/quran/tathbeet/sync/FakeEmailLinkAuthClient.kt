package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeEmailLinkAuthClient(
    override var isConfigured: Boolean = true,
) : EmailLinkAuthClient {
    private val currentUser = MutableStateFlow<AuthUser?>(null)
    val sentSignInLinks = mutableListOf<String>()
    val signInEmails = mutableListOf<String>()

    override fun observeCurrentUser(): Flow<AuthUser?> = currentUser

    override suspend fun sendSignInLink(email: String) {
        sentSignInLinks += email
    }

    override fun isSignInLink(link: String): Boolean = link.startsWith("https://")

    override suspend fun signInWithEmailLink(
        email: String,
        link: String,
    ): AuthUser {
        signInEmails += email
        val user = AuthUser(
            uid = "uid-${email.substringBefore('@')}",
            email = email,
        )
        currentUser.value = user
        return user
    }

    override suspend fun signOut() {
        currentUser.value = null
    }
}
