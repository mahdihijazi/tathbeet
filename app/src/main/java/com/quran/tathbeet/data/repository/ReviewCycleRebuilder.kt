package com.quran.tathbeet.data.repository

import com.quran.tathbeet.data.local.entity.ReviewAssignmentEntity
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.ui.model.ReviewUnitTemplate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal data class ReviewCycleRebuildResult(
    val retainedAssignments: List<ReviewAssignmentEntity>,
    val generatedAssignments: List<ReviewAssignmentEntity>,
    val staleAssignmentIds: List<String>,
) {
    val allAssignments: List<ReviewAssignmentEntity>
        get() = retainedAssignments + generatedAssignments
}

internal fun rebuildReviewCycleAssignments(
    learnerId: String,
    restartDate: LocalDate,
    schedule: RevisionSchedule,
    allUnits: List<ReviewUnitTemplate>,
    existingAssignments: List<ReviewAssignmentEntity>,
    preservedRatings: Map<String, Int>,
): ReviewCycleRebuildResult {
    val validTaskKeys = allUnits.mapTo(linkedSetOf()) { unit -> unit.id }
    val retainedAssignments = existingAssignments.filter { assignment ->
        assignment.taskKey in validTaskKeys
    }
    val staleAssignmentIds = existingAssignments
        .filterNot { assignment -> assignment.taskKey in validTaskKeys }
        .map { assignment -> assignment.id }

    val assignedKeys = retainedAssignments.mapTo(linkedSetOf()) { assignment -> assignment.taskKey }
    val remainingUnits = ArrayDeque(allUnits.filterNot { unit -> unit.id in assignedKeys })
    if (remainingUnits.isEmpty()) {
        return ReviewCycleRebuildResult(
            retainedAssignments = retainedAssignments,
            generatedAssignments = emptyList(),
            staleAssignmentIds = staleAssignmentIds,
        )
    }

    val generatedAssignments = mutableListOf<ReviewAssignmentEntity>()
    var cursorDate = restartDate

    while (remainingUnits.isNotEmpty()) {
        val dateKey = cursorDate.toString()
        val assignmentsForDate = (retainedAssignments + generatedAssignments)
            .filter { assignment -> assignment.assignedForDate == dateKey }
        val dailyCapacity = dailyCapacityForDate(
            schedule = schedule,
            restartDate = restartDate,
            cursorDate = cursorDate,
            remainingUnits = remainingUnits.toList(),
        )
        val remainingCapacity = dailyCapacity - assignmentsForDate.sumOf { assignment -> assignment.weight }

        if (remainingCapacity > EPSILON) {
            val unitsForDate = remainingUnits.toList()
            val endExclusive = determineEndExclusive(
                units = unitsForDate,
                dailyCapacity = remainingCapacity,
            )
            if (endExclusive > 0) {
                val baseOrder = (assignmentsForDate.maxOfOrNull { it.displayOrder } ?: -1) + 1
                val selectedUnits = List(endExclusive) { remainingUnits.removeFirst() }
                generatedAssignments += selectedUnits.mapIndexed { index, unit ->
                    ReviewAssignmentEntity(
                        id = "$dateKey-${unit.id}",
                        reviewDayId = "$learnerId-$dateKey",
                        learnerId = learnerId,
                        assignedForDate = dateKey,
                        taskKey = unit.id,
                        rubId = unit.rubId,
                        startSurahId = unit.start.surahId,
                        startAyah = unit.start.ayah,
                        endSurahId = unit.end.surahId,
                        endAyah = unit.end.ayah,
                        title = unit.title,
                        detail = unit.detail,
                        weight = unit.weight,
                        displayOrder = baseOrder + index,
                        isRollover = cursorDate.isBefore(restartDate),
                        isDone = false,
                        rating = preservedRatings[unit.id],
                        completedAt = null,
                    )
                }
            }
        }
        cursorDate = cursorDate.plusDays(1)
    }

    return ReviewCycleRebuildResult(
        retainedAssignments = retainedAssignments,
        generatedAssignments = generatedAssignments,
        staleAssignmentIds = staleAssignmentIds,
    )
}

private fun dailyCapacityForDate(
    schedule: RevisionSchedule,
    restartDate: LocalDate,
    cursorDate: LocalDate,
    remainingUnits: List<ReviewUnitTemplate>,
): Double = when (schedule.paceMethod) {
    PaceMethod.Manual -> schedule.manualPace.dailySegments.toDouble()
    PaceMethod.CycleTarget -> {
        val elapsedDays = ChronoUnit.DAYS.between(restartDate, cursorDate).toInt()
        val remainingDays = (schedule.cycleTarget.days - elapsedDays).coerceAtLeast(1)
        val remainingWeight = remainingUnits.sumOf { unit -> unit.weight }
        (remainingWeight / remainingDays.toDouble()).coerceAtLeast(EPSILON)
    }
}

private fun determineEndExclusive(
    units: List<ReviewUnitTemplate>,
    dailyCapacity: Double,
): Int {
    var currentIndex = 0
    var consumedWeight = 0.0

    while (currentIndex < units.size) {
        val nextWeight = units[currentIndex].weight
        val wouldExceedCapacity = consumedWeight + nextWeight > dailyCapacity + EPSILON
        if (currentIndex > 0 && wouldExceedCapacity) {
            break
        }
        consumedWeight += nextWeight
        currentIndex += 1
        if (consumedWeight >= dailyCapacity - EPSILON) {
            break
        }
    }

    return currentIndex.coerceAtLeast(1)
}

private const val EPSILON = 0.0001
