package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ReviewCycleProgressionTest : BaseUiFlowTest() {

    @Test
    fun review_screen_reveals_future_days_until_cycle_ends_and_allows_restart() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_next_title),
        ).assertIsDisplayed()
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.review_section_day_after_next_title),
        ).assertCountEquals(0)

        toggleReviewTask("rub-15")
        toggleReviewTask("rub-16")

        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.review_section_day_after_next_title)),
        )
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_day_after_next_title),
        ).assertIsDisplayed()
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.review_section_cycle_last_title),
        ).assertCountEquals(0)

        toggleReviewTask("rub-17")

        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.review_section_cycle_last_title)),
        )
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_cycle_last_title),
        ).assertIsDisplayed()

        toggleReviewTask("rub-18")

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
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.review_cycle_complete_title),
        ).assertCountEquals(0)
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_next_title),
        ).assertIsDisplayed()
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.review_section_day_after_next_title),
        ).assertCountEquals(0)
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.review_section_cycle_last_title),
        ).assertCountEquals(0)
    }
}
