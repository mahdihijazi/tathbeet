package com.quran.tathbeet.feature.settings

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import com.quran.tathbeet.test.BaseUiFlowTest
import com.quran.tathbeet.domain.model.AppThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsFlowTest : BaseUiFlowTest() {

    @Test
    fun theme_mode_persists_from_settings() {
        completeOnboardingWithJuzOne()
        openSettingsTab()

        selectDarkThemeMode()

        runBlocking {
            val settings = appContainer.settingsRepository.observeSettings().first()
            assertEquals(AppThemeMode.Dark, settings.themeMode)
        }

        openProfilesTab()
        openSettingsTab()

        composeRule.onNodeWithTag("settings-theme-mode-dark").assertIsSelected()
    }

    @Test
    fun settings_changes_persist_and_reschedule_local_reminders() {
        completeOnboardingWithJuzOne()
        openSettingsTab()

        toggleGlobalNotifications()
        toggleGlobalNotifications()
        toggleMotivationalMessages()
        changeReminderTime(hour = 6, minute = 0)

        runBlocking {
            val settings = appContainer.settingsRepository.observeSettings().first()

            assertTrue(settings.globalNotificationsEnabled)
            assertFalse(settings.motivationalMessagesEnabled)
            assertEquals(6, settings.reminderHour)
            assertEquals(0, settings.reminderMinute)
        }

        val latestSchedule = appContainer.recordingReminderScheduler.scheduledProfiles.last()
        assertEquals(listOf("self"), latestSchedule)
    }
}
