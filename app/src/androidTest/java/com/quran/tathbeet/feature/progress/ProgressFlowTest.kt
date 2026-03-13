package com.quran.tathbeet.feature.progress

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.R
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.test.BaseUiFlowTest
import java.time.LocalDate
import org.junit.Test

class ProgressFlowTest : BaseUiFlowTest() {

    @Test
    fun progress_screen_uses_real_review_timeline_data_and_updates_after_task_completion() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        openProgressTab()
        assertProgressVisible()
        val initialExpectation = awaitReviewTimeline().toExpectation(todayDate())
        assertProgressSummary(initialExpectation)

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.progress_back_to_review),
        ).performClick()

        val assignmentToComplete = awaitTodayReviewDay().assignments.first { assignment -> !assignment.isDone }
        completeReviewTask(assignmentToComplete.id)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            awaitTodayReviewDay().assignments.any { assignment ->
                assignment.id == assignmentToComplete.id && assignment.isDone
            }
        }

        openProgressTab()
        assertProgressVisible()
        val updatedExpectation = awaitReviewTimeline().toExpectation(todayDate())

        check(updatedExpectation.todayCompleted > initialExpectation.todayCompleted) {
            "Expected Progress data to reflect the completed review task."
        }
        assertProgressSummary(updatedExpectation)
    }

    private fun openProgressTab() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.destination_progress),
        ).performClick()
    }

    private fun assertProgressVisible() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.progress_today_title),
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.progress_today_title),
        ).assertIsDisplayed()
    }

    private fun assertProgressSummary(expectation: ProgressExpectation) {
        composeRule.onNodeWithText(
            composeRule.activity.getString(
                R.string.progress_today_ratio,
                expectation.todayCompleted,
                expectation.todayTotal,
            ),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            if (expectation.remainingCount == 0) {
                composeRule.activity.getString(R.string.progress_today_done)
            } else {
                composeRule.activity.getString(
                    R.string.progress_today_remaining,
                    expectation.remainingCount,
                )
            },
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.percentage_value, expectation.completionRate),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(
                R.string.progress_week_days_value,
                expectation.completedDays,
                7,
            ),
        ).assertIsDisplayed()
    }
}

private data class ProgressExpectation(
    val todayCompleted: Int,
    val todayTotal: Int,
    val remainingCount: Int,
    val completionRate: Int,
    val completedDays: Int,
)

private fun List<ReviewDay>.toExpectation(today: LocalDate): ProgressExpectation {
    val overdueAssignments = filter { day -> day.assignedForDate.isBefore(today) }
        .flatMap { day -> day.assignments }
    val todayAssignments = firstOrNull { day -> day.assignedForDate == today }?.assignments.orEmpty()
    val visibleAssignments = overdueAssignments + todayAssignments
    val weeklyCompletionRates = (0..6).map { offset ->
        val date = today.minusDays((6 - offset).toLong())
        firstOrNull { day -> day.assignedForDate == date }?.completionRate ?: 0
    }
    return ProgressExpectation(
        todayCompleted = visibleAssignments.count { assignment -> assignment.isDone },
        todayTotal = visibleAssignments.size,
        remainingCount = visibleAssignments.count { assignment -> !assignment.isDone },
        completionRate = weeklyCompletionRates.average().toInt(),
        completedDays = weeklyCompletionRates.count { rate -> rate >= 100 },
    )
}
