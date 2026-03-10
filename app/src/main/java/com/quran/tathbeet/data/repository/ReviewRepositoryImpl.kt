package com.quran.tathbeet.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.local.entity.ReviewAssignmentEntity
import com.quran.tathbeet.data.local.entity.ReviewDayEntity
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.repository.QuranCatalogRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.ui.model.SelectionCategory
import com.quran.tathbeet.ui.model.selectionKey
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ReviewRepositoryImpl(
    private val appContext: Context,
    private val database: TathbeetDatabase,
    private val scheduleRepository: ScheduleRepository,
    private val quranCatalogRepository: QuranCatalogRepository,
    private val timeProvider: TimeProvider,
) : ReviewRepository {

    override fun observeReviewDay(
        learnerId: String,
        assignedForDate: LocalDate,
    ): Flow<ReviewDay?> {
        val dateKey = assignedForDate.toString()
        return combine(
            database.reviewDayDao().observeReviewDay(learnerId, dateKey),
            database.reviewAssignmentDao().observeAssignments(learnerId, dateKey),
        ) { reviewDayEntity, assignmentEntities ->
            reviewDayEntity?.toDomainModel(assignmentEntities)
        }
    }

    override suspend fun ensureAssignmentsForDate(
        learnerId: String,
        assignedForDate: LocalDate,
    ) {
        val dateKey = assignedForDate.toString()
        if (database.reviewDayDao().getReviewDay(learnerId, dateKey) != null) {
            return
        }

        val schedule = scheduleRepository.observeActiveSchedule(learnerId).first() ?: return
        val quranCatalog = quranCatalogRepository.getCatalog()
        val selectionKeys = schedule.selections.map { selection ->
            selectionKey(
                category = SelectionCategory.valueOf(selection.category.name),
                itemId = selection.itemId,
            )
        }.toSet()
        val maxAssignments = when (schedule.paceMethod) {
            PaceMethod.CycleTarget -> schedule.manualPace.dailySegments
            PaceMethod.Manual -> schedule.manualPace.dailySegments
        }
        val assignments = quranCatalog.buildReviewUnits(
            context = appContext,
            keys = selectionKeys,
        )
            .take(maxAssignments)
            .mapIndexed { index, unit ->
                ReviewAssignmentEntity(
                    id = "$dateKey-${unit.id}",
                    reviewDayId = "$learnerId-$dateKey",
                    learnerId = learnerId,
                    assignedForDate = dateKey,
                    rubId = index + 1,
                    title = unit.title,
                    detail = unit.detail,
                    displayOrder = index,
                    isRollover = false,
                    isDone = false,
                    completedAt = null,
                )
            }

        database.withTransaction {
            database.reviewDayDao().upsert(
                ReviewDayEntity(
                    id = "$learnerId-$dateKey",
                    learnerId = learnerId,
                    assignedForDate = dateKey,
                    completionRate = 0,
                ),
            )
            database.reviewAssignmentDao().insertAll(assignments)
        }
    }

    override suspend fun toggleAssignmentCompletion(assignmentId: String) {
        val completedAt = timeProvider.now().toString()
        database.reviewAssignmentDao().toggleCompletion(
            assignmentId = assignmentId,
            completedAt = completedAt,
        )
    }

    private fun ReviewDayEntity.toDomainModel(
        assignments: List<ReviewAssignmentEntity>,
    ): ReviewDay {
        val completed = assignments.count { it.isDone }
        val completionRate = if (assignments.isEmpty()) 0 else (completed * 100) / assignments.size
        return ReviewDay(
            learnerId = learnerId,
            assignedForDate = LocalDate.parse(assignedForDate),
            completionRate = completionRate,
            assignments = assignments.map { assignment -> assignment.toDomainModel() },
        )
    }

    private fun ReviewAssignmentEntity.toDomainModel(): ReviewAssignment =
        ReviewAssignment(
            id = id,
            learnerId = learnerId,
            assignedForDate = LocalDate.parse(assignedForDate),
            title = title,
            detail = detail,
            rubId = rubId,
            displayOrder = displayOrder,
            isRollover = isRollover,
            isDone = isDone,
            completedAt = completedAt?.let(ZonedDateTime::parse),
        )
}
