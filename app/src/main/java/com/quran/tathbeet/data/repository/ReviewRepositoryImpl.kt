package com.quran.tathbeet.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.quran.tathbeet.core.text.formatAyahCount
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
import com.quran.tathbeet.ui.model.ReviewUnitTemplate
import com.quran.tathbeet.ui.model.buildReviewUnits
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.max
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
        val dailyCapacity = when (schedule.paceMethod) {
            PaceMethod.CycleTarget -> schedule.manualPace.dailySegments.toDouble()
            PaceMethod.Manual -> schedule.manualPace.dailySegments.toDouble()
        }
        val startIndex = database.reviewAssignmentDao().countAssignmentsBeforeDate(
            learnerId = learnerId,
            assignedForDate = dateKey,
        )
        if (startIndex >= allUnits.size) {
            return false
        }
        val endExclusive = determineEndExclusive(
            units = allUnits,
            startIndex = startIndex,
            dailyCapacity = dailyCapacity,
        )
        val preservedRatings = preservedRatingsByLearner[learnerId].orEmpty()
        val assignments = allUnits
            .subList(startIndex, endExclusive)
            .mapIndexed { index, unit ->
                ReviewAssignmentEntity(
                    id = "$dateKey-${unit.id}",
                    reviewDayId = "$learnerId-$dateKey",
                    learnerId = learnerId,
                    assignedForDate = dateKey,
                    taskKey = unit.id,
                    rubId = unit.rubId,
                    title = unit.title,
                    detail = unit.detail,
                    weight = unit.weight,
                    displayOrder = index,
                    isRollover = false,
                    isDone = false,
                    rating = preservedRatings[unit.id],
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
        preservedRatingsByLearner[learnerId] = database.reviewAssignmentDao()
            .getRatedAssignments(learnerId)
            .fold(linkedMapOf<String, Int>()) { acc, assignment ->
                if (assignment.rating != null && !acc.containsKey(assignment.taskKey)) {
                    acc[assignment.taskKey] = assignment.rating
                }
                acc
            }
        database.withTransaction {
            database.reviewAssignmentDao().deleteForLearner(learnerId)
            database.reviewDayDao().deleteForLearner(learnerId)
        }
        ensureAssignmentsForDate(
            learnerId = learnerId,
            assignedForDate = restartDate,
        )
    }

    override suspend fun refreshForScheduleChange(
        learnerId: String,
        restartDate: LocalDate,
    ) {
        restartCycle(
            learnerId = learnerId,
            restartDate = restartDate,
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
            weight = weight,
            displayOrder = displayOrder,
            isRollover = isRollover,
            isDone = isDone,
            rating = rating,
            completedAt = completedAt?.let(ZonedDateTime::parse),
        )

    private fun ReviewAssignmentEntity.normalizeDetail(
        quranCatalog: com.quran.tathbeet.ui.model.QuranCatalog,
    ): String {
        val surahId = taskKey.removePrefix("surah-").toIntOrNull() ?: return detail
        return quranCatalog.surahAyahCounts[surahId]
            ?.let { ayahCount -> formatAyahCount(appContext, ayahCount) }
            ?: detail
    }

    private fun determineEndExclusive(
        units: List<ReviewUnitTemplate>,
        startIndex: Int,
        dailyCapacity: Double,
    ): Int {
        var currentIndex = startIndex
        var consumedWeight = 0.0

        while (currentIndex < units.size) {
            val nextWeight = units[currentIndex].weight
            val wouldExceedCapacity = consumedWeight + nextWeight > dailyCapacity + EPSILON
            if (currentIndex > startIndex && wouldExceedCapacity) {
                break
            }
            consumedWeight += nextWeight
            currentIndex += 1
            if (consumedWeight >= dailyCapacity - EPSILON) {
                break
            }
        }

        return max(startIndex + 1, currentIndex)
    }

    private companion object {
        private const val EPSILON = 0.0001
    }
}
