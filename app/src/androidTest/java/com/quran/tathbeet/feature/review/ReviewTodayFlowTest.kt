package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ReviewTodayFlowTest : BaseUiFlowTest() {

    @Test
    fun fresh_saved_plan_shows_only_real_today_assignments_without_fake_rollover() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.review_section_rollover_title),
        ).assertCountEquals(0)
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.review_section_next_title),
        ).assertCountEquals(0)

        val reviewDay = awaitTodayReviewDay()
        val firstAssignment = reviewDay.assignments.first()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_today_title),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(firstAssignment.title).assertIsDisplayed()
    }
}
