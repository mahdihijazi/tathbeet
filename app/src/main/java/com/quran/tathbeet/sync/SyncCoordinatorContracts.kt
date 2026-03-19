package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule

interface SyncProfileRepository {
    suspend fun getAccount(accountId: String): LearnerAccount?

    suspend fun getSelfProfile(): LearnerAccount?

    suspend fun updateSyncState(
        accountId: String,
        syncMode: ProfileSyncMode,
        cloudProfileId: String?,
        isShared: Boolean,
    )
}

interface SyncScheduleRepository {
    suspend fun getActiveSchedule(learnerId: String): RevisionSchedule?
}

interface SyncReviewRepository {
    suspend fun getReviewTimeline(learnerId: String): List<ReviewDay>
}
