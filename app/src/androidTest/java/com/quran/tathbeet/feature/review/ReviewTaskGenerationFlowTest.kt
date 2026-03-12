package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Test

class ReviewTaskGenerationFlowTest : BaseUiFlowTest() {

    @Test
    fun short_selected_surahs_appear_as_separate_tasks() {
        tapNext()
        assertPoolSelectorVisible()
        selectVisibleSurah("الإخلاص")
        selectVisibleSurah("الفلق")
        selectVisibleSurah("الناس")
        tapNext()
        saveSchedule()
        assertReviewVisible()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.review_section_today_title),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "الإخلاص"),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "الفلق"),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "الناس"),
        ).assertIsDisplayed()
    }

    @Test
    fun juz_thirty_shows_short_surahs_as_separate_tasks() {
        tapNext()
        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(30)
        tapNext()
        saveSchedule()
        assertReviewVisible()

        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.quran_surah_title, "النبإ")),
        )
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "النبإ"),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "النازعات"),
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.quran_surah_title, "العاديات")),
        )
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "العاديات"),
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.quran_surah_title, "القارعة")),
        )
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "القارعة"),
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.quran_surah_title, "الناس")),
        )
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "الناس"),
        ).assertIsDisplayed()
        val collapsedLateRubTitle = composeRule.activity.getString(
            R.string.review_unit_title_multi_surah,
            "العاديات",
            "الناس",
        )
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(collapsedLateRubTitle)
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }
}
