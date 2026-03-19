package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object DisabledCloudSyncStore : CloudSyncStore {
    override fun observeAccessibleProfiles(userId: String): Flow<List<CloudProfileSummary>> =
        flowOf(emptyList())

    override fun observeProfileSnapshot(cloudProfileId: String): Flow<CloudProfileSnapshot?> =
        flowOf(null)

    override suspend fun claimAccessibleProfiles(user: AuthUser): List<CloudProfileSummary> =
        emptyList()

    override suspend fun fetchProfileSnapshot(cloudProfileId: String): CloudProfileSnapshot? = null

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
