package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.ProfileSyncMode

open class ProfileSyncCoordinator(
    private val profileRepository: SyncProfileRepository,
    private val scheduleRepository: SyncScheduleRepository,
    private val reviewRepository: SyncReviewRepository,
    private val cloudSyncStore: CloudSyncStore,
) {
    open suspend fun ensureSelfProfileSynced(user: AuthUser) {
        syncOwnedProfile(
            accountId = profileRepository.getSelfProfile()?.id ?: return,
            user = user,
        )
    }

    open suspend fun syncOwnedProfile(
        accountId: String,
        user: AuthUser,
        shared: Boolean? = null,
    ) {
        val profile = profileRepository.getAccount(accountId) ?: return
        val isShared = shared ?: profile.isShared
        val cloudProfileId = resolvedCloudProfileId(profile, user)
        val syncMode = resolvedSyncMode(isShared, profile)
        val desiredSnapshot = buildDesiredSnapshot(
            profile = profile,
            user = user,
            cloudProfileId = cloudProfileId,
            syncMode = syncMode,
        )
        val currentSnapshot = cloudSyncStore.fetchProfileSnapshot(cloudProfileId)
            ?.copy(localProfileId = profile.id)
        if (currentSnapshot == desiredSnapshot) {
            profileRepository.updateSyncState(
                accountId = profile.id,
                syncMode = syncMode,
                cloudProfileId = cloudProfileId,
                isShared = isShared,
            )
            return
        }
        cloudSyncStore.upsertOwnedProfile(
            snapshot = desiredSnapshot,
        )
        profileRepository.updateSyncState(
            accountId = profile.id,
            syncMode = syncMode,
            cloudProfileId = cloudProfileId,
            isShared = isShared,
        )
    }

    private suspend fun resolvedCloudProfileId(
        profile: com.quran.tathbeet.domain.model.LearnerAccount,
        user: AuthUser,
    ): String = profile.cloudProfileId ?: if (profile.isSelfProfile) {
        "personal-${user.uid}"
    } else {
        "shared-${profile.id}"
    }

    private fun resolvedSyncMode(
        isShared: Boolean,
        profile: com.quran.tathbeet.domain.model.LearnerAccount,
    ): ProfileSyncMode = when {
        isShared -> ProfileSyncMode.SharedOwner
        profile.isSelfProfile -> ProfileSyncMode.SoloSynced
        else -> ProfileSyncMode.SoloSynced
    }

    private suspend fun buildDesiredSnapshot(
        profile: com.quran.tathbeet.domain.model.LearnerAccount,
        user: AuthUser,
        cloudProfileId: String,
        syncMode: ProfileSyncMode,
    ): CloudProfileSnapshot = CloudProfileSnapshot(
        cloudProfileId = cloudProfileId,
        localProfileId = profile.id,
        displayName = profile.name,
        syncMode = syncMode,
        ownerUserId = user.uid,
        ownerEmail = user.email,
        schedule = scheduleRepository.getActiveSchedule(profile.id),
        reviewDays = reviewRepository.getReviewTimeline(profile.id),
    )
}
