package com.quran.tathbeet.feature.schedule

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.R
import com.quran.tathbeet.test.BaseUiFlowTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertFalse

class ScheduleEditingFlowTest : BaseUiFlowTest() {

    @Test
    fun edit_plan_from_review_reopens_wizard_and_saves_without_crashing() {
        completeOnboardingWithJuzOne()
        assertReviewVisible()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).performClick()

        assertPoolSelectorVisible()

        tapNext()
        assertScheduleDoseVisible()

        saveSchedule()
        assertReviewVisible()

        runBlocking {
            val account = appContainer.profileRepository.observeActiveAccount()
                .filterNotNull()
                .first()
            val schedule = appContainer.scheduleRepository.observeActiveSchedule(account.id)
                .filterNotNull()
                .first()

            assert(schedule.selections.isNotEmpty())
        }
    }

    @Test
    fun edit_plan_opens_directly_on_pool_selector_without_onboarding_header() {
        completeOnboardingWithJuzOne()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).performClick()

        assertPoolSelectorVisible()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.wizard_step_counter, 2, 3),
            ).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun editing_plan_rebuilds_review_timeline_from_new_pool() {
        tapNext()
        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(30)
        tapNext()
        saveSchedule()
        assertReviewVisible()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).performClick()

        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(29)
        openRubTab()
        selectVisibleRub(1)
        openSurahTab()
        selectVisibleSurah("الصف")
        selectVisibleSurah("الجمعة")
        selectVisibleSurah("المنافقون")
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val timeline = awaitReviewTimeline()
        val allAssignments = timeline.flatMap { it.assignments }

        assertEquals(1, timeline.first().assignments.first().rubId)
        assertTrue(allAssignments.any { it.taskKey == "rub-1" })
        assertTrue(allAssignments.any { it.taskKey == "surah-61" })
        assertTrue(allAssignments.any { it.rubId in 229..236 })
    }

    @Test
    fun editing_juz_thirty_plan_adds_new_pool_items_and_keeps_late_short_surahs_friendly() {
        tapNext()
        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(30)
        tapNext()
        saveSchedule()
        assertReviewVisible()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).performClick()

        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(29)
        openRubTab()
        selectVisibleRub(1)
        openSurahTab()
        selectVisibleSurah("الصف")
        selectVisibleSurah("الجمعة")
        selectVisibleSurah("المنافقون")
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val allAssignments = awaitReviewTimeline().flatMap { it.assignments }

        assertTrue(allAssignments.any { it.taskKey == "rub-1" })
        assertTrue(allAssignments.any { it.taskKey == "surah-61" })
        assertTrue(allAssignments.any { it.taskKey == "surah-62" })
        assertTrue(allAssignments.any { it.taskKey == "surah-63" })
        assertTrue(allAssignments.any { it.rubId in 225..232 })
        assertTrue(allAssignments.any {
            it.title == composeRule.activity.getString(R.string.quran_surah_title, "العاديات")
        })
        assertFalse(allAssignments.any {
            it.title == composeRule.activity.getString(
                R.string.quran_range_single_surah,
                "العاديات",
                1,
                8,
            )
        })
        assertFalse(allAssignments.any {
            it.title == composeRule.activity.getString(
                R.string.quran_range_single_surah,
                "العاديات",
                9,
                11,
            )
        })
    }

    @Test
    fun editing_plan_preserves_done_and_overdue_work_and_rebuilds_remaining_cycle() {
        tapNext()
        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(30)
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val learnerId = activeAccountId()
        val yesterday = todayDate().minusDays(1)
        runBlocking {
            appContainer.reviewRepository.ensureAssignmentsForDate(
                learnerId = learnerId,
                assignedForDate = yesterday,
            )
        }

        val seededYesterday = awaitReviewDay(yesterday)
        val completedYesterday = seededYesterday.assignments.first()
        val overdueYesterday = seededYesterday.assignments.last()
        val completedToday = awaitTodayReviewDay().assignments.first()
        val oldFutureTaskKey = awaitReviewTimeline()
            .first { day -> day.assignedForDate.isAfter(todayDate()) }
            .assignments
            .first()
            .taskKey

        runBlocking {
            appContainer.reviewRepository.completeAssignment(completedYesterday.id, 4)
            appContainer.reviewRepository.completeAssignment(completedToday.id, 5)
        }

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).performClick()

        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(30)
        selectVisibleJuz(29)
        tapNext()
        saveSchedule()
        assertReviewVisible()

        val refreshedTimeline = awaitReviewTimeline()
        val refreshedYesterday = refreshedTimeline.first { it.assignedForDate == yesterday }
        val refreshedToday = refreshedTimeline.first { it.assignedForDate == todayDate() }
        val refreshedFutureAssignments = refreshedTimeline
            .filter { day -> !day.assignedForDate.isBefore(todayDate()) }
            .flatMap { day -> day.assignments }

        assertTrue(refreshedYesterday.assignments.any { assignment ->
            assignment.id == completedYesterday.id && assignment.isDone && assignment.rating == 4
        })
        assertTrue(refreshedYesterday.assignments.any { assignment ->
            assignment.id == overdueYesterday.id && !assignment.isDone
        })
        assertTrue(refreshedToday.assignments.any { assignment ->
            assignment.id == completedToday.id && assignment.isDone && assignment.rating == 5
        })
        assertTrue(refreshedFutureAssignments.any { assignment -> assignment.rubId in 229..236 })
        assertTrue(refreshedFutureAssignments.none { assignment ->
            !assignment.isDone && assignment.taskKey == oldFutureTaskKey
        })
    }
}
