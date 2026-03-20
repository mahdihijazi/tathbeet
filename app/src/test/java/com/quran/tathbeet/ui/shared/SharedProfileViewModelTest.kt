package com.quran.tathbeet.ui.features.shared

import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.sync.AuthSessionRepository
import com.quran.tathbeet.sync.AuthSessionState
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.sync.CloudProfileMember
import com.quran.tathbeet.sync.CloudProfileMemberRole
import com.quran.tathbeet.sync.CloudSyncManager
import com.quran.tathbeet.sync.EmailLinkCompletionResult
import com.quran.tathbeet.sync.EmailLinkRequestResult
import com.quran.tathbeet.sync.SharedProfileActionResult
import com.quran.tathbeet.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharedProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun local_sub_profile_shows_enable_sharing_when_user_is_signed_in() = runTest {
        val profile = profile(
            id = "child-1",
            name = "أحمد",
            syncMode = ProfileSyncMode.SoloSynced,
            cloudProfileId = "personal-child-1",
        )
        val profileRepository = FakeSharedProfileRepository(
            accounts = listOf(profile),
            active = profile,
        )
        val manager = FakeCloudSyncManager()
        val viewModel = SharedProfileViewModel(
            selectedProfileId = profile.id,
            profileRepository = profileRepository,
            authSessionRepository = FakeSharedAuthRepository(),
            cloudSyncManager = manager,
        )

        advanceUntilIdle()

        assertEquals("أحمد", viewModel.uiState.value.profileName)
        assertTrue(viewModel.uiState.value.canEnableSharing)
        assertFalse(viewModel.uiState.value.canInviteEditors)
    }

    @Test
    fun selected_profile_id_is_used_even_when_active_account_differs() = runTest {
        val self = profile(
            id = "self",
            name = "حسابي",
            isSelfProfile = true,
            isActive = true,
        )
        val child = profile(
            id = "child-1",
            name = "أحمد",
            syncMode = ProfileSyncMode.SoloSynced,
            cloudProfileId = "personal-child-1",
        )
        val profileRepository = FakeSharedProfileRepository(
            accounts = listOf(self, child),
            active = self,
        )
        val manager = FakeCloudSyncManager()
        val viewModel = SharedProfileViewModel(
            selectedProfileId = child.id,
            profileRepository = profileRepository,
            authSessionRepository = FakeSharedAuthRepository(),
            cloudSyncManager = manager,
        )

        advanceUntilIdle()

        assertEquals("child-1", viewModel.uiState.value.profileId)
        assertEquals("أحمد", viewModel.uiState.value.profileName)
        assertTrue(viewModel.uiState.value.canEnableSharing)
    }

    @Test
    fun shared_owner_profile_loads_members_and_allows_invites() = runTest {
        val owner = profile(
            id = "child-1",
            name = "أحمد",
            isShared = true,
            syncMode = ProfileSyncMode.SharedOwner,
            cloudProfileId = "shared-child-1",
        )
        val profileRepository = FakeSharedProfileRepository(
            accounts = listOf(owner),
            active = owner,
        )
        val manager = FakeCloudSyncManager().apply {
            members = listOf(
                CloudProfileMember("owner@example.com", CloudProfileMemberRole.Owner, "uid-owner"),
                CloudProfileMember("teacher@example.com", CloudProfileMemberRole.Editor, "uid-teacher"),
            )
        }
        val viewModel = SharedProfileViewModel(
            selectedProfileId = owner.id,
            profileRepository = profileRepository,
            authSessionRepository = FakeSharedAuthRepository(),
            cloudSyncManager = manager,
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canInviteEditors)
        assertEquals(2, viewModel.uiState.value.members.size)
        assertTrue(viewModel.uiState.value.members.any { member -> member.email == "teacher@example.com" })
    }

    @Test
    fun shared_owner_profile_renders_before_members_load_completes() = runTest {
        val membersGate = CompletableDeferred<Unit>()
        val owner = profile(
            id = "child-1",
            name = "أحمد",
            isShared = true,
            syncMode = ProfileSyncMode.SharedOwner,
            cloudProfileId = "shared-child-1",
        )
        val profileRepository = FakeSharedProfileRepository(
            accounts = listOf(owner),
            active = owner,
        )
        val manager = FakeCloudSyncManager().apply {
            this.membersGate = membersGate
            members = listOf(
                CloudProfileMember("owner@example.com", CloudProfileMemberRole.Owner, "uid-owner"),
                CloudProfileMember("teacher@example.com", CloudProfileMemberRole.Editor, "uid-teacher"),
            )
        }
        val viewModel = SharedProfileViewModel(
            selectedProfileId = owner.id,
            profileRepository = profileRepository,
            authSessionRepository = FakeSharedAuthRepository(),
            cloudSyncManager = manager,
        )

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("أحمد", viewModel.uiState.value.profileName)
        assertTrue(viewModel.uiState.value.canInviteEditors)
        assertTrue(viewModel.uiState.value.members.isEmpty())

        membersGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.members.size)
        assertTrue(viewModel.uiState.value.members.any { member -> member.email == "teacher@example.com" })
    }

    @Test
    fun shared_owner_profile_member_load_failures_do_not_crash_screen() = runTest {
        val owner = profile(
            id = "child-1",
            name = "أحمد",
            isShared = true,
            syncMode = ProfileSyncMode.SharedOwner,
            cloudProfileId = "shared-child-1",
        )
        val profileRepository = FakeSharedProfileRepository(
            accounts = listOf(owner),
            active = owner,
        )
        val manager = FakeCloudSyncManager().apply {
            membersError = RuntimeException("PERMISSION_DENIED")
        }
        val viewModel = SharedProfileViewModel(
            selectedProfileId = owner.id,
            profileRepository = profileRepository,
            authSessionRepository = FakeSharedAuthRepository(),
            cloudSyncManager = manager,
        )

        advanceUntilIdle()

        assertEquals("أحمد", viewModel.uiState.value.profileName)
        assertTrue(viewModel.uiState.value.members.isEmpty())
        assertTrue(viewModel.uiState.value.canInviteEditors)
    }

    @Test
    fun enabling_sharing_calls_manager_and_updates_feedback() = runTest {
        val child = profile(
            id = "child-1",
            name = "أحمد",
            syncMode = ProfileSyncMode.SoloSynced,
            cloudProfileId = "personal-child-1",
        )
        val profileRepository = FakeSharedProfileRepository(
            accounts = listOf(child),
            active = child,
        )
        val manager = FakeCloudSyncManager().apply {
            enableSharingResult = SharedProfileActionResult.Success
        }
        val viewModel = SharedProfileViewModel(
            selectedProfileId = child.id,
            profileRepository = profileRepository,
            authSessionRepository = FakeSharedAuthRepository(),
            cloudSyncManager = manager,
        )

        advanceUntilIdle()
        viewModel.enableSharing()
        advanceUntilIdle()

        assertEquals(listOf("child-1"), manager.enabledSharingFor)
        assertEquals(SharedProfileBanner.SharedEnabled, viewModel.uiState.value.banner)
    }

    @Test
    fun inviting_editor_refreshes_members_and_updates_feedback() = runTest {
        val owner = profile(
            id = "child-1",
            name = "أحمد",
            isShared = true,
            syncMode = ProfileSyncMode.SharedOwner,
            cloudProfileId = "shared-child-1",
        )
        val profileRepository = FakeSharedProfileRepository(
            accounts = listOf(owner),
            active = owner,
        )
        val manager = FakeCloudSyncManager().apply {
            members = listOf(
                CloudProfileMember("owner@example.com", CloudProfileMemberRole.Owner, "uid-owner"),
            )
            inviteEditorResult = SharedProfileActionResult.Success
        }
        val viewModel = SharedProfileViewModel(
            selectedProfileId = owner.id,
            profileRepository = profileRepository,
            authSessionRepository = FakeSharedAuthRepository(),
            cloudSyncManager = manager,
        )

        advanceUntilIdle()
        manager.members = listOf(
            CloudProfileMember("owner@example.com", CloudProfileMemberRole.Owner, "uid-owner"),
            CloudProfileMember("teacher@example.com", CloudProfileMemberRole.Editor, "uid-teacher"),
        )

        viewModel.inviteEditor("teacher@example.com")
        advanceUntilIdle()

        assertEquals(listOf("teacher@example.com"), manager.invitedEditors)
        assertEquals(SharedProfileBanner.InviteSent, viewModel.uiState.value.banner)
        assertEquals(2, viewModel.uiState.value.members.size)
        assertTrue(viewModel.uiState.value.members.any { member -> member.email == "teacher@example.com" })
    }
}

private class FakeSharedProfileRepository(
    accounts: List<LearnerAccount>,
    active: LearnerAccount,
) : ProfileRepository {
    private val accounts = MutableStateFlow(accounts)
    private val activeAccount = MutableStateFlow(active)

    override fun observeAccounts(): Flow<List<LearnerAccount>> = accounts

    override fun observeActiveAccount(): Flow<LearnerAccount?> = activeAccount

    override suspend fun ensureDefaultAccount(name: String) = Unit

    override suspend fun createProfile(name: String): LearnerAccount = activeAccount.value

    override suspend fun updateAccountName(accountId: String, name: String) = Unit

    override suspend fun updateNotificationsEnabled(accountId: String, enabled: Boolean) = Unit

    override suspend fun deleteProfile(accountId: String) = Unit

    override suspend fun setActiveAccount(accountId: String) = Unit
}

private class FakeSharedAuthRepository : AuthSessionRepository {
    private val session = MutableStateFlow(
        AuthSessionState(
            isRuntimeConfigured = true,
            status = AuthSessionStatus.SignedIn,
            email = "owner@example.com",
            userId = "uid-owner",
            pendingEmail = null,
        ),
    )

    override fun observeSession(): Flow<AuthSessionState> = session

    override suspend fun requestEmailLink(email: String) = EmailLinkRequestResult.Success

    override suspend fun completeEmailLinkSignIn(link: String) = EmailLinkCompletionResult.Success

    override suspend fun signOut() = Unit
}

private class FakeCloudSyncManager : CloudSyncManager(
    authSessionRepository = FakeSharedAuthRepository(),
    profileSyncCoordinator = com.quran.tathbeet.sync.ProfileSyncCoordinator(
        profileRepository = object : com.quran.tathbeet.sync.SyncProfileRepository {
            override suspend fun getAccount(accountId: String) = null
            override suspend fun getSelfProfile() = null
            override suspend fun updateSyncState(
                accountId: String,
                syncMode: ProfileSyncMode,
                cloudProfileId: String?,
                isShared: Boolean,
            ) = Unit
        },
        scheduleRepository = object : com.quran.tathbeet.sync.SyncScheduleRepository {
            override suspend fun getActiveSchedule(learnerId: String) = null
        },
        reviewRepository = object : com.quran.tathbeet.sync.SyncReviewRepository {
            override suspend fun getReviewTimeline(learnerId: String) = emptyList<com.quran.tathbeet.domain.model.ReviewDay>()
        },
        cloudSyncStore = com.quran.tathbeet.sync.DisabledCloudSyncStore,
    ),
    profileRepository = object : com.quran.tathbeet.sync.SyncProfileRepository {
        override suspend fun getAccount(accountId: String) = null
        override suspend fun getSelfProfile() = null
        override suspend fun updateSyncState(
            accountId: String,
            syncMode: ProfileSyncMode,
            cloudProfileId: String?,
            isShared: Boolean,
        ) = Unit
    },
    cloudSyncStore = com.quran.tathbeet.sync.DisabledCloudSyncStore,
    localMirror = object : com.quran.tathbeet.sync.LocalCloudProfileMirror {
        override suspend fun hasMeaningfulLocalData(accountId: String) = false
        override suspend fun findAccountByCloudProfileId(cloudProfileId: String) = null
        override suspend fun importSelfProfile(snapshot: com.quran.tathbeet.sync.CloudProfileSnapshot) = Unit
        override suspend fun importSharedProfile(
            snapshot: com.quran.tathbeet.sync.CloudProfileSnapshot,
            syncMode: ProfileSyncMode,
        ) = Unit
        override suspend fun removeStaleSharedProfiles(allowedCloudProfileIds: Set<String>) = Unit
        override suspend fun removeSharedProfile(cloudProfileId: String) = Unit
    },
) {
    var membersGate: CompletableDeferred<Unit>? = null
    var members: List<CloudProfileMember> = emptyList()
    var membersError: Throwable? = null
    var enableSharingResult: SharedProfileActionResult = SharedProfileActionResult.Success
    var inviteEditorResult: SharedProfileActionResult = SharedProfileActionResult.Success
    val enabledSharingFor = mutableListOf<String>()
    val invitedEditors = mutableListOf<String>()

    override suspend fun listMembers(accountId: String): List<CloudProfileMember> {
        membersGate?.await()
        membersError?.let { throw it }
        return members
    }

    override suspend fun enableSharing(accountId: String): SharedProfileActionResult {
        enabledSharingFor += accountId
        return enableSharingResult
    }

    override suspend fun inviteEditor(
        accountId: String,
        email: String,
    ): SharedProfileActionResult {
        invitedEditors += email
        return inviteEditorResult
    }
}

private fun profile(
    id: String,
    name: String,
    isSelfProfile: Boolean = false,
    isShared: Boolean = false,
    isActive: Boolean = false,
    syncMode: ProfileSyncMode = ProfileSyncMode.LocalOnly,
    cloudProfileId: String? = null,
) = LearnerAccount(
    id = id,
    name = name,
    isSelfProfile = isSelfProfile,
    isShared = isShared,
    notificationsEnabled = true,
    syncMode = syncMode,
    cloudProfileId = cloudProfileId,
)
