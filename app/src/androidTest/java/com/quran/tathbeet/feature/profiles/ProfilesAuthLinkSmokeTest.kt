package com.quran.tathbeet.feature.profiles

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ProfilesAuthLinkSmokeTest : BaseUiFlowTest() {

    @Test
    fun requesting_and_completing_an_email_link_signs_the_main_profile_in() {
        completeOnboardingWithJuzOne()
        openProfilesTab()

        composeRule.onNodeWithTag("profiles-account-request-link").performClick()
        composeRule.onNodeWithTag("settings-account-email-input").performTextInput("owner@example.com")
        composeRule.onNodeWithTag("settings-account-send-link").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.profile_account_state_link_sent),
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("profiles-account-request-link").assertIsDisplayed()
        composeRule.onNodeWithTag("profiles-card-self").assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_account_state_link_sent),
        ).assertIsDisplayed()

        runBlocking {
            appContainer.syncStartupCoordinator.handleIncomingAuthLink(
                "https://example.com/__/auth/links?link=https%3A%2F%2Fexample.com%2FfinishSignIn%3FoobCode%3Ddebug-link",
            )
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("profiles-account-sign-out").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("profiles-account-sign-out").assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_account_state_signed_in),
        ).assertIsDisplayed()
        composeRule.onNodeWithText("owner@example.com").assertIsDisplayed()
    }
}
