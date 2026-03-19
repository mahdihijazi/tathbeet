package com.quran.tathbeet.feature.settings

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.quran.tathbeet.R
import com.quran.tathbeet.app.ReminderNotificationDebugScenario
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.runBlocking

class SettingsDebugFlowTest : BaseUiFlowTest() {

    @Test
    fun debug_tools_from_settings_can_trigger_a_reminder_preview() {
        completeOnboardingWithJuzOne()
        openDebugTools()

        composeRule.onNodeWithTag("debug-open-local-notifications").performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.debug_notifications_title),
        ).assertIsDisplayed()

        composeRule.onNodeWithTag("screen-layout-list").performScrollToNode(
            hasTestTag("debug-notification-trigger-${ReminderNotificationDebugScenario.TodayGeneral.id}"),
        )
        composeRule.onNodeWithTag(
            "debug-notification-trigger-${ReminderNotificationDebugScenario.TodayGeneral.id}",
        ).performClick()

        assertEquals(
            listOf(ReminderNotificationDebugScenario.TodayGeneral),
            appContainer.recordingDebugNotificationController.triggeredScenarios,
        )
    }

    @Test
    fun debug_tools_ui_catalog_can_be_opened_backed_out_and_reopened() {
        completeOnboardingWithJuzOne()
        openDebugTools()

        composeRule.onNodeWithTag("debug-open-ui-catalog").performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.debug_ui_catalog_title),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.debug_ui_catalog_screen_layout_title),
        ).assertIsDisplayed()

        navigateBack()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.debug_tools_title),
        ).assertIsDisplayed()

        composeRule.onNodeWithTag("debug-open-ui-catalog").performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.debug_ui_catalog_title),
        ).assertIsDisplayed()
    }

    @Test
    fun debug_tools_can_copy_cached_auth_link_for_replay() {
        completeOnboardingWithJuzOne()

        val expectedAuthLink = "https://example.com/finishSignIn?oobCode=debug-link"
        runBlocking {
            appContainer.debugAuthLinkStore.setLastAuthLink(expectedAuthLink)
        }

        openDebugTools()

        composeRule.onNodeWithTag("debug-auth-link-input").assertTextEquals(expectedAuthLink)
        composeRule.onNodeWithTag("debug-copy-auth-link").performClick()

        val clipboard = composeRule.activity
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copiedText = clipboard.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(composeRule.activity)
            ?.toString()

        assertEquals(expectedAuthLink, copiedText)
        composeRule.onNodeWithTag("debug-open-auth-link").assertIsDisplayed()
    }

    private fun openDebugTools() {
        openSettingsTab()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_open_debug_tools),
        ).performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.debug_tools_title),
        ).assertIsDisplayed()
    }
}
