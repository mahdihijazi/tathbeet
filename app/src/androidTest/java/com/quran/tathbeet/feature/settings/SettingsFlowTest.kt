package com.quran.tathbeet.feature.settings

import com.quran.tathbeet.test.BaseUiFlowTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsFlowTest : BaseUiFlowTest() {

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

    @Test
    fun new_sub_profiles_start_with_notifications_disabled_but_can_be_enabled_from_settings() {
        completeOnboardingWithJuzOne()
        openProfilesTab()
        openAddProfileDialog()
        enterProfileEditorName("أحمد")
        saveProfileDialog()
        assertPoolSelectorVisible()
        navigateBack()

        val createdProfileId = activeAccountId()

        openSettingsTab()

        runBlocking {
            val accounts = appContainer.profileRepository.observeAccounts().first()
            val createdProfile = accounts.first { account -> account.id == createdProfileId }
            assertFalse(createdProfile.notificationsEnabled)
        }

        toggleProfileReminder(createdProfileId)

        runBlocking {
            val accounts = appContainer.profileRepository.observeAccounts().first()
            val createdProfile = accounts.first { account -> account.id == createdProfileId }
            assertTrue(createdProfile.notificationsEnabled)
        }

        val latestSchedule = appContainer.recordingReminderScheduler.scheduledProfiles.last()
        assertEquals(listOf("self"), latestSchedule)
    }
}
