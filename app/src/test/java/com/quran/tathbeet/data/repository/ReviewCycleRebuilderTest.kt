package com.quran.tathbeet.data.repository

import com.quran.tathbeet.data.local.entity.ReviewAssignmentEntity
import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.ui.model.Boundary
import com.quran.tathbeet.ui.model.ReviewUnitTemplate
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewCycleRebuilderTest {

    @Test
    fun preserves_existing_completed_and_overdue_assignments_when_they_still_exist_in_plan() {
        val result = rebuildReviewCycleAssignments(
            learnerId = "learner-1",
            restartDate = LocalDate.of(2026, 3, 14),
            schedule = manualSchedule(),
            allUnits = listOf(reviewUnit("rub-1", 1), reviewUnit("rub-2", 2), reviewUnit("rub-3", 3)),
            existingAssignments = listOf(
                assignment(
                    id = "2026-03-13-rub-1",
                    taskKey = "rub-1",
                    assignedForDate = "2026-03-13",
                    isDone = false,
                    displayOrder = 0,
                ),
                assignment(
                    id = "2026-03-14-rub-2",
                    taskKey = "rub-2",
                    assignedForDate = "2026-03-14",
                    isDone = true,
                    rating = 4,
                    displayOrder = 0,
                ),
            ),
            preservedRatings = mapOf("rub-2" to 4),
        )

        assertTrue(result.retainedAssignments.any { it.taskKey == "rub-1" && !it.isDone })
        assertTrue(result.retainedAssignments.any { it.taskKey == "rub-2" && it.isDone && it.rating == 4 })
        assertTrue(result.generatedAssignments.any { it.taskKey == "rub-3" })
        assertTrue(result.staleAssignmentIds.isEmpty())
    }

    @Test
    fun drops_existing_assignments_that_are_no_longer_in_the_edited_plan_even_if_completed() {
        val result = rebuildReviewCycleAssignments(
            learnerId = "learner-1",
            restartDate = LocalDate.of(2026, 3, 14),
            schedule = manualSchedule(),
            allUnits = listOf(reviewUnit("rub-1", 1), reviewUnit("rub-3", 3)),
            existingAssignments = listOf(
                assignment(
                    id = "2026-03-14-rub-2",
                    taskKey = "rub-2",
                    assignedForDate = "2026-03-14",
                    isDone = true,
                    rating = 5,
                    displayOrder = 0,
                ),
            ),
            preservedRatings = mapOf("rub-2" to 5),
        )

        assertEquals(listOf("2026-03-14-rub-2"), result.staleAssignmentIds)
        assertFalse(result.allAssignments.any { it.taskKey == "rub-2" })
    }

    @Test
    fun adds_new_units_that_now_fall_before_and_after_a_preserved_completed_task() {
        val result = rebuildReviewCycleAssignments(
            learnerId = "learner-1",
            restartDate = LocalDate.of(2026, 3, 14),
            schedule = manualSchedule(),
            allUnits = listOf(reviewUnit("rub-1", 1), reviewUnit("rub-2", 2), reviewUnit("rub-3", 3)),
            existingAssignments = listOf(
                assignment(
                    id = "2026-03-14-rub-2",
                    taskKey = "rub-2",
                    assignedForDate = "2026-03-14",
                    isDone = true,
                    rating = 4,
                    displayOrder = 0,
                ),
            ),
            preservedRatings = mapOf("rub-2" to 4),
        )

        assertEquals(setOf("rub-1", "rub-3"), result.generatedAssignments.map { it.taskKey }.toSet())
        assertTrue(result.allAssignments.any { it.taskKey == "rub-2" && it.isDone })
    }

    @Test
    fun removed_pending_future_units_are_not_recreated_after_plan_edit() {
        val result = rebuildReviewCycleAssignments(
            learnerId = "learner-1",
            restartDate = LocalDate.of(2026, 3, 14),
            schedule = manualSchedule(),
            allUnits = listOf(reviewUnit("rub-1", 1), reviewUnit("rub-3", 3)),
            existingAssignments = listOf(
                assignment(
                    id = "2026-03-14-rub-1",
                    taskKey = "rub-1",
                    assignedForDate = "2026-03-14",
                    isDone = true,
                    rating = 4,
                    displayOrder = 0,
                ),
            ),
            preservedRatings = mapOf("rub-1" to 4),
        )

        assertFalse(result.allAssignments.any { it.taskKey == "rub-2" })
        assertTrue(result.generatedAssignments.any { it.taskKey == "rub-3" })
    }

    private fun manualSchedule(): RevisionSchedule =
        RevisionSchedule(
            id = "schedule-1",
            learnerId = "learner-1",
            paceMethod = PaceMethod.Manual,
            cycleTarget = CycleTarget.OneWeek,
            manualPace = PaceOption.OneHizb,
            selections = emptyList(),
        )

    private fun reviewUnit(
        id: String,
        rubId: Int,
    ): ReviewUnitTemplate =
        ReviewUnitTemplate(
            id = id,
            rubId = rubId,
            title = id,
            detail = "detail-$id",
            weight = 1.0,
            isPartial = false,
            start = Boundary(surahId = rubId, surahNameArabic = "سورة $rubId", ayah = 1),
            end = Boundary(surahId = rubId, surahNameArabic = "سورة $rubId", ayah = 7),
        )

    private fun assignment(
        id: String,
        taskKey: String,
        assignedForDate: String,
        isDone: Boolean,
        displayOrder: Int,
        rating: Int? = null,
    ): ReviewAssignmentEntity =
        ReviewAssignmentEntity(
            id = id,
            reviewDayId = "learner-1-$assignedForDate",
            learnerId = "learner-1",
            assignedForDate = assignedForDate,
            taskKey = taskKey,
            rubId = taskKey.removePrefix("rub-").toInt(),
            startSurahId = 1,
            startAyah = 1,
            endSurahId = 1,
            endAyah = 7,
            title = taskKey,
            detail = "detail-$taskKey",
            weight = 1.0,
            displayOrder = displayOrder,
            isRollover = assignedForDate < "2026-03-14",
            isDone = isDone,
            rating = rating,
            completedAt = if (isDone) "2026-03-14T10:00:00Z" else null,
        )
}
