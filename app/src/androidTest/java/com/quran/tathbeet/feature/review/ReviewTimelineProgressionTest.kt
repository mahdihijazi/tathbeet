package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ReviewTimelineProgressionTest : BaseUiFlowTest() {

    @Test
    fun completing_today_reveals_tomorrows_work_automatically() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.review_section_next_title),
        ).assertCountEquals(0)

        val todayAssignment = firstTodayAssignment()
        completeReviewTask(todayAssignment.id, rating = 4)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.review_section_next_title),
            ).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_next_title),
        ).assertIsDisplayed()

        val tomorrowDay = awaitReviewDay(todayDate().plusDays(1))
        composeRule.onNodeWithText(tomorrowDay.assignments.first().title).assertIsDisplayed()
    }

    @Test
    fun completing_full_cycle_shows_restart_dialog_and_restart_resets_to_first_day() {
        tapNext()
        assertPoolSelectorVisible()
        selectVisibleSurah("الفاتحة")
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val firstAssignment = firstTodayAssignment()
        completeReviewTask(firstAssignment.id)

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
            composeRule.activity.getString(R.string.review_section_next_title),
        ).assertCountEquals(0)
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_today_title),
        ).assertIsDisplayed()

        val restartedToday = awaitTodayReviewDay()
        val restartedAssignment = restartedToday.assignments.first()
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(restartedAssignment.title),
        )
        composeRule.onNodeWithText(restartedAssignment.title).assertIsDisplayed()
        composeRule.onNodeWithTag("review-complete-${restartedAssignment.id}").assertIsDisplayed()
    }
}
