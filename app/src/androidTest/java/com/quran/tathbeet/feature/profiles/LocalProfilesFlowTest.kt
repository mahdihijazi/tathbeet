package com.quran.tathbeet.feature.profiles

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import com.quran.tathbeet.test.BaseUiFlowTest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalProfilesFlowTest : BaseUiFlowTest() {

    @Test
    fun adding_local_profile_activates_it_and_opens_schedule_setup() {
        completeOnboardingWithJuzOne()
        openProfilesTab()

        openAddProfileDialog()
        enterProfileEditorName("أحمد")
        saveProfileDialog()

        assertPoolSelectorVisible()

        val activeProfileId = runBlocking {
            val activeAccount = appContainer.profileRepository.observeActiveAccount()
                .filterNotNull()
                .first()
            val accounts = appContainer.profileRepository.observeAccounts().first()

            assertEquals("أحمد", activeAccount.name)
            assertEquals(2, accounts.size)
            activeAccount.id
        }

        navigateBack()
        assertProfilesVisible()
        composeRule.onNodeWithTag("profiles-card-$activeProfileId").assertIsDisplayed()
    }

    @Test
    fun renaming_active_profile_updates_persistence() {
        completeOnboardingWithJuzOne()
        openProfilesTab()

        openEditActiveProfileDialog()
        enterProfileEditorName("محمود")
        saveProfileDialog()

        runBlocking {
            val activeAccount = appContainer.profileRepository.observeActiveAccount()
                .filterNotNull()
                .first()

            assertEquals("محمود", activeAccount.name)
        }
    }

    @Test
    fun toggling_child_profile_notifications_from_accounts_updates_persistence() {
        completeOnboardingWithJuzOne()
        openProfilesTab()

        openAddProfileDialog()
        enterProfileEditorName("سارة")
        saveProfileDialog()
        assertPoolSelectorVisible()
        navigateBack()

        val createdProfileId = activeAccountId()

        toggleProfileNotifications(createdProfileId)

        runBlocking {
            val accounts = appContainer.profileRepository.observeAccounts().first()
            val createdProfile = accounts.first { account -> account.id == createdProfileId }

            assertTrue(createdProfile.notificationsEnabled)
        }

        val latestSchedule = appContainer.recordingReminderScheduler.scheduledProfiles.last()
        assertEquals(listOf("self"), latestSchedule)
    }

    @Test
    fun deleting_local_profile_removes_it_and_restores_self_profile() {
        completeOnboardingWithJuzOne()
        openProfilesTab()

        openAddProfileDialog()
        enterProfileEditorName("سارة")
        saveProfileDialog()
        assertPoolSelectorVisible()
        navigateBack()
        assertProfilesVisible()

        val createdProfileId = activeAccountId()

        openEditActiveProfileDialog()
        deleteProfileFromDialog()
        confirmProfileDeletion()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            activeAccountId() == "self"
        }

        runBlocking {
            val activeAccount = appContainer.profileRepository.observeActiveAccount()
                .filterNotNull()
                .first()
            val accounts = appContainer.profileRepository.observeAccounts().first()

            assertEquals("self", activeAccount.id)
            assertFalse(accounts.any { account -> account.id == createdProfileId })
        }
    }

    @Test
    fun toggling_profile_notifications_updates_active_account() {
        completeOnboardingWithJuzOne()
        openProfilesTab()

        toggleProfileNotifications("self")

        runBlocking {
            val activeAccount = appContainer.profileRepository.observeActiveAccount()
                .filterNotNull()
                .first()

            assertFalse(activeAccount.notificationsEnabled)
        }
    }
}
