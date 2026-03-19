package com.quran.tathbeet.sync

import androidx.room.withTransaction
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.local.entity.LearnerAccountEntity
import com.quran.tathbeet.data.local.entity.ReviewAssignmentEntity
import com.quran.tathbeet.data.local.entity.ReviewDayEntity
import com.quran.tathbeet.data.local.entity.RevisionScheduleEntity
import com.quran.tathbeet.data.local.entity.ScheduleSelectionEntity
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ScheduleSelection
import kotlinx.coroutines.flow.first

interface LocalCloudProfileMirror {
    suspend fun hasMeaningfulLocalData(accountId: String): Boolean

    suspend fun findAccountByCloudProfileId(cloudProfileId: String): LearnerAccount?

    suspend fun importSelfProfile(snapshot: CloudProfileSnapshot)

    suspend fun importSharedProfile(
        snapshot: CloudProfileSnapshot,
        syncMode: ProfileSyncMode,
    )

    suspend fun removeStaleSharedProfiles(allowedCloudProfileIds: Set<String>)

    suspend fun removeSharedProfile(cloudProfileId: String)
}

class RoomCloudProfileMirror(
    private val database: TathbeetDatabase,
) : LocalCloudProfileMirror {
    override suspend fun hasMeaningfulLocalData(accountId: String): Boolean {
        val schedule = database.revisionScheduleDao().observeActiveSchedule(accountId)
        val hasSchedule = schedule.first() != null
        if (hasSchedule) {
            return true
        }
        return database.reviewDayDao().getFirstReviewDay(accountId) != null
    }

    override suspend fun findAccountByCloudProfileId(cloudProfileId: String): LearnerAccount? =
        database.learnerAccountDao()
            .getByCloudProfileId(cloudProfileId)
            ?.toDomainModel()

    override suspend fun importSelfProfile(snapshot: CloudProfileSnapshot) {
        importSnapshot(
            snapshot = snapshot,
            targetAccountId = SELF_PROFILE_ID,
            isSelfProfile = true,
            notificationsEnabled = true,
            syncMode = ProfileSyncMode.SoloSynced,
        )
    }

    override suspend fun importSharedProfile(
        snapshot: CloudProfileSnapshot,
        syncMode: ProfileSyncMode,
    ) {
        val existing = database.learnerAccountDao().getByCloudProfileId(snapshot.cloudProfileId)
        importSnapshot(
            snapshot = snapshot,
            targetAccountId = existing?.id ?: snapshot.cloudProfileId,
            isSelfProfile = false,
            notificationsEnabled = existing?.notificationsEnabled ?: false,
            syncMode = syncMode,
        )
    }

    override suspend fun removeStaleSharedProfiles(allowedCloudProfileIds: Set<String>) {
        database.withTransaction {
            database.learnerAccountDao()
                .getSyncedSubAccounts()
                .filter { entity -> entity.cloudProfileId !in allowedCloudProfileIds }
                .forEach { entity ->
                    val wasActive = entity.isActive
                    database.reviewAssignmentDao().deleteForLearner(entity.id)
                    database.reviewDayDao().deleteForLearner(entity.id)
                    database.scheduleSelectionDao().deleteForSchedule("active-${entity.id}")
                    database.revisionScheduleDao().deleteForLearner(entity.id)
                    database.learnerAccountDao().deleteAccount(entity.id)
                    if (wasActive) {
                        database.learnerAccountDao().clearActiveAccount()
                        database.learnerAccountDao().getPreferredAccount()?.let { fallback ->
                            database.learnerAccountDao().setActiveAccount(fallback.id)
                        }
                    }
                }
        }
    }

    override suspend fun removeSharedProfile(cloudProfileId: String) {
        val entity = database.learnerAccountDao().getByCloudProfileId(cloudProfileId) ?: return
        database.withTransaction {
            val wasActive = entity.isActive
            database.reviewAssignmentDao().deleteForLearner(entity.id)
            database.reviewDayDao().deleteForLearner(entity.id)
            database.scheduleSelectionDao().deleteForSchedule("active-${entity.id}")
            database.revisionScheduleDao().deleteForLearner(entity.id)
            database.learnerAccountDao().deleteAccount(entity.id)
            if (wasActive) {
                database.learnerAccountDao().clearActiveAccount()
                database.learnerAccountDao().getPreferredAccount()?.let { fallback ->
                    database.learnerAccountDao().setActiveAccount(fallback.id)
                }
            }
        }
    }

    private suspend fun importSnapshot(
        snapshot: CloudProfileSnapshot,
        targetAccountId: String,
        isSelfProfile: Boolean,
        notificationsEnabled: Boolean,
        syncMode: ProfileSyncMode,
    ) {
        database.withTransaction {
            val existing = database.learnerAccountDao().getAccount(targetAccountId)
            database.learnerAccountDao().upsert(
                LearnerAccountEntity(
                    id = targetAccountId,
                    name = snapshot.displayName,
                    isSelfProfile = isSelfProfile,
                    isShared = syncMode == ProfileSyncMode.SharedOwner || syncMode == ProfileSyncMode.SharedEditor,
                    notificationsEnabled = existing?.notificationsEnabled ?: notificationsEnabled,
                    isActive = existing?.isActive ?: false,
                    syncMode = syncMode.name,
                    cloudProfileId = snapshot.cloudProfileId,
                ),
            )
            replaceSchedule(
                learnerId = targetAccountId,
                schedule = snapshot.schedule?.copy(
                    id = "active-$targetAccountId",
                    learnerId = targetAccountId,
                ),
            )
            replaceReviewTimeline(
                learnerId = targetAccountId,
                reviewDays = snapshot.reviewDays.map { day ->
                    day.copy(
                        learnerId = targetAccountId,
                        assignments = day.assignments.map { assignment ->
                            assignment.copy(learnerId = targetAccountId)
                        },
                    )
                },
            )
        }
    }

    private suspend fun replaceSchedule(
        learnerId: String,
        schedule: RevisionSchedule?,
    ) {
        database.revisionScheduleDao().deleteForLearner(learnerId)
        database.scheduleSelectionDao().deleteForSchedule("active-$learnerId")
        if (schedule == null) {
            return
        }

        database.revisionScheduleDao().upsert(
            RevisionScheduleEntity(
                id = "active-$learnerId",
                learnerId = learnerId,
                paceMethod = schedule.paceMethod.name,
                cycleTargetDays = schedule.cycleTarget.days,
                manualPaceSegments = schedule.manualPace.dailySegments,
                isActive = true,
            ),
        )
        database.scheduleSelectionDao().insertAll(
            schedule.selections.map { selection ->
                selection.toEntity(scheduleId = "active-$learnerId")
            },
        )
    }

    private suspend fun replaceReviewTimeline(
        learnerId: String,
        reviewDays: List<ReviewDay>,
    ) {
        database.reviewAssignmentDao().deleteForLearner(learnerId)
        database.reviewDayDao().deleteForLearner(learnerId)
        if (reviewDays.isEmpty()) {
            return
        }

        database.reviewDayDao().upsertAll(
            reviewDays.map { day ->
                ReviewDayEntity(
                    id = "$learnerId-${day.assignedForDate}",
                    learnerId = learnerId,
                    assignedForDate = day.assignedForDate.toString(),
                    completionRate = day.completionRate,
                )
            },
        )
        database.reviewAssignmentDao().insertAll(
            reviewDays.flatMap { day ->
                day.assignments.map { assignment ->
                    ReviewAssignmentEntity(
                        id = assignment.id,
                        reviewDayId = "$learnerId-${day.assignedForDate}",
                        learnerId = learnerId,
                        assignedForDate = day.assignedForDate.toString(),
                        taskKey = assignment.taskKey,
                        rubId = assignment.rubId,
                        startSurahId = assignment.readingTarget?.startSurahId,
                        startAyah = assignment.readingTarget?.startAyah,
                        endSurahId = assignment.readingTarget?.endSurahId,
                        endAyah = assignment.readingTarget?.endAyah,
                        title = assignment.title,
                        detail = assignment.detail,
                        weight = assignment.weight,
                        displayOrder = assignment.displayOrder,
                        isRollover = assignment.isRollover,
                        isDone = assignment.isDone,
                        rating = assignment.rating,
                        completedAt = assignment.completedAt?.toString(),
                    )
                }
            },
        )
    }

    private fun ScheduleSelection.toEntity(scheduleId: String) =
        ScheduleSelectionEntity(
            scheduleId = scheduleId,
            category = category.name,
            itemId = itemId,
            displayOrder = displayOrder,
        )

    private fun LearnerAccountEntity.toDomainModel() =
        LearnerAccount(
            id = id,
            name = name,
            isSelfProfile = isSelfProfile,
            isShared = isShared,
            notificationsEnabled = notificationsEnabled,
            syncMode = ProfileSyncMode.valueOf(syncMode),
            cloudProfileId = cloudProfileId,
        )

    companion object {
        private const val SELF_PROFILE_ID = "self"
    }
}
