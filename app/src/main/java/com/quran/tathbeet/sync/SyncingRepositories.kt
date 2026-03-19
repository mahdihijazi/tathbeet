package com.quran.tathbeet.sync

import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class SyncingProfileRepository(
    private val delegate: ProfileRepository,
    private val syncManager: CloudSyncManager,
) : ProfileRepository by delegate {
    override suspend fun updateAccountName(
        accountId: String,
        name: String,
    ) {
        delegate.updateAccountName(accountId, name)
        syncManager.syncAccountIfEligible(accountId)
    }

    override suspend fun deleteProfile(accountId: String) {
        val deleteResult = syncManager.deleteOwnedProfile(accountId)
        if (deleteResult == SharedProfileActionResult.EditorsMustBeRemovedFirst) {
            return
        }
        delegate.deleteProfile(accountId)
    }
}

class SyncingScheduleRepository(
    private val delegate: ScheduleRepository,
    private val syncManager: CloudSyncManager,
) : ScheduleRepository {
    override fun observeActiveSchedule(learnerId: String): Flow<RevisionSchedule?> =
        delegate.observeActiveSchedule(learnerId)

    override suspend fun saveSchedule(schedule: RevisionSchedule) {
        delegate.saveSchedule(schedule)
        syncManager.syncAccountIfEligible(schedule.learnerId)
    }
}

class SyncingReviewRepository(
    private val delegate: ReviewRepository,
    private val syncManager: CloudSyncManager,
    private val database: TathbeetDatabase,
) : ReviewRepository {
    override fun observeReviewTimeline(learnerId: String) = delegate.observeReviewTimeline(learnerId)

    override fun observeReviewDay(
        learnerId: String,
        assignedForDate: LocalDate,
    ) = delegate.observeReviewDay(learnerId, assignedForDate)

    override suspend fun ensureAssignmentsForDate(
        learnerId: String,
        assignedForDate: LocalDate,
    ): Boolean {
        val changed = delegate.ensureAssignmentsForDate(learnerId, assignedForDate)
        syncLearnerIf(changed, learnerId)
        return changed
    }

    override suspend fun completeAssignment(
        assignmentId: String,
        rating: Int,
    ) {
        delegate.completeAssignment(assignmentId, rating)
        syncAssignmentLearner(assignmentId)
    }

    override suspend fun updateAssignmentRating(
        assignmentId: String,
        rating: Int,
    ) {
        delegate.updateAssignmentRating(assignmentId, rating)
        syncAssignmentLearner(assignmentId)
    }

    override suspend fun refreshForScheduleChange(
        learnerId: String,
        restartDate: LocalDate,
    ) {
        delegate.refreshForScheduleChange(learnerId, restartDate)
        syncManager.syncAccountIfEligible(learnerId)
    }

    override suspend fun restartCycle(
        learnerId: String,
        restartDate: LocalDate,
    ) {
        delegate.restartCycle(learnerId, restartDate)
        syncManager.syncAccountIfEligible(learnerId)
    }

    private suspend fun syncAssignmentLearner(assignmentId: String) {
        database.reviewAssignmentDao().getAssignment(assignmentId)?.learnerId?.let { learnerId ->
            syncManager.syncAccountIfEligible(learnerId)
        }
    }

    private suspend fun syncLearnerIf(
        changed: Boolean,
        learnerId: String,
    ) {
        if (changed) {
            syncManager.syncAccountIfEligible(learnerId)
        }
    }
}
