package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewQuranLaunchFlowTest : BaseUiFlowTest() {

    @Test
    fun task_launch_icon_opens_quran_android_directly_when_available() {
        appContainer.recordingQuranExternalLauncher.quranAppInstalled = true

        tapNext()
        assertPoolSelectorVisible()
        selectVisibleSurah("الإخلاص")
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val assignment = firstTodayAssignment()
        scrollReviewListToTag("review-launch-${assignment.id}")
        composeRule.onNodeWithTag("review-launch-${assignment.id}").performClick()

        assertEquals(1, appContainer.recordingQuranExternalLauncher.openReaderRequests.size)
        assertEquals(112, appContainer.recordingQuranExternalLauncher.openReaderRequests.single().startSurahId)
        assertEquals(1, appContainer.recordingQuranExternalLauncher.openReaderRequests.single().startAyah)
        assertTrue(appContainer.recordingQuranExternalLauncher.installRequests.isEmpty())
        assertTrue(appContainer.recordingQuranExternalLauncher.openWebRequests.isEmpty())
    }

    @Test
    fun task_launch_icon_shows_fallback_dialog_when_quran_android_is_unavailable() {
        appContainer.recordingQuranExternalLauncher.quranAppInstalled = false

        tapNext()
        assertPoolSelectorVisible()
        selectVisibleSurah("الإخلاص")
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val assignment = firstTodayAssignment()
        scrollReviewListToTag("review-launch-${assignment.id}")
        composeRule.onNodeWithTag("review-launch-${assignment.id}").performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_external_quran_dialog_title),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_external_quran_install_action),
        ).performClick()

        assertEquals(1, appContainer.recordingQuranExternalLauncher.installRequests.size)
        assertTrue(appContainer.recordingQuranExternalLauncher.openReaderRequests.isEmpty())

        scrollReviewListToTag("review-launch-${assignment.id}")
        composeRule.onNodeWithTag("review-launch-${assignment.id}").performClick()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_external_quran_web_action),
        ).performClick()

        assertEquals(1, appContainer.recordingQuranExternalLauncher.openWebRequests.size)
        assertEquals(112, appContainer.recordingQuranExternalLauncher.openWebRequests.single().startSurahId)
        assertEquals(1, appContainer.recordingQuranExternalLauncher.openWebRequests.single().startAyah)
    }
}
