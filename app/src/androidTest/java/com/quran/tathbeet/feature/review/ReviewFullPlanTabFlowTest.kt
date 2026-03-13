package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ReviewFullPlanTabFlowTest : BaseUiFlowTest() {

    @Test
    fun full_plan_sort_action_only_shows_on_full_plan_tab() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        assertSortActionHidden()

        openFullPlanTab()

        composeRule.onNodeWithTag("review-full-plan-list").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_sort_full_plan),
        ).assertIsDisplayed()

        openDailyReviewTab()
        assertSortActionHidden()
    }

    @Test
    fun full_plan_sort_modes_reorder_the_list() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val timeline = awaitReviewTimeline()
        val firstAssignment = timeline.first().assignments.first()
        val middleAssignment = timeline[timeline.lastIndex / 2].assignments.first()
        val lastAssignment = timeline.last().assignments.first()

        runBlocking {
            appContainer.database.reviewAssignmentDao().completeAssignment(
                assignmentId = firstAssignment.id,
                rating = 5,
                completedAt = ZonedDateTime.of(2026, 3, 9, 8, 0, 0, 0, ZoneId.of("UTC")).toString(),
            )
            appContainer.database.reviewAssignmentDao().completeAssignment(
                assignmentId = middleAssignment.id,
                rating = 1,
                completedAt = ZonedDateTime.of(2026, 3, 8, 8, 0, 0, 0, ZoneId.of("UTC")).toString(),
            )
            appContainer.database.reviewAssignmentDao().completeAssignment(
                assignmentId = lastAssignment.id,
                rating = 3,
                completedAt = ZonedDateTime.of(2026, 2, 1, 8, 0, 0, 0, ZoneId.of("UTC")).toString(),
            )
        }

        openFullPlanTab()

        composeRule.onNodeWithTag("review-full-plan-position-0-${middleAssignment.id}")
            .assertIsDisplayed()

        openFullPlanSortMenu()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_sort_last_memorized),
        ).performClick()
        composeRule.onNodeWithTag("review-full-plan-position-0-${lastAssignment.id}")
            .assertIsDisplayed()

        openFullPlanSortMenu()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_sort_quran_order),
        ).performClick()
        composeRule.onNodeWithTag("review-full-plan-position-0-${firstAssignment.id}")
            .assertIsDisplayed()
    }

    @Test
    fun completing_task_from_full_plan_updates_daily_timeline_list() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val tomorrowAssignment = awaitReviewTimeline()
            .first { it.assignedForDate == todayDate().plusDays(1) }
            .assignments
            .first()

        openFullPlanTab()
        composeRule.onNodeWithTag("review-full-plan-list").performScrollToNode(
            hasTestTag("review-task-${tomorrowAssignment.id}"),
        )
        composeRule.onNodeWithTag("review-complete-${tomorrowAssignment.id}").performClick()

        openDailyReviewTab()
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasTestTag("review-task-${tomorrowAssignment.id}"),
        )
        composeRule.onNodeWithTag("review-completed-rating-${tomorrowAssignment.id}-3")
            .assertIsDisplayed()
    }

    @Test
    fun rating_incomplete_task_from_full_plan_keeps_it_incomplete_and_reorders_rating_sort() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val targetAssignment = awaitReviewTimeline()
            .last()
            .assignments
            .first()

        openFullPlanTab()
        composeRule.onNodeWithTag("review-full-plan-list").performScrollToNode(
            hasTestTag("review-task-${targetAssignment.id}"),
        )
        composeRule.onNodeWithTag("review-inline-rating-${targetAssignment.id}-1").performClick()

        composeRule.onNodeWithTag("review-full-plan-position-0-${targetAssignment.id}")
            .assertIsDisplayed()

        val updatedAssignment = awaitReviewTimeline()
            .flatMap { it.assignments }
            .first { it.id == targetAssignment.id }

        assert(!updatedAssignment.isDone)
        assert(updatedAssignment.rating == 1)
    }

    private fun openDailyReviewTab() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_tab_today),
        ).performClick()
    }

    private fun openFullPlanTab() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_tab_full_plan),
        ).performClick()
    }

    private fun openFullPlanSortMenu() {
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_sort_full_plan),
        ).performClick()
    }

    private fun assertSortActionHidden() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithContentDescription(
                composeRule.activity.getString(R.string.content_sort_full_plan),
            ).fetchSemanticsNodes().isEmpty()
        }
    }
}
