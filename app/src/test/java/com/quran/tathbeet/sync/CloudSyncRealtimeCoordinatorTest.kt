package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.ScheduleSelection
import com.quran.tathbeet.domain.model.SelectionCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CloudSyncRealtimeCoordinatorTest {

    @Test
    fun active_account_updates_with_the_same_synced_profile_do_not_rebind_remote_listener() = runTest {
        val authRepository = FakeRealtimeAuthRepository()
        val profileRepository = FakeRealtimeProfileRepository()
        val cloudStore = FakeRealtimeCloudStore()
        val cloudSyncManager = FakeRealtimeCloudSyncManager(authRepository)
        profileRepository.activeAccount.tryEmit(
            syncedSelfAccount(name = "الحساب"),
        )
        val coordinator = CloudSyncRealtimeCoordinator(
            authSessionRepository = authRepository,
            profileRepository = profileRepository,
            cloudSyncStore = cloudStore,
            cloudSyncManager = cloudSyncManager,
        )
        val coordinatorScope = CoroutineScope(
            backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler),
        )

        coordinator.bind(coordinatorScope)

        advanceUntilIdle()
        profileRepository.activeAccount.tryEmit(
            syncedSelfAccount(name = "الحساب المعدل"),
        )
        advanceUntilIdle()

        assertEquals(
            "Expected one remote snapshot listener subscription.",
            1,
            cloudStore.observeProfileSnapshotCalls,
        )
        assertEquals(
            "Expected one remote snapshot import.",
            1,
            cloudSyncManager.importCalls,
        )
    }

    private fun syncedSelfAccount(name: String) =
        LearnerAccount(
            id = "self",
            name = name,
            isSelfProfile = true,
            isShared = false,
            notificationsEnabled = true,
            syncMode = ProfileSyncMode.SoloSynced,
            cloudProfileId = "personal-8DK3YQScAiTAnrlbEyfNs9MSuGa2",
        )
}

private class FakeRealtimeAuthRepository : AuthSessionRepository {
    val session = MutableSharedFlow<AuthSessionState>(replay = 1).also {
        it.tryEmit(
            AuthSessionState(
                isRuntimeConfigured = true,
                status = AuthSessionStatus.SignedIn,
                email = "mahdi.hijaz@hotmail.com",
                userId = "8DK3YQScAiTAnrlbEyfNs9MSuGa2",
                pendingEmail = null,
            ),
        )
    }

    override fun observeSession(): Flow<AuthSessionState> = session

    override suspend fun requestEmailLink(email: String): EmailLinkRequestResult =
        EmailLinkRequestResult.Success

    override suspend fun completeEmailLinkSignIn(link: String): EmailLinkCompletionResult =
        EmailLinkCompletionResult.Success

    override suspend fun signOut() = Unit
}

private class FakeRealtimeProfileRepository : com.quran.tathbeet.domain.repository.ProfileRepository {
    val activeAccount = MutableSharedFlow<LearnerAccount?>(replay = 1)

    override fun observeAccounts(): Flow<List<LearnerAccount>> = flowOf(emptyList())

    override fun observeActiveAccount(): Flow<LearnerAccount?> = activeAccount

    override suspend fun ensureDefaultAccount(name: String) = Unit

    override suspend fun createProfile(name: String): LearnerAccount = error("not used")

    override suspend fun updateAccountName(accountId: String, name: String) = Unit

    override suspend fun updateNotificationsEnabled(accountId: String, enabled: Boolean) = Unit

    override suspend fun deleteProfile(accountId: String) = Unit

    override suspend fun setActiveAccount(accountId: String) = Unit
}

private class FakeRealtimeCloudStore : CloudSyncStore {
    var observeProfileSnapshotCalls = 0
    private val snapshot = CloudProfileSnapshot(
        cloudProfileId = "personal-8DK3YQScAiTAnrlbEyfNs9MSuGa2",
        localProfileId = "self",
        displayName = "الحساب",
        syncMode = ProfileSyncMode.SoloSynced,
        ownerUserId = "8DK3YQScAiTAnrlbEyfNs9MSuGa2",
        ownerEmail = "mahdi.hijaz@hotmail.com",
        schedule = RevisionSchedule(
            id = "active-self",
            learnerId = "self",
            paceMethod = PaceMethod.CycleTarget,
            cycleTarget = CycleTarget.OneMonth,
            manualPace = PaceOption.OneJuz,
            selections = listOf(
                ScheduleSelection(
                    category = SelectionCategory.Juz,
                    itemId = 30,
                    displayOrder = 0,
                ),
            ),
        ),
        reviewDays = emptyList(),
    )

    override fun observeAccessibleProfiles(userId: String): Flow<List<CloudProfileSummary>> =
        flowOf(emptyList())

    override fun observeProfileSnapshot(cloudProfileId: String): Flow<CloudProfileSnapshot?> =
        flowOf(snapshot).also {
            observeProfileSnapshotCalls++
        }

    override suspend fun claimAccessibleProfiles(user: AuthUser): List<CloudProfileSummary> =
        emptyList()

    override suspend fun fetchProfileSnapshot(cloudProfileId: String): CloudProfileSnapshot? =
        null

    override suspend fun upsertOwnedProfile(snapshot: CloudProfileSnapshot) = Unit

    override suspend fun listMembers(cloudProfileId: String): List<CloudProfileMember> = emptyList()

    override suspend fun inviteEditor(
        cloudProfileId: String,
        ownerUserId: String,
        email: String,
    ) = Unit

    override suspend fun removeMember(
        cloudProfileId: String,
        email: String,
    ) = Unit

    override suspend fun deleteProfile(cloudProfileId: String) = Unit
}

private class FakeRealtimeCloudSyncManager(
    authRepository: AuthSessionRepository,
) : CloudSyncManager(
    authSessionRepository = authRepository,
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
    var importCalls = 0

    override suspend fun bootstrapSignedInUser() = Unit

    override suspend fun importRemoteSnapshotForActiveProfile(accountId: String) {
        importCalls++
    }
}
