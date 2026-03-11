package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ReviewTodayFlowTest : BaseUiFlowTest() {

    @Test
    fun fresh_saved_plan_shows_real_cycle_tasks_without_fake_rollover() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        val timeline = awaitReviewTimeline()
        val firstTodayAssignment = timeline.first { it.assignedForDate == todayDate() }.assignments.first()
        val tomorrowAssignment = timeline.first { it.assignedForDate == todayDate().plusDays(1) }.assignments.first()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.review_section_today_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(firstTodayAssignment.title).assertIsDisplayed()
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.review_section_next_title)),
        )
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.review_section_next_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(tomorrowAssignment.title).assertIsDisplayed()
    }
}
