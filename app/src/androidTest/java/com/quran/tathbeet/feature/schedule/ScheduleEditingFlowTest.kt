package com.quran.tathbeet.feature.schedule

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class ScheduleEditingFlowTest : BaseUiFlowTest() {

    @Test
    fun edit_plan_from_review_reopens_wizard_and_saves_without_crashing() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).performClick()

        assertPoolSelectorVisible()

        tapNext()
        assertScheduleDoseVisible()

        saveSchedule()
        assertReviewVisible()

        runBlocking {
            val account = appContainer.profileRepository.observeActiveAccount()
                .filterNotNull()
                .first()
            val schedule = appContainer.scheduleRepository.observeActiveSchedule(account.id)
                .filterNotNull()
                .first()

            assert(schedule.selections.isNotEmpty())
        }
    }

    @Test
    fun edit_plan_opens_directly_on_pool_selector_without_onboarding_header() {
        completeOnboardingWithJuzOne()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).performClick()

        assertPoolSelectorVisible()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.wizard_step_counter, 2, 3),
            ).fetchSemanticsNodes().isEmpty()
        }
    }
}
