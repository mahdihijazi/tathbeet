package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ReviewTaskRatingFlowTest : BaseUiFlowTest() {

    @Test
    fun completing_task_opens_rating_dialog_and_editing_updates_saved_rating() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val assignment = firstTodayAssignment()

        completeReviewTask(taskId = assignment.id, rating = 3)

        scrollReviewListToTag("review-edit-rating-${assignment.id}")
        composeRule.onNodeWithTag("review-edit-rating-${assignment.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("review-completed-rating-${assignment.id}-3").assertIsDisplayed()

        composeRule.onNodeWithTag("review-edit-rating-${assignment.id}").performClick()
        composeRule.onNodeWithTag("review-rating-2").performClick()

        composeRule.onNodeWithTag("review-completed-rating-${assignment.id}-2").assertIsDisplayed()

        val updatedReviewDay = awaitTodayReviewDay()
        val updatedAssignment = updatedReviewDay.assignments.first { it.id == assignment.id }

        assert(updatedAssignment.isDone)
        assert(updatedAssignment.rating == 2)
    }

    @Test
    fun dismissing_rating_dialog_uses_default_five_stars_for_first_completion() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val assignment = firstTodayAssignment()

        completeReviewTask(taskId = assignment.id)

        scrollReviewListToTag("review-edit-rating-${assignment.id}")
        composeRule.onNodeWithTag("review-edit-rating-${assignment.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("review-completed-rating-${assignment.id}-5").assertIsDisplayed()

        val updatedReviewDay = awaitTodayReviewDay()
        val updatedAssignment = updatedReviewDay.assignments.first { it.id == assignment.id }

        assert(updatedAssignment.isDone)
        assert(updatedAssignment.rating == 5)
    }
}
