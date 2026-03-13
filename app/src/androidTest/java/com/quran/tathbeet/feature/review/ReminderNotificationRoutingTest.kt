package com.quran.tathbeet.feature.review

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import com.quran.tathbeet.MainActivity
import com.quran.tathbeet.R
import com.quran.tathbeet.app.AndroidLocalReminderScheduler
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

class ReminderNotificationRoutingTest : BaseMainActivityUiTest() {

    @Test
    fun reminder_notification_cold_launch_opens_target_profile_review() {
        val seeded = seedReminderProfiles()
        try {
            launchMainActivity(reminderIntent(seeded.targetProfileId))
            assertTargetProfileReviewVisible(seeded)
        } finally {
            seeded.appContainer.database.close()
        }
    }

    @Test
    fun reminder_notification_while_app_is_open_switches_to_target_profile_review() {
        val seeded = seedReminderProfiles()
        try {
            val scenario = launchMainActivity()
            assertReviewVisible()
            scenario.onActivity { activity ->
                MainActivity::class.java
                    .getDeclaredMethod("onNewIntent", Intent::class.java)
                    .apply { isAccessible = true }
                    .invoke(activity, reminderIntent(seeded.targetProfileId))
            }
            assertTargetProfileReviewVisible(seeded)
        } finally {
            seeded.appContainer.database.close()
        }
    }

    private fun assertTargetProfileReviewVisible(seeded: SeededReminderProfiles) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runBlocking {
                seeded.appContainer.database.learnerAccountDao().getActiveAccount()?.id
            } == seeded.targetProfileId
        }

        assertReviewVisible()
    }

    private fun seedReminderProfiles(): SeededReminderProfiles {
        val appContainer = AppContainer(targetContext)
        return runBlocking {
            appContainer.profileRepository.ensureDefaultAccount(
                name = targetContext.getString(R.string.profile_name_self),
            )
            saveSchedule(
                appContainer = appContainer,
                learnerId = "self",
                selection = ScheduleSelection(
                    category = SelectionCategory.Juz,
                    itemId = 1,
                    displayOrder = 0,
                ),
            )

            val targetProfile = appContainer.profileRepository.createProfile("أحمد")
            appContainer.profileRepository.updateNotificationsEnabled(targetProfile.id, true)
            saveSchedule(
                appContainer = appContainer,
                learnerId = targetProfile.id,
                selection = ScheduleSelection(
                    category = SelectionCategory.Surahs,
                    itemId = 112,
                    displayOrder = 0,
                ),
            )
            appContainer.profileRepository.setActiveAccount("self")

            SeededReminderProfiles(
                appContainer = appContainer,
                targetProfileId = targetProfile.id,
            )
        }
    }

    private suspend fun saveSchedule(
        appContainer: AppContainer,
        learnerId: String,
        selection: ScheduleSelection,
    ) {
        appContainer.scheduleRepository.saveSchedule(
            RevisionSchedule(
                id = "active-$learnerId",
                learnerId = learnerId,
                paceMethod = PaceMethod.CycleTarget,
                cycleTarget = CycleTarget.OneMonth,
                manualPace = PaceOption.OneRub,
                selections = listOf(selection),
            ),
        )
        appContainer.reviewRepository.refreshForScheduleChange(
            learnerId = learnerId,
            restartDate = appContainer.timeProvider.today(),
        )
        appContainer.reviewRepository.ensureAssignmentsForDate(
            learnerId = learnerId,
            assignedForDate = appContainer.timeProvider.today(),
        )
    }

    private fun reminderIntent(profileId: String): Intent =
        Intent(targetContext, MainActivity::class.java).apply {
            action = AndroidLocalReminderScheduler.ReminderNotificationAction
            putExtra(AndroidLocalReminderScheduler.ReminderProfileIdExtra, profileId)
        }
}

private data class SeededReminderProfiles(
    val appContainer: AppContainer,
    val targetProfileId: String,
)
