package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.Flow

interface CloudSyncStore {
    fun observeAccessibleProfiles(userId: String): Flow<List<CloudProfileSummary>>

    fun observeProfileSnapshot(cloudProfileId: String): Flow<CloudProfileSnapshot?>

    suspend fun claimAccessibleProfiles(user: AuthUser): List<CloudProfileSummary>

    suspend fun fetchProfileSnapshot(cloudProfileId: String): CloudProfileSnapshot?

    suspend fun upsertOwnedProfile(snapshot: CloudProfileSnapshot)

    suspend fun listMembers(cloudProfileId: String): List<CloudProfileMember>

    suspend fun inviteEditor(
        cloudProfileId: String,
        ownerUserId: String,
        email: String,
    )

    suspend fun removeMember(
        cloudProfileId: String,
        email: String,
    )

    suspend fun deleteProfile(cloudProfileId: String)
}
