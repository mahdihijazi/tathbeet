package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ReviewTimelineProgressionTest : BaseUiFlowTest() {

    @Test
    fun review_screen_shows_full_current_cycle_from_start() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val timeline = awaitReviewTimeline()
        val todayAssignment = timeline.first { it.assignedForDate == todayDate() }.assignments.first()
        val tomorrowAssignment = timeline.first { it.assignedForDate == todayDate().plusDays(1) }.assignments.first()
        val lastDay = timeline.maxBy { it.assignedForDate }
        val lastAssignment = lastDay.assignments.first()

        composeRule.onNodeWithText(todayAssignment.title).assertIsDisplayed()
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.review_section_next_title)),
        )
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_next_title),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(tomorrowAssignment.title).assertIsDisplayed()
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(lastAssignment.title),
        )
        composeRule.onNodeWithText(lastAssignment.title).assertIsDisplayed()
    }

    @Test
    fun completing_full_cycle_shows_restart_dialog_and_restart_resets_to_first_day() {
        tapNext()
        assertPoolSelectorVisible()
        selectVisibleSurah("الفاتحة")
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val initialTimeline = awaitReviewTimeline()
        initialTimeline.flatMap { it.assignments }.forEach { assignment ->
            completeReviewTask(assignment.id)
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.review_cycle_complete_title),
            ).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_cycle_complete_title),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_cycle_restart),
        ).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.review_cycle_complete_title),
            ).fetchSemanticsNodes().isEmpty()
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_today_title),
        ).assertIsDisplayed()

        val restartedToday = awaitTodayReviewDay()
        val restartedAssignment = restartedToday.assignments.first()
        scrollReviewListToTag("review-task-${restartedAssignment.id}")
        composeRule.onNodeWithTag("review-task-${restartedAssignment.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("review-complete-${restartedAssignment.id}").assertIsDisplayed()
    }

    @Test
    fun reset_cycle_from_toolbar_requires_confirmation_and_resets_progress() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val initialAssignment = awaitTodayReviewDay().assignments.first()
        completeReviewTask(initialAssignment.id, rating = 4)

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_reset_cycle),
        ).performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_cycle_reset_title),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_cycle_reset_cancel),
        ).performClick()

        composeRule.onNodeWithTag("review-completed-rating-${initialAssignment.id}-4").assertIsDisplayed()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_reset_cycle),
        ).performClick()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_cycle_reset_confirm),
        ).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.review_cycle_reset_title),
            ).fetchSemanticsNodes().isEmpty()
        }

        val restartedToday = awaitTodayReviewDay()
        val restartedAssignment = restartedToday.assignments.first()
        scrollReviewListToTag("review-task-${restartedAssignment.id}")
        composeRule.onNodeWithTag("review-task-${restartedAssignment.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("review-complete-${restartedAssignment.id}").assertIsDisplayed()
    }
}
