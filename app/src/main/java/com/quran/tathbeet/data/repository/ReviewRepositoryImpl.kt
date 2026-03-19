package com.quran.tathbeet.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.quran.tathbeet.core.text.formatAyahCount
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.local.entity.ReviewAssignmentEntity
import com.quran.tathbeet.data.local.entity.ReviewDayEntity
import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.QuranReadingTarget
import com.quran.tathbeet.domain.repository.QuranCatalogRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.ui.model.ReviewUnitTemplate
import com.quran.tathbeet.ui.model.buildReviewUnits
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ReviewRepositoryImpl(
    private val appContext: Context,
    private val database: TathbeetDatabase,
    private val scheduleRepository: ScheduleRepository,
    private val quranCatalogRepository: QuranCatalogRepository,
    private val timeProvider: TimeProvider,
) : ReviewRepository {

    private val preservedRatingsByLearner = mutableMapOf<String, Map<String, Int>>()

    override fun observeReviewTimeline(learnerId: String): Flow<List<ReviewDay>> =
        combine(
            database.reviewDayDao().observeReviewDays(learnerId),
            database.reviewAssignmentDao().observeAssignmentsForLearner(learnerId),
        ) { dayEntities, assignmentEntities ->
            val quranCatalog = quranCatalogRepository.getCatalog()
            val assignmentsByDate = assignmentEntities.groupBy { it.assignedForDate }
            dayEntities.map { dayEntity ->
                dayEntity.toDomainModel(
                    assignments = assignmentsByDate[dayEntity.assignedForDate].orEmpty(),
                    quranCatalog = quranCatalog,
                )
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
            reviewDayEntity?.toDomainModel(
                assignments = assignmentEntities,
                quranCatalog = quranCatalogRepository.getCatalog(),
            )
        }
    }

    override suspend fun ensureAssignmentsForDate(
        learnerId: String,
        assignedForDate: LocalDate,
    ): Boolean {
        refreshPendingTimelineFromDate(
            learnerId = learnerId,
            restartDate = assignedForDate,
        )
        val dateKey = assignedForDate.toString()
        return database.reviewAssignmentDao().getAssignments(learnerId, dateKey).isNotEmpty()
    }

    override suspend fun completeAssignment(
        assignmentId: String,
        rating: Int,
    ) {
        database.reviewAssignmentDao().completeAssignment(
            assignmentId = assignmentId,
            rating = rating,
            completedAt = timeProvider.now().toString(),
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
        preservedRatingsByLearner[learnerId] = collectPreservedRatings(learnerId)
        database.withTransaction {
            database.reviewAssignmentDao().deleteForLearner(learnerId)
            database.reviewDayDao().deleteForLearner(learnerId)
        }
        rebuildCycleFromDate(
            learnerId = learnerId,
            restartDate = restartDate,
        )
    }

    override suspend fun refreshForScheduleChange(
        learnerId: String,
        restartDate: LocalDate,
    ) {
        refreshPendingTimelineFromDate(
            learnerId = learnerId,
            restartDate = restartDate,
        )
    }

    private suspend fun refreshPendingTimelineFromDate(
        learnerId: String,
        restartDate: LocalDate,
    ) {
        preservedRatingsByLearner[learnerId] = collectPreservedRatings(learnerId)
        database.withTransaction {
            database.reviewAssignmentDao().deletePendingAssignmentsOnOrAfter(
                learnerId = learnerId,
                assignedForDate = restartDate.toString(),
            )
            database.reviewDayDao().deleteOnOrAfter(
                learnerId = learnerId,
                assignedForDate = restartDate.toString(),
            )
        }
        rebuildCycleFromDate(
            learnerId = learnerId,
            restartDate = restartDate,
        )
    }

    private suspend fun rebuildCycleFromDate(
        learnerId: String,
        restartDate: LocalDate,
    ) {
        val schedule = scheduleRepository.observeActiveSchedule(learnerId).first() ?: return
        val allUnits = loadReviewUnits(schedule)
        if (allUnits.isEmpty()) {
            database.reviewDayDao().deleteEmptyDays(learnerId)
            return
        }

        val existingAssignments = database.reviewAssignmentDao().getAssignmentsForLearner(learnerId)
        val rebuildResult = rebuildReviewCycleAssignments(
            learnerId = learnerId,
            restartDate = restartDate,
            schedule = schedule,
            allUnits = allUnits,
            existingAssignments = existingAssignments,
            preservedRatings = preservedRatingsByLearner[learnerId].orEmpty(),
        )

        val reviewDays = rebuildResult.allAssignments
            .groupBy { assignment -> assignment.assignedForDate }
            .map { (dateKey, assignments) ->
                ReviewDayEntity(
                    id = "$learnerId-$dateKey",
                    learnerId = learnerId,
                    assignedForDate = dateKey,
                    completionRate = completionRate(assignments),
                )
            }

        database.withTransaction {
            if (rebuildResult.staleAssignmentIds.isNotEmpty()) {
                database.reviewAssignmentDao().deleteByIds(rebuildResult.staleAssignmentIds)
            }
            if (rebuildResult.generatedAssignments.isNotEmpty()) {
                database.reviewAssignmentDao().insertAll(rebuildResult.generatedAssignments)
            }
            if (reviewDays.isNotEmpty()) {
                database.reviewDayDao().upsertAll(reviewDays)
            }
            database.reviewDayDao().deleteEmptyDays(learnerId)
        }
    }

    private suspend fun collectPreservedRatings(learnerId: String): Map<String, Int> =
        database.reviewAssignmentDao()
            .getRatedAssignments(learnerId)
            .fold(linkedMapOf<String, Int>()) { acc, assignment ->
                if (assignment.rating != null && !acc.containsKey(assignment.taskKey)) {
                    acc[assignment.taskKey] = assignment.rating
                }
                acc
            }

    private fun loadReviewUnits(schedule: RevisionSchedule): List<ReviewUnitTemplate> {
        val selectionKeys = schedule.selections
            .map { selection -> "${selection.category.name.lowercase()}-${selection.itemId}" }
            .toSet()
        return quranCatalogRepository.getCatalog().buildReviewUnits(
            context = appContext,
            keys = selectionKeys,
        )
    }

    private fun ReviewDayEntity.toDomainModel(
        assignments: List<ReviewAssignmentEntity>,
        quranCatalog: com.quran.tathbeet.ui.model.QuranCatalog,
    ): ReviewDay {
        val completed = assignments.count { it.isDone }
        val completionRate = if (assignments.isEmpty()) 0 else (completed * 100) / assignments.size
        return ReviewDay(
            learnerId = learnerId,
            assignedForDate = LocalDate.parse(assignedForDate),
            completionRate = completionRate,
            assignments = assignments.map { assignment -> assignment.toDomainModel(quranCatalog) },
        )
    }

    private fun ReviewAssignmentEntity.toDomainModel(
        quranCatalog: com.quran.tathbeet.ui.model.QuranCatalog,
    ): ReviewAssignment =
        ReviewAssignment(
            id = id,
            learnerId = learnerId,
            assignedForDate = LocalDate.parse(assignedForDate),
            taskKey = taskKey,
            title = title,
            detail = normalizeDetail(quranCatalog),
            rubId = rubId,
            readingTarget = readingTarget(),
            weight = weight,
            displayOrder = displayOrder,
            isRollover = isRollover,
            isDone = isDone,
            rating = rating,
            completedAt = completedAt?.let(ZonedDateTime::parse),
        )

    private fun ReviewAssignmentEntity.readingTarget(): QuranReadingTarget? {
        val resolvedStartSurahId = startSurahId ?: return null
        val resolvedStartAyah = startAyah ?: return null
        val resolvedEndSurahId = endSurahId ?: return null
        val resolvedEndAyah = endAyah ?: return null
        return QuranReadingTarget(
            startSurahId = resolvedStartSurahId,
            startAyah = resolvedStartAyah,
            endSurahId = resolvedEndSurahId,
            endAyah = resolvedEndAyah,
        )
    }

    private fun ReviewAssignmentEntity.normalizeDetail(
        quranCatalog: com.quran.tathbeet.ui.model.QuranCatalog,
    ): String {
        val surahId = taskKey.removePrefix("surah-").toIntOrNull() ?: return detail
        return quranCatalog.surahAyahCounts[surahId]
            ?.let { ayahCount -> formatAyahCount(appContext, ayahCount) }
            ?: detail
    }

    private fun completionRate(assignments: List<ReviewAssignmentEntity>): Int {
        if (assignments.isEmpty()) return 0
        val completed = assignments.count { assignment -> assignment.isDone }
        return (completed * 100) / assignments.size
    }
}
