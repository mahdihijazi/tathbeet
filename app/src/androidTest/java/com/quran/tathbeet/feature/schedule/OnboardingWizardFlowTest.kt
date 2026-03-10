package com.quran.tathbeet.feature.schedule

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test

class OnboardingWizardFlowTest : BaseUiFlowTest() {

    @Test
    fun onboarding_pool_step_disables_next_until_user_selects_at_least_one_item() {
        tapNext()
        assertPoolSelectorVisible()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.action_next),
        ).assertIsNotEnabled()
    }

    @Test
    fun onboarding_flow_saves_schedule_and_opens_review() {
        completeOnboardingWithJuzOne()

        assertReviewVisible()

        runBlocking {
            val account = appContainer.profileRepository.observeActiveAccount()
                .filterNotNull()
                .first()
            val schedule = appContainer.scheduleRepository.observeActiveSchedule(account.id)
                .filterNotNull()
                .first()

            assert(schedule.selections.any { it.itemId == 1 })
        }
    }
}
