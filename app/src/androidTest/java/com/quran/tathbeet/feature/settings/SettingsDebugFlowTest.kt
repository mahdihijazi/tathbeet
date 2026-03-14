package com.quran.tathbeet.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
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
