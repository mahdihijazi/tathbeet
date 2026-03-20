package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ScheduleSelection
import com.quran.tathbeet.domain.model.SelectionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudSyncManagerTest {

    @Test
    fun bootstrap_imports_remote_self_profile_and_syncs_it_when_local_profile_has_no_content() = runTest {
        val authRepository = FakeAuthRepository()
        val profileRepository = FakeSyncProfileRepository()
        val cloudStore = FakeCloudStore().apply {
            profileSnapshots["personal-uid-owner"] = remoteSnapshot()
        }
        val localMirror = FakeLocalCloudProfileMirror(hasLocalContent = false)
        val coordinator = FakeProfileSyncCoordinator(profileRepository, cloudStore)
        val manager = CloudSyncManager(
            authSessionRepository = authRepository,
            profileSyncCoordinator = coordinator,
            profileRepository = profileRepository,
            cloudSyncStore = cloudStore,
            localMirror = localMirror,
        )

        manager.bootstrapSignedInUser()

        assertEquals("personal-uid-owner", localMirror.importedSelf?.cloudProfileId)
        assertEquals(listOf("self"), coordinator.syncedAccounts)
        assertEquals(ProfileSyncMode.SoloSynced, profileRepository.accounts["self"]?.syncMode)
        assertEquals("personal-uid-owner", profileRepository.accounts["self"]?.cloudProfileId)
    }

    @Test
    fun bootstrap_syncs_local_self_profile_when_local_content_exists() = runTest {
        val authRepository = FakeAuthRepository()
        val profileRepository = FakeSyncProfileRepository()
        val cloudStore = FakeCloudStore()
        val localMirror = FakeLocalCloudProfileMirror(hasLocalContent = true)
        val coordinator = FakeProfileSyncCoordinator(profileRepository, cloudStore)
        val manager = CloudSyncManager(
            authSessionRepository = authRepository,
            profileSyncCoordinator = coordinator,
            profileRepository = profileRepository,
            cloudSyncStore = cloudStore,
            localMirror = localMirror,
        )

        manager.bootstrapSignedInUser()

        assertEquals(listOf("self"), coordinator.syncedAccounts)
        assertEquals(ProfileSyncMode.SoloSynced, profileRepository.accounts["self"]?.syncMode)
        assertTrue(localMirror.importedSelf == null)
    }

    @Test
    fun self_profile_changes_are_synced_to_cloud() = runTest {
        val authRepository = FakeAuthRepository()
        val profileRepository = FakeSyncProfileRepository()
        val cloudStore = FakeCloudStore()
        val localMirror = FakeLocalCloudProfileMirror(hasLocalContent = true)
        val coordinator = FakeProfileSyncCoordinator(profileRepository, cloudStore)
        val manager = CloudSyncManager(
            authSessionRepository = authRepository,
            profileSyncCoordinator = coordinator,
            profileRepository = profileRepository,
            cloudSyncStore = cloudStore,
            localMirror = localMirror,
        )

        manager.syncAccountIfEligible("self")

        assertEquals(listOf("self"), coordinator.syncedAccounts)
        assertEquals(ProfileSyncMode.SoloSynced, profileRepository.accounts["self"]?.syncMode)
        assertEquals("personal-uid-owner", profileRepository.accounts["self"]?.cloudProfileId)
        assertEquals("self", cloudStore.profileSnapshots["personal-uid-owner"]?.localProfileId)
    }

    @Test
    fun local_only_child_profile_is_not_synced() = runTest {
        val profileRepository = FakeSyncProfileRepository(
            accounts = linkedMapOf(
                "child-1" to childAccount(
                    id = "child-1",
                    syncMode = ProfileSyncMode.LocalOnly,
                    isShared = false,
                ),
            ),
        )
        val coordinator = RecordingProfileSyncCoordinator()
        val manager = buildManager(
            profileRepository = profileRepository,
            profileSyncCoordinator = coordinator,
        )

        manager.syncAccountIfEligible("child-1")

        assertTrue(coordinator.syncedAccounts.isEmpty())
    }

    @Test
    fun solo_synced_child_profile_is_synced() = runTest {
        val profileRepository = FakeSyncProfileRepository(
            accounts = linkedMapOf(
                "child-1" to childAccount(
                    id = "child-1",
                    syncMode = ProfileSyncMode.SoloSynced,
                    cloudProfileId = "personal-child-1",
                ),
            ),
        )
        val coordinator = RecordingProfileSyncCoordinator()
        val manager = buildManager(
            profileRepository = profileRepository,
            profileSyncCoordinator = coordinator,
        )

        manager.syncAccountIfEligible("child-1")

        assertEquals(listOf("child-1"), coordinator.syncedAccounts)
    }

    @Test
    fun shared_owner_profile_is_synced() = runTest {
        val profileRepository = FakeSyncProfileRepository(
            accounts = linkedMapOf(
                "child-1" to childAccount(
                    id = "child-1",
                    isShared = true,
                    syncMode = ProfileSyncMode.SharedOwner,
                    cloudProfileId = "shared-child-1",
                ),
            ),
        )
        val coordinator = RecordingProfileSyncCoordinator()
        val manager = buildManager(
            profileRepository = profileRepository,
            profileSyncCoordinator = coordinator,
        )

        manager.syncAccountIfEligible("child-1")

        assertEquals(listOf("child-1"), coordinator.syncedAccounts)
    }

    @Test
    fun shared_editor_profile_is_synced() = runTest {
        val profileRepository = FakeSyncProfileRepository(
            accounts = linkedMapOf(
                "child-1" to childAccount(
                    id = "child-1",
                    isShared = true,
                    syncMode = ProfileSyncMode.SharedEditor,
                    cloudProfileId = "shared-child-1",
                ),
            ),
        )
        val coordinator = RecordingProfileSyncCoordinator()
        val manager = buildManager(
            profileRepository = profileRepository,
            profileSyncCoordinator = coordinator,
        )

        manager.syncAccountIfEligible("child-1")

        assertEquals(listOf("child-1"), coordinator.syncedAccounts)
    }

    @Test
    fun invite_editor_upgrades_profile_to_shared_owner_and_creates_editor_membership() = runTest {
        val authRepository = FakeAuthRepository()
        val profileRepository = FakeSyncProfileRepository(
            accounts = linkedMapOf(
                "child-1" to LearnerAccount(
                    id = "child-1",
                    name = "أحمد",
                    isSelfProfile = false,
                    isShared = false,
                    notificationsEnabled = true,
                ),
            ),
        )
        val cloudStore = FakeCloudStore()
        val localMirror = FakeLocalCloudProfileMirror(hasLocalContent = true)
        val coordinator = FakeProfileSyncCoordinator(profileRepository, cloudStore)
        val manager = CloudSyncManager(
            authSessionRepository = authRepository,
            profileSyncCoordinator = coordinator,
            profileRepository = profileRepository,
            cloudSyncStore = cloudStore,
            localMirror = localMirror,
        )

        val result = manager.inviteEditor("child-1", "teacher@example.com")

        assertEquals(SharedProfileActionResult.Success, result)
        assertEquals(ProfileSyncMode.SharedOwner, profileRepository.accounts["child-1"]?.syncMode)
        assertTrue("teacher@example.com" in cloudStore.invitedEditors["shared-child-1"].orEmpty())
    }

    @Test
    fun deleting_shared_profile_is_blocked_while_editors_remain() = runTest {
        val authRepository = FakeAuthRepository()
        val profileRepository = FakeSyncProfileRepository(
            accounts = linkedMapOf(
                "child-1" to LearnerAccount(
                    id = "child-1",
                    name = "أحمد",
                    isSelfProfile = false,
                    isShared = true,
                    notificationsEnabled = true,
                    syncMode = ProfileSyncMode.SharedOwner,
                    cloudProfileId = "shared-child-1",
                ),
            ),
        )
        val cloudStore = FakeCloudStore().apply {
            members["shared-child-1"] = mutableListOf(
                CloudProfileMember(
                    email = "owner@example.com",
                    role = CloudProfileMemberRole.Owner,
                    userId = "uid-owner",
                ),
                CloudProfileMember(
                    email = "teacher@example.com",
                    role = CloudProfileMemberRole.Editor,
                    userId = "uid-teacher",
                ),
            )
        }
        val manager = CloudSyncManager(
            authSessionRepository = authRepository,
            profileSyncCoordinator = FakeProfileSyncCoordinator(profileRepository, cloudStore),
            profileRepository = profileRepository,
            cloudSyncStore = cloudStore,
            localMirror = FakeLocalCloudProfileMirror(hasLocalContent = true),
        )

        val result = manager.deleteOwnedProfile("child-1")

        assertEquals(SharedProfileActionResult.EditorsMustBeRemovedFirst, result)
        assertTrue("shared-child-1" !in cloudStore.deletedProfiles)
    }

    @Test
    fun enable_sharing_requires_signed_in_user() = runTest {
        val manager = buildManager(
            authSessionRepository = FakeAuthRepository(sessionState = signedOutSessionState()),
        )

        val result = manager.enableSharing("child-1")

        assertEquals(SharedProfileActionResult.SignInRequired, result)
    }

    @Test
    fun invite_editor_requires_signed_in_user() = runTest {
        val manager = buildManager(
            authSessionRepository = FakeAuthRepository(sessionState = signedOutSessionState()),
        )

        val result = manager.inviteEditor("child-1", "teacher@example.com")

        assertEquals(SharedProfileActionResult.SignInRequired, result)
    }

    @Test
    fun leave_shared_profile_requires_signed_in_user() = runTest {
        val manager = buildManager(
            authSessionRepository = FakeAuthRepository(sessionState = signedOutSessionState()),
        )

        val result = manager.leaveSharedProfile("child-1")

        assertEquals(SharedProfileActionResult.SignInRequired, result)
    }

    @Test
    fun enable_sharing_returns_profile_not_found_for_missing_account() = runTest {
        val manager = buildManager()

        val result = manager.enableSharing("missing")

        assertEquals(SharedProfileActionResult.ProfileNotFound, result)
    }

    @Test
    fun invite_editor_returns_profile_not_found_for_missing_account() = runTest {
        val manager = buildManager()

        val result = manager.inviteEditor("missing", "teacher@example.com")

        assertEquals(SharedProfileActionResult.ProfileNotFound, result)
    }

    @Test
    fun leave_shared_profile_returns_profile_not_found_for_missing_account() = runTest {
        val manager = buildManager()

        val result = manager.leaveSharedProfile("missing")

        assertEquals(SharedProfileActionResult.ProfileNotFound, result)
    }

    @Test
    fun shared_profile_lifecycle_can_be_enabled_invited_cleared_and_deleted() = runTest {
        val authRepository = FakeAuthRepository()
        val profileRepository = FakeSyncProfileRepository(
            accounts = linkedMapOf(
                "child-1" to childAccount(
                    id = "child-1",
                    syncMode = ProfileSyncMode.LocalOnly,
                ),
            ),
        )
        val cloudStore = FakeCloudStore()
        val manager = CloudSyncManager(
            authSessionRepository = authRepository,
            profileSyncCoordinator = FakeProfileSyncCoordinator(profileRepository, cloudStore),
            profileRepository = profileRepository,
            cloudSyncStore = cloudStore,
            localMirror = FakeLocalCloudProfileMirror(hasLocalContent = true),
        )

        assertEquals(SharedProfileActionResult.Success, manager.enableSharing("child-1"))
        val cloudProfileId = profileRepository.accounts["child-1"]?.cloudProfileId
            ?: error("Expected cloud profile id after enabling sharing.")

        cloudStore.members[cloudProfileId] = mutableListOf(
            CloudProfileMember(
                email = "owner@example.com",
                role = CloudProfileMemberRole.Owner,
                userId = "uid-owner",
            ),
        )

        assertEquals(SharedProfileActionResult.Success, manager.inviteEditor("child-1", "teacher@example.com"))
        assertEquals(
            listOf("owner@example.com", "teacher@example.com"),
            manager.listMembers("child-1").map { member -> member.email },
        )

        assertEquals(SharedProfileActionResult.Success, manager.removeEditor("child-1", "teacher@example.com"))
        assertEquals(
            listOf("owner@example.com"),
            manager.listMembers("child-1").map { member -> member.email },
        )

        assertEquals(SharedProfileActionResult.Success, manager.deleteOwnedProfile("child-1"))
        assertTrue(cloudProfileId in cloudStore.deletedProfiles)
    }

    private fun remoteSnapshot() =
        CloudProfileSnapshot(
            cloudProfileId = "personal-uid-owner",
            localProfileId = "self",
            displayName = "صاحب الحساب",
            syncMode = ProfileSyncMode.SoloSynced,
            ownerUserId = "uid-owner",
            ownerEmail = "owner@example.com",
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

    private fun buildManager(
        authSessionRepository: AuthSessionRepository = FakeAuthRepository(),
        profileRepository: FakeSyncProfileRepository = FakeSyncProfileRepository(),
        cloudStore: FakeCloudStore = FakeCloudStore(),
        localMirror: FakeLocalCloudProfileMirror = FakeLocalCloudProfileMirror(hasLocalContent = true),
        profileSyncCoordinator: ProfileSyncCoordinator = FakeProfileSyncCoordinator(profileRepository, cloudStore),
    ) = CloudSyncManager(
        authSessionRepository = authSessionRepository,
        profileSyncCoordinator = profileSyncCoordinator,
        profileRepository = profileRepository,
        cloudSyncStore = cloudStore,
        localMirror = localMirror,
    )

    private fun childAccount(
        id: String,
        syncMode: ProfileSyncMode,
        isShared: Boolean = false,
        cloudProfileId: String? = null,
    ) = LearnerAccount(
        id = id,
        name = "أحمد",
        isSelfProfile = false,
        isShared = isShared,
        notificationsEnabled = true,
        syncMode = syncMode,
        cloudProfileId = cloudProfileId,
    )
}

private class FakeAuthRepository : AuthSessionRepository {
    private val session: MutableStateFlow<AuthSessionState>

    constructor(
        sessionState: AuthSessionState = signedInSessionState(),
    ) {
        session = MutableStateFlow(sessionState)
    }

    override fun observeSession(): Flow<AuthSessionState> = session

    override suspend fun requestEmailLink(email: String) = EmailLinkRequestResult.Success

    override suspend fun completeEmailLinkSignIn(link: String) = EmailLinkCompletionResult.Success

    override suspend fun signOut() = Unit
}

private fun signedInSessionState() = AuthSessionState(
    isRuntimeConfigured = true,
    status = AuthSessionStatus.SignedIn,
    email = "owner@example.com",
    userId = "uid-owner",
    pendingEmail = null,
)

private fun signedOutSessionState() = AuthSessionState(
    isRuntimeConfigured = true,
    status = AuthSessionStatus.SignedOut,
    email = null,
    userId = null,
    pendingEmail = null,
)

private class FakeSyncProfileRepository(
    val accounts: LinkedHashMap<String, LearnerAccount> = linkedMapOf(
        "self" to LearnerAccount(
            id = "self",
            name = "صاحب الحساب",
            isSelfProfile = true,
            isShared = false,
            notificationsEnabled = true,
        ),
    ),
) : SyncProfileRepository {
    override suspend fun getAccount(accountId: String): LearnerAccount? = accounts[accountId]

    override suspend fun getSelfProfile(): LearnerAccount? = accounts["self"]

    override suspend fun updateSyncState(
        accountId: String,
        syncMode: ProfileSyncMode,
        cloudProfileId: String?,
        isShared: Boolean,
    ) {
        val current = accounts[accountId] ?: return
        accounts[accountId] = current.copy(
            syncMode = syncMode,
            cloudProfileId = cloudProfileId,
            isShared = isShared,
        )
    }
}

private class FakeProfileSyncCoordinator(
    private val profileRepository: FakeSyncProfileRepository,
    private val cloudStore: FakeCloudStore,
) : ProfileSyncCoordinator(
    profileRepository = profileRepository,
    scheduleRepository = object : SyncScheduleRepository {
        override suspend fun getActiveSchedule(learnerId: String): RevisionSchedule? = null
    },
    reviewRepository = object : SyncReviewRepository {
        override suspend fun getReviewTimeline(learnerId: String) = emptyList<com.quran.tathbeet.domain.model.ReviewDay>()
    },
    cloudSyncStore = cloudStore,
) {
    val syncedAccounts = mutableListOf<String>()

    override suspend fun syncOwnedProfile(
        accountId: String,
        user: AuthUser,
        shared: Boolean?,
    ) {
        syncedAccounts += accountId
        super.syncOwnedProfile(
            accountId = accountId,
            user = user,
            shared = shared,
        )
    }
}

private class RecordingProfileSyncCoordinator : ProfileSyncCoordinator(
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
        override suspend fun getReviewTimeline(learnerId: String) = emptyList<com.quran.tathbeet.domain.model.ReviewDay>()
    },
    cloudSyncStore = DisabledCloudSyncStore,
) {
    val syncedAccounts = mutableListOf<String>()

    override suspend fun syncOwnedProfile(
        accountId: String,
        user: AuthUser,
        shared: Boolean?,
    ) {
        syncedAccounts += accountId
    }
}

private class FakeCloudStore : CloudSyncStore {
    val profileSnapshots = linkedMapOf<String, CloudProfileSnapshot>()
    val invitedEditors = linkedMapOf<String, MutableList<String>>()
    val members = linkedMapOf<String, MutableList<CloudProfileMember>>()
    val deletedProfiles = mutableListOf<String>()

    override fun observeAccessibleProfiles(userId: String): Flow<List<CloudProfileSummary>> =
        flowOf(
            profileSnapshots.values
                .filter { snapshot -> snapshot.syncMode == ProfileSyncMode.SharedOwner || snapshot.syncMode == ProfileSyncMode.SharedEditor }
                .map { snapshot ->
                    CloudProfileSummary(
                        cloudProfileId = snapshot.cloudProfileId,
                        displayName = snapshot.displayName,
                        syncMode = snapshot.syncMode,
                        ownerEmail = snapshot.ownerEmail,
                        memberRole = if (snapshot.syncMode == ProfileSyncMode.SharedEditor) {
                            CloudProfileMemberRole.Editor
                        } else {
                            CloudProfileMemberRole.Owner
                        },
                    )
                },
        )

    override fun observeProfileSnapshot(cloudProfileId: String): Flow<CloudProfileSnapshot?> =
        flowOf(profileSnapshots[cloudProfileId])

    override suspend fun claimAccessibleProfiles(user: AuthUser): List<CloudProfileSummary> =
        observeAccessibleProfiles(user.uid).first()

    override suspend fun fetchProfileSnapshot(cloudProfileId: String): CloudProfileSnapshot? =
        profileSnapshots[cloudProfileId]

    override suspend fun upsertOwnedProfile(snapshot: CloudProfileSnapshot) {
        profileSnapshots[snapshot.cloudProfileId] = snapshot
    }

    override suspend fun listMembers(cloudProfileId: String): List<CloudProfileMember> =
        members[cloudProfileId].orEmpty()

    override suspend fun inviteEditor(
        cloudProfileId: String,
        ownerUserId: String,
        email: String,
    ) {
        invitedEditors.getOrPut(cloudProfileId) { mutableListOf() }.add(email)
        members.getOrPut(cloudProfileId) { mutableListOf() }.add(
            CloudProfileMember(
                email = email,
                role = CloudProfileMemberRole.Editor,
            ),
        )
    }

    override suspend fun removeMember(
        cloudProfileId: String,
        email: String,
    ) {
        members[cloudProfileId] = members[cloudProfileId]
            .orEmpty()
            .filterNot { member -> member.email == email }
            .toMutableList()
    }

    override suspend fun deleteProfile(cloudProfileId: String) {
        deletedProfiles += cloudProfileId
    }
}

private class FakeLocalCloudProfileMirror(
    private val hasLocalContent: Boolean,
) : LocalCloudProfileMirror {
    var importedSelf: CloudProfileSnapshot? = null
    val importedShared = mutableListOf<Pair<CloudProfileSnapshot, ProfileSyncMode>>()
    var removedSharedProfileId: String? = null

    override suspend fun hasMeaningfulLocalData(accountId: String): Boolean = hasLocalContent

    override suspend fun findAccountByCloudProfileId(cloudProfileId: String): LearnerAccount? = null

    override suspend fun importSelfProfile(snapshot: CloudProfileSnapshot) {
        importedSelf = snapshot
    }

    override suspend fun importSharedProfile(
        snapshot: CloudProfileSnapshot,
        syncMode: ProfileSyncMode,
    ) {
        importedShared += snapshot to syncMode
    }

    override suspend fun removeStaleSharedProfiles(allowedCloudProfileIds: Set<String>) = Unit

    override suspend fun removeSharedProfile(cloudProfileId: String) {
        removedSharedProfileId = cloudProfileId
    }
}
