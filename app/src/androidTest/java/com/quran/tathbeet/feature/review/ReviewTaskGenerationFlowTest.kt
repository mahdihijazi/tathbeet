package com.quran.tathbeet.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.quran.tathbeet.R
import com.quran.tathbeet.data.local.entity.ReviewAssignmentEntity
import com.quran.tathbeet.data.local.entity.ReviewDayEntity
import com.quran.tathbeet.test.BaseUiFlowTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
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

    @Test
    fun opening_review_refreshes_stale_future_juz_thirty_assignments() {
        tapNext()
        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(30)
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val learnerId = activeAccountId()
        val tomorrow = todayDate().plusDays(1)
        val tomorrowKey = tomorrow.toString()
        runBlocking {
            appContainer.database.reviewAssignmentDao().deletePendingAssignmentsOnOrAfter(
                learnerId = learnerId,
                assignedForDate = tomorrowKey,
            )
            appContainer.database.reviewDayDao().deleteOnOrAfter(
                learnerId = learnerId,
                assignedForDate = tomorrowKey,
            )
            appContainer.database.reviewDayDao().upsertAll(
                listOf(
                    ReviewDayEntity(
                        id = "$learnerId-$tomorrowKey",
                        learnerId = learnerId,
                        assignedForDate = tomorrowKey,
                        completionRate = 0,
                    ),
                ),
            )
            appContainer.database.reviewAssignmentDao().insertAll(
                listOf(
                    ReviewAssignmentEntity(
                        id = "$tomorrowKey-segment-239",
                        reviewDayId = "$learnerId-$tomorrowKey",
                        learnerId = learnerId,
                        assignedForDate = tomorrowKey,
                        taskKey = "segment-239-100-1-100-8",
                        rubId = 239,
                        title = composeRule.activity.getString(
                            R.string.quran_range_single_surah,
                            "العاديات",
                            1,
                            8,
                        ),
                        detail = "",
                        weight = 0.5,
                        displayOrder = 0,
                        isRollover = false,
                        isDone = false,
                        rating = null,
                        completedAt = null,
                    ),
                    ReviewAssignmentEntity(
                        id = "$tomorrowKey-segment-240",
                        reviewDayId = "$learnerId-$tomorrowKey",
                        learnerId = learnerId,
                        assignedForDate = tomorrowKey,
                        taskKey = "segment-240-100-9-100-11",
                        rubId = 240,
                        title = composeRule.activity.getString(
                            R.string.quran_range_single_surah,
                            "العاديات",
                            9,
                            11,
                        ),
                        detail = "",
                        weight = 0.2,
                        displayOrder = 1,
                        isRollover = false,
                        isDone = false,
                        rating = null,
                        completedAt = null,
                    ),
                ),
            )
        }

        runBlocking {
            appContainer.reviewRepository.ensureAssignmentsForDate(
                learnerId = learnerId,
                assignedForDate = todayDate(),
            )
        }

        assertReviewVisible()

        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasText(composeRule.activity.getString(R.string.quran_surah_title, "العاديات")),
        )
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.quran_surah_title, "العاديات"),
        ).assertIsDisplayed()

        val allAssignments = awaitReviewTimeline().flatMap { it.assignments }
        assertTrue(
            allAssignments.any { assignment ->
                assignment.title == composeRule.activity.getString(R.string.quran_surah_title, "العاديات")
            },
        )

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(
                    R.string.quran_range_single_surah,
                    "العاديات",
                    1,
                    8,
                ),
            ).fetchSemanticsNodes().isEmpty() &&
                composeRule.onAllNodesWithText(
                    composeRule.activity.getString(
                        R.string.quran_range_single_surah,
                        "العاديات",
                        9,
                        11,
                    ),
                ).fetchSemanticsNodes().isEmpty()
        }
    }
}
