package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ReviewTaskRatingFlowTest : BaseUiFlowTest() {

    @Test
    fun completing_task_immediately_saves_default_three_stars_and_allows_inline_edit() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val assignment = firstTodayAssignment()

        completeReviewTask(taskId = assignment.id)

        composeRule.onNodeWithTag("review-completed-rating-${assignment.id}-3").assertIsDisplayed()

        composeRule.onNodeWithTag("review-inline-rating-${assignment.id}-2").performClick()

        composeRule.onNodeWithTag("review-completed-rating-${assignment.id}-2").assertIsDisplayed()

        val updatedReviewDay = awaitTodayReviewDay()
        val updatedAssignment = updatedReviewDay.assignments.first { it.id == assignment.id }

        assert(updatedAssignment.isDone)
        assert(updatedAssignment.rating == 2)
    }

    @Test
    fun completed_task_shows_inline_stars_without_extra_edit_action() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val assignment = firstTodayAssignment()

        completeReviewTask(taskId = assignment.id)

        composeRule.onNodeWithTag("review-completed-rating-${assignment.id}-3").assertIsDisplayed()
        composeRule.onNodeWithTag("review-inline-rating-${assignment.id}-1").assertIsDisplayed()

        val updatedReviewDay = awaitTodayReviewDay()
        val updatedAssignment = updatedReviewDay.assignments.first { it.id == assignment.id }

        assert(updatedAssignment.isDone)
        assert(updatedAssignment.rating == 3)
    }
}
