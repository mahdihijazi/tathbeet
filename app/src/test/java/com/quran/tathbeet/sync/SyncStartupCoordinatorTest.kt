package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncStartupCoordinatorTest {

    @Test
    fun handleIncomingAuthLink_completes_sign_in_before_bootstrapping() = runTest {
        val authRepository = FakeStartupAuthRepository()
        val cloudSyncManager = FakeStartupCloudSyncManager()
        val coordinator = SyncStartupCoordinator(
            authSessionRepository = authRepository,
            cloudSyncManager = cloudSyncManager,
        )

        coordinator.handleIncomingAuthLink("https://example.com/auth")

        assertEquals(listOf("https://example.com/auth"), authRepository.completedLinks)
        assertEquals(1, cloudSyncManager.bootstrapCalls)
    }

    @Test
    fun handleIncomingAuthLink_skips_completion_for_blank_link_but_still_bootstraps() = runTest {
        val authRepository = FakeStartupAuthRepository()
        val cloudSyncManager = FakeStartupCloudSyncManager()
        val coordinator = SyncStartupCoordinator(
            authSessionRepository = authRepository,
            cloudSyncManager = cloudSyncManager,
        )

        coordinator.handleIncomingAuthLink("   ")

        assertTrue(authRepository.completedLinks.isEmpty())
        assertEquals(1, cloudSyncManager.bootstrapCalls)
    }

    @Test
    fun syncSignedInSelfProfile_waits_for_session_before_bootstrapping() = runTest {
        val authRepository = StreamingStartupAuthRepository()
        val cloudSyncManager = FakeStartupCloudSyncManager()
        val coordinator = SyncStartupCoordinator(
            authSessionRepository = authRepository,
            cloudSyncManager = cloudSyncManager,
        )

        val job = launch {
            coordinator.syncSignedInSelfProfile()
        }
        advanceUntilIdle()

        assertFalse(cloudSyncManager.bootstrapStarted)

        authRepository.session.emit(
            AuthSessionState(
                isRuntimeConfigured = true,
                status = AuthSessionStatus.SignedIn,
                email = "owner@example.com",
                userId = "uid-owner",
                pendingEmail = null,
            ),
        )
        advanceUntilIdle()

        assertTrue(cloudSyncManager.bootstrapStarted)
        assertEquals(1, cloudSyncManager.bootstrapCalls)
        job.cancel()
    }

    @Test
    fun syncSignedInSelfProfile_ignores_signed_out_session_until_signed_in() = runTest {
        val authRepository = StreamingStartupAuthRepository(replay = 1).apply {
            session.tryEmit(
                AuthSessionState(
                    isRuntimeConfigured = true,
                    status = AuthSessionStatus.SignedOut,
                    email = null,
                    userId = null,
                    pendingEmail = null,
                ),
            )
        }
        val cloudSyncManager = FakeStartupCloudSyncManager()
        val coordinator = SyncStartupCoordinator(
            authSessionRepository = authRepository,
            cloudSyncManager = cloudSyncManager,
        )

        val job = launch {
            coordinator.syncSignedInSelfProfile()
        }
        advanceUntilIdle()

        assertFalse(cloudSyncManager.bootstrapStarted)

        authRepository.session.emit(
            AuthSessionState(
                isRuntimeConfigured = true,
                status = AuthSessionStatus.SignedIn,
                email = "owner@example.com",
                userId = "uid-owner",
                pendingEmail = null,
            ),
        )
        advanceUntilIdle()

        assertTrue(cloudSyncManager.bootstrapStarted)
        assertEquals(1, cloudSyncManager.bootstrapCalls)
        job.cancel()
    }
}

private class FakeStartupAuthRepository(
    private val sessionState: AuthSessionState = AuthSessionState(
        isRuntimeConfigured = true,
        status = AuthSessionStatus.SignedIn,
        email = "owner@example.com",
        userId = "uid-owner",
        pendingEmail = null,
    ),
) : AuthSessionRepository {
    val completedLinks = mutableListOf<String>()

    override fun observeSession(): Flow<AuthSessionState> = flowOf(sessionState)

    override suspend fun requestEmailLink(email: String): EmailLinkRequestResult =
        EmailLinkRequestResult.Success

    override suspend fun completeEmailLinkSignIn(link: String): EmailLinkCompletionResult {
        completedLinks += link
        return EmailLinkCompletionResult.Success
    }

    override suspend fun signOut() = Unit
}

private class StreamingStartupAuthRepository(
    replay: Int = 0,
) : AuthSessionRepository {
    val session = MutableSharedFlow<AuthSessionState>(replay = replay)

    override fun observeSession(): Flow<AuthSessionState> = session

    override suspend fun requestEmailLink(email: String): EmailLinkRequestResult =
        EmailLinkRequestResult.Success

    override suspend fun completeEmailLinkSignIn(link: String): EmailLinkCompletionResult =
        EmailLinkCompletionResult.Success

    override suspend fun signOut() = Unit
}

private class FakeStartupCloudSyncManager : CloudSyncManager(
    authSessionRepository = object : AuthSessionRepository {
        override fun observeSession(): Flow<AuthSessionState> = flowOf(
            AuthSessionState(
                isRuntimeConfigured = true,
                status = AuthSessionStatus.SignedIn,
                email = "owner@example.com",
                userId = "uid-owner",
                pendingEmail = null,
            ),
        )

        override suspend fun requestEmailLink(email: String): EmailLinkRequestResult =
            EmailLinkRequestResult.Success

        override suspend fun completeEmailLinkSignIn(link: String): EmailLinkCompletionResult =
            EmailLinkCompletionResult.Success

        override suspend fun signOut() = Unit
    },
    profileSyncCoordinator = object : ProfileSyncCoordinator(
        profileRepository = object : SyncProfileRepository {
            override suspend fun getAccount(accountId: String): LearnerAccount? = null

            override suspend fun getSelfProfile(): LearnerAccount? = null

            override suspend fun updateSyncState(
                accountId: String,
                syncMode: ProfileSyncMode,
                cloudProfileId: String?,
                isShared: Boolean,
            ) = Unit
        },
        scheduleRepository = object : SyncScheduleRepository {
            override suspend fun getActiveSchedule(learnerId: String): RevisionSchedule? = null
        },
        reviewRepository = object : SyncReviewRepository {
            override suspend fun getReviewTimeline(learnerId: String): List<ReviewDay> = emptyList()
        },
        cloudSyncStore = DisabledCloudSyncStore,
    ) {},
    profileRepository = object : SyncProfileRepository {
        override suspend fun getAccount(accountId: String): LearnerAccount? = null

        override suspend fun getSelfProfile(): LearnerAccount? = null

        override suspend fun updateSyncState(
            accountId: String,
            syncMode: ProfileSyncMode,
            cloudProfileId: String?,
            isShared: Boolean,
        ) = Unit
    },
    cloudSyncStore = DisabledCloudSyncStore,
    localMirror = object : LocalCloudProfileMirror {
        override suspend fun hasMeaningfulLocalData(accountId: String): Boolean = false

        override suspend fun findAccountByCloudProfileId(cloudProfileId: String): LearnerAccount? = null

        override suspend fun importSelfProfile(snapshot: CloudProfileSnapshot) = Unit

        override suspend fun importSharedProfile(
            snapshot: CloudProfileSnapshot,
            syncMode: ProfileSyncMode,
        ) = Unit

        override suspend fun removeStaleSharedProfiles(allowedCloudProfileIds: Set<String>) = Unit

        override suspend fun removeSharedProfile(cloudProfileId: String) = Unit
    },
) {
    var bootstrapCalls = 0
    var bootstrapStarted = false

    override suspend fun bootstrapSignedInUser() {
        bootstrapStarted = true
        bootstrapCalls++
    }
}
