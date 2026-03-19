package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewDay
import kotlinx.coroutines.flow.first

open class CloudSyncManager(
    private val authSessionRepository: AuthSessionRepository,
    private val profileSyncCoordinator: ProfileSyncCoordinator,
    private val profileRepository: SyncProfileRepository,
    private val cloudSyncStore: CloudSyncStore,
    private val localMirror: LocalCloudProfileMirror,
) {
    open suspend fun bootstrapSignedInUser() {
        val user = signedInUser() ?: return
        val selfProfile = profileRepository.getSelfProfile() ?: return
        importSelfProfileIfNeeded(selfProfile, user)
        syncOwnedProfile(
            accountId = selfProfile.id,
            user = user,
        )
        refreshAccessibleProfiles(user)
    }

    open suspend fun syncAccountIfEligible(accountId: String) {
        val user = signedInUser() ?: return
        val account = profileRepository.getAccount(accountId) ?: return
        if (!account.isCloudSyncEligible()) {
            return
        }
        syncOwnedProfile(
            accountId = accountId,
            user = user,
            shared = account.isShared,
        )
    }

    open suspend fun enableSharing(accountId: String): SharedProfileActionResult {
        return withSignedInAccount(accountId) { user, account ->
            if (account.isSelfProfile) {
                return@withSignedInAccount SharedProfileActionResult.SelfProfileCannotBeShared
            }
            syncOwnedProfile(
                accountId = accountId,
                user = user,
                shared = true,
            )
            SharedProfileActionResult.Success
        }
    }

    open suspend fun inviteEditor(
        accountId: String,
        email: String,
    ): SharedProfileActionResult {
        return withSignedInAccount(accountId) { user, account ->
            if (account.isSelfProfile) {
                return@withSignedInAccount SharedProfileActionResult.SelfProfileCannotBeShared
            }

            val shareResult = if (account.syncMode == ProfileSyncMode.SharedOwner) {
                SharedProfileActionResult.Success
            } else {
                enableSharing(accountId)
            }
            if (shareResult != SharedProfileActionResult.Success) {
                return@withSignedInAccount shareResult
            }

            val cloudProfileId = cloudProfileIdFor(accountId)
                ?: return@withSignedInAccount SharedProfileActionResult.ProfileNotFound
            cloudSyncStore.inviteEditor(
                cloudProfileId = cloudProfileId,
                ownerUserId = user.uid,
                email = email,
            )
            SharedProfileActionResult.Success
        }
    }

    open suspend fun removeEditor(
        accountId: String,
        email: String,
    ): SharedProfileActionResult {
        val cloudProfileId = cloudProfileIdFor(accountId)
            ?: return SharedProfileActionResult.ProfileNotFound
        cloudSyncStore.removeMember(
            cloudProfileId = cloudProfileId,
            email = email,
        )
        return SharedProfileActionResult.Success
    }

    open suspend fun listMembers(accountId: String): List<CloudProfileMember> {
        val cloudProfileId = cloudProfileIdFor(accountId) ?: return emptyList()
        return cloudSyncStore.listMembers(cloudProfileId)
    }

    open suspend fun deleteOwnedProfile(accountId: String): SharedProfileActionResult {
        val account = profileRepository.getAccount(accountId) ?: return SharedProfileActionResult.ProfileNotFound
        val cloudProfileId = account.cloudProfileId ?: return SharedProfileActionResult.Success
        val editors = cloudSyncStore.listMembers(cloudProfileId)
            .filter { member -> member.role == CloudProfileMemberRole.Editor }
        if (editors.isNotEmpty()) {
            return SharedProfileActionResult.EditorsMustBeRemovedFirst
        }
        cloudSyncStore.deleteProfile(cloudProfileId)
        return SharedProfileActionResult.Success
    }

    open suspend fun leaveSharedProfile(accountId: String): SharedProfileActionResult {
        return withSignedInAccount(accountId) { user, account ->
            if (account.syncMode != ProfileSyncMode.SharedEditor) {
                return@withSignedInAccount SharedProfileActionResult.OwnerCannotLeaveWhileEditorsRemain
            }
            val cloudProfileId = account.cloudProfileId
                ?: return@withSignedInAccount SharedProfileActionResult.ProfileNotFound
            val email = user.email ?: return@withSignedInAccount SharedProfileActionResult.SignInRequired
            cloudSyncStore.removeMember(
                cloudProfileId = cloudProfileId,
                email = email,
            )
            localMirror.removeSharedProfile(cloudProfileId)
            SharedProfileActionResult.Success
        }
    }

    open suspend fun importAccessibleSharedProfiles(user: AuthUser?) {
        val signedInUser = user ?: return
        val accessibleProfiles = cloudSyncStore.observeAccessibleProfiles(signedInUser.uid).first()
        val sharedProfiles = accessibleProfiles.filter(CloudProfileSummary::isSharedProfile)
        sharedProfiles.forEach { summary ->
            cloudSyncStore.fetchProfileSnapshot(summary.cloudProfileId)?.let { snapshot ->
                localMirror.importSharedProfile(
                    snapshot = snapshot,
                    syncMode = summary.syncMode,
                )
            }
        }
        localMirror.removeStaleSharedProfiles(
            allowedCloudProfileIds = sharedProfiles.mapTo(linkedSetOf()) { it.cloudProfileId },
        )
    }

    open suspend fun importRemoteSnapshotForActiveProfile(accountId: String) {
        val account = profileRepository.getAccount(accountId) ?: return
        val cloudProfileId = account.cloudProfileId ?: return
        val snapshot = cloudSyncStore.fetchProfileSnapshot(cloudProfileId) ?: return
        if (account.isSelfProfile) {
            localMirror.importSelfProfile(snapshot)
            return
        }
        localMirror.importSharedProfile(snapshot, account.syncMode)
    }

    open suspend fun activeProfileTimeline(accountId: String): List<ReviewDay> {
        val cloudProfileId = cloudProfileIdFor(accountId) ?: return emptyList()
        return cloudSyncStore.fetchProfileSnapshot(cloudProfileId)?.reviewDays.orEmpty()
    }

    private suspend fun importSelfProfileIfNeeded(
        selfProfile: com.quran.tathbeet.domain.model.LearnerAccount,
        user: AuthUser,
    ) {
        if (localMirror.hasMeaningfulLocalData(selfProfile.id)) {
            return
        }
        val cloudProfileId = selfProfile.cloudProfileId ?: "personal-${user.uid}"
        val remoteSnapshot = cloudSyncStore.fetchProfileSnapshot(cloudProfileId) ?: return
        localMirror.importSelfProfile(remoteSnapshot)
    }

    private suspend fun refreshAccessibleProfiles(user: AuthUser) {
        cloudSyncStore.claimAccessibleProfiles(user)
        importAccessibleSharedProfiles(user)
    }

    private suspend fun syncOwnedProfile(
        accountId: String,
        user: AuthUser,
        shared: Boolean? = null,
    ) {
        profileSyncCoordinator.syncOwnedProfile(
            accountId = accountId,
            user = user,
            shared = shared,
        )
    }

    private suspend fun withSignedInAccount(
        accountId: String,
        block: suspend (AuthUser, com.quran.tathbeet.domain.model.LearnerAccount) -> SharedProfileActionResult,
    ): SharedProfileActionResult {
        val user = signedInUser() ?: return SharedProfileActionResult.SignInRequired
        val account = profileRepository.getAccount(accountId) ?: return SharedProfileActionResult.ProfileNotFound
        return block(user, account)
    }

    private suspend fun cloudProfileIdFor(accountId: String): String? =
        profileRepository.getAccount(accountId)?.cloudProfileId

    private suspend fun signedInUser(): AuthUser? {
        val session = authSessionRepository.observeSession().first()
        if (session.status != AuthSessionStatus.SignedIn || session.userId.isNullOrBlank()) {
            return null
        }
        return AuthUser(
            uid = session.userId,
            email = session.email,
        )
    }
}

sealed interface SharedProfileActionResult {
    data object Success : SharedProfileActionResult

    data object SignInRequired : SharedProfileActionResult

    data object ProfileNotFound : SharedProfileActionResult

    data object SelfProfileCannotBeShared : SharedProfileActionResult

    data object EditorsMustBeRemovedFirst : SharedProfileActionResult

    data object OwnerCannotLeaveWhileEditorsRemain : SharedProfileActionResult
}

private fun com.quran.tathbeet.domain.model.LearnerAccount.isCloudSyncEligible(): Boolean =
    isSelfProfile || isShared || syncMode != ProfileSyncMode.LocalOnly

private fun CloudProfileSummary.isSharedProfile(): Boolean =
    syncMode == ProfileSyncMode.SharedOwner || syncMode == ProfileSyncMode.SharedEditor
