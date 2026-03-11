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

        completeReviewTask(taskId = "rub-15", rating = 3)

        scrollReviewListToTag("review-edit-rating-rub-15")
        composeRule.onNodeWithTag("review-edit-rating-rub-15").assertIsDisplayed()
        composeRule.onNodeWithTag("review-completed-rating-rub-15-3").assertIsDisplayed()

        composeRule.onNodeWithTag("review-edit-rating-rub-15").performClick()
        composeRule.onNodeWithTag("review-rating-2").performClick()

        composeRule.onNodeWithTag("review-completed-rating-rub-15-2").assertIsDisplayed()
    }

    @Test
    fun dismissing_rating_dialog_uses_default_five_stars_for_first_completion() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        completeReviewTask(taskId = "rub-15")

        scrollReviewListToTag("review-edit-rating-rub-15")
        composeRule.onNodeWithTag("review-edit-rating-rub-15").assertIsDisplayed()
        composeRule.onNodeWithTag("review-completed-rating-rub-15-5").assertIsDisplayed()
    }
}
