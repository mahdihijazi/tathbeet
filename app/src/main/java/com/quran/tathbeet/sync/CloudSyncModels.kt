package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ProfileSyncMode

data class CloudProfileSummary(
    val cloudProfileId: String,
    val displayName: String,
    val syncMode: ProfileSyncMode,
    val ownerEmail: String?,
    val memberRole: CloudProfileMemberRole,
)

data class CloudProfileSnapshot(
    val cloudProfileId: String,
    val localProfileId: String,
    val displayName: String,
    val syncMode: ProfileSyncMode,
    val ownerUserId: String,
    val ownerEmail: String?,
    val schedule: RevisionSchedule?,
    val reviewDays: List<ReviewDay>,
)

data class CloudProfileMember(
    val email: String,
    val role: CloudProfileMemberRole,
    val userId: String? = null,
)

enum class CloudProfileMemberRole {
    Owner,
    Editor,
}
