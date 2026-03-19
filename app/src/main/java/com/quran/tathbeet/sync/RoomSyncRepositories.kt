package com.quran.tathbeet.sync

import androidx.room.withTransaction
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first

class RoomSyncProfileRepository(
    private val database: TathbeetDatabase,
) : SyncProfileRepository {
    override suspend fun getAccount(accountId: String): LearnerAccount? =
        database.learnerAccountDao().getAccount(accountId)?.toDomainModel()

    override suspend fun getSelfProfile(): LearnerAccount? =
        database.learnerAccountDao().getAccount("self")?.toDomainModel()

    override suspend fun updateSyncState(
        accountId: String,
        syncMode: ProfileSyncMode,
        cloudProfileId: String?,
        isShared: Boolean,
    ) {
        database.withTransaction {
            val account = database.learnerAccountDao().getAccount(accountId) ?: return@withTransaction
            val updatedAccount = account.copy(
                isShared = isShared,
                syncMode = syncMode.name,
                cloudProfileId = cloudProfileId,
            )
            if (account == updatedAccount) {
                return@withTransaction
            }
            database.learnerAccountDao().upsert(updatedAccount)
        }
    }

    private fun com.quran.tathbeet.data.local.entity.LearnerAccountEntity.toDomainModel(): LearnerAccount =
        LearnerAccount(
            id = id,
            name = name,
            isSelfProfile = isSelfProfile,
            isShared = isShared,
            notificationsEnabled = notificationsEnabled,
            syncMode = ProfileSyncMode.valueOf(syncMode),
            cloudProfileId = cloudProfileId,
        )
}

class RepositoryBackedSyncScheduleRepository(
    private val scheduleRepository: ScheduleRepository,
) : SyncScheduleRepository {
    override suspend fun getActiveSchedule(learnerId: String): RevisionSchedule? =
        scheduleRepository.observeActiveSchedule(learnerId).first()
}

class RepositoryBackedSyncReviewRepository(
    private val reviewRepository: ReviewRepository,
) : SyncReviewRepository {
    override suspend fun getReviewTimeline(learnerId: String): List<ReviewDay> =
        reviewRepository.observeReviewTimeline(learnerId).first()
}
