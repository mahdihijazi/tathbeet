package com.quran.tathbeet.feature.profiles

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.R
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ScheduleSelection
import com.quran.tathbeet.domain.model.SelectionCategory
import com.quran.tathbeet.test.BaseMainActivityUiTest
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ProfilesColdLaunchPersistenceTest : BaseMainActivityUiTest() {

    @Test
    fun relaunching_after_seeding_profiles_keeps_the_profile_cards_visible() {
        val seeded = seedProfiles()
        try {
            launchMainActivity().use {
                openProfilesTab()
                assertProfileCardsVisible(seeded.childProfileId)
            }

            launchMainActivity().use {
                openProfilesTab()
                assertProfileCardsVisible(seeded.childProfileId)
            }
        } finally {
            seeded.appContainer.database.close()
        }
    }

    private fun openProfilesTab() {
        composeRule.onNodeWithText(
            targetContext.getString(R.string.destination_profiles),
        ).performClick()
    }

    private fun assertProfileCardsVisible(childProfileId: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("profiles-card-self").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithTag("profiles-card-$childProfileId").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("profiles-card-self").assertIsDisplayed()
        composeRule.onNodeWithTag("profiles-card-$childProfileId").assertIsDisplayed()
    }

    private fun seedProfiles(): SeededProfiles {
        val appContainer = AppContainer(targetContext)
        return runBlocking {
            appContainer.profileRepository.ensureDefaultAccount(
                name = targetContext.getString(R.string.profile_name_self),
            )
            val childProfile = appContainer.profileRepository.createProfile("أحمد")
            appContainer.profileRepository.setActiveAccount("self")
            appContainer.scheduleRepository.saveSchedule(
                RevisionSchedule(
                    id = "active-self",
                    learnerId = "self",
                    paceMethod = PaceMethod.CycleTarget,
                    cycleTarget = CycleTarget.OneMonth,
                    manualPace = PaceOption.OneJuz,
                    selections = listOf(
                        ScheduleSelection(
                            category = SelectionCategory.Juz,
                            itemId = 1,
                            displayOrder = 0,
                        ),
                    ),
                ),
            )
            appContainer.reviewRepository.refreshForScheduleChange(
                learnerId = "self",
                restartDate = appContainer.timeProvider.today(),
            )
            appContainer.reviewRepository.ensureAssignmentsForDate(
                learnerId = "self",
                assignedForDate = appContainer.timeProvider.today(),
            )

            SeededProfiles(
                appContainer = appContainer,
                childProfileId = childProfile.id,
            )
        }
    }
}

private data class SeededProfiles(
    val appContainer: AppContainer,
    val childProfileId: String,
)
