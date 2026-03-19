package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.first

class SyncStartupCoordinator(
    private val authSessionRepository: AuthSessionRepository,
    private val cloudSyncManager: CloudSyncManager,
) {
    suspend fun handleIncomingAuthLink(link: String?) {
        if (!link.isNullOrBlank()) {
            authSessionRepository.completeEmailLinkSignIn(link)
        }
        syncSignedInSelfProfile()
    }

    suspend fun syncSignedInSelfProfile() {
        authSessionRepository.observeSession().first { session ->
            session.status == AuthSessionStatus.SignedIn && !session.userId.isNullOrBlank()
        }
        cloudSyncManager.bootstrapSignedInUser()
    }
}
