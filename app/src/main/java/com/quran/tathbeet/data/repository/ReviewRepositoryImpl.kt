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
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.min
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

    override fun observeReviewTimeline(learnerId: String): Flow<List<ReviewDay>> =
        combine(
            database.reviewDayDao().observeReviewDays(learnerId),
            database.reviewAssignmentDao().observeAssignmentsForLearner(learnerId),
        ) { dayEntities, assignmentEntities ->
            val assignmentsByDate = assignmentEntities.groupBy { it.assignedForDate }
            dayEntities.map { dayEntity ->
                dayEntity.toDomainModel(assignmentsByDate[dayEntity.assignedForDate].orEmpty())
            }
        }

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
    ): Boolean {
        val dateKey = assignedForDate.toString()
        if (database.reviewDayDao().getReviewDay(learnerId, dateKey) != null) {
            return true
        }

        val schedule = scheduleRepository.observeActiveSchedule(learnerId).first() ?: return false
        val quranCatalog = quranCatalogRepository.getCatalog()
        val selectionKeys = schedule.selections
            .map { selection -> "${selection.category.name.lowercase()}-${selection.itemId}" }
            .toSet()
        val allUnits = quranCatalog.buildReviewUnits(
            context = appContext,
            keys = selectionKeys,
        )
        if (allUnits.isEmpty()) {
            return false
        }
        val maxAssignments = when (schedule.paceMethod) {
            PaceMethod.CycleTarget -> schedule.manualPace.dailySegments
            PaceMethod.Manual -> schedule.manualPace.dailySegments
        }
        val cycleStartDate = database.reviewDayDao().getFirstReviewDay(learnerId)
            ?.assignedForDate
            ?.let(LocalDate::parse)
            ?: assignedForDate
        val dayOffset = ChronoUnit.DAYS.between(cycleStartDate, assignedForDate).toInt()
        if (dayOffset < 0) {
            return false
        }
        val startIndex = dayOffset * maxAssignments
        if (startIndex >= allUnits.size) {
            return false
        }
        val endExclusive = min(startIndex + maxAssignments, allUnits.size)
        val assignments = allUnits
            .subList(startIndex, endExclusive)
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
                    rating = null,
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
        return true
    }

    override suspend fun completeAssignment(
        assignmentId: String,
        rating: Int,
    ) {
        val completedAt = timeProvider.now().toString()
        database.reviewAssignmentDao().completeAssignment(
            assignmentId = assignmentId,
            rating = rating,
            completedAt = completedAt,
        )
    }

    override suspend fun updateAssignmentRating(
        assignmentId: String,
        rating: Int,
    ) {
        database.reviewAssignmentDao().updateRating(
            assignmentId = assignmentId,
            rating = rating,
        )
    }

    override suspend fun restartCycle(
        learnerId: String,
        restartDate: LocalDate,
    ) {
        database.withTransaction {
            database.reviewAssignmentDao().deleteForLearner(learnerId)
            database.reviewDayDao().deleteForLearner(learnerId)
        }
        ensureAssignmentsForDate(
            learnerId = learnerId,
            assignedForDate = restartDate,
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
            rating = rating,
            completedAt = completedAt?.let(ZonedDateTime::parse),
        )
}
