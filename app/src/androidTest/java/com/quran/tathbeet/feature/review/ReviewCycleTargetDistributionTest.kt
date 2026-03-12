package com.quran.tathbeet.feature.review

import com.quran.tathbeet.test.BaseUiFlowTest
import com.quran.tathbeet.ui.model.CycleTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewCycleTargetDistributionTest : BaseUiFlowTest() {

    @Test
    fun juz_thirty_with_one_week_target_spreads_work_across_the_whole_week() {
        tapNext()
        assertPoolSelectorVisible()
        openJuzTab()
        selectVisibleJuz(30)
        tapNext()
        assertScheduleDoseVisible()
        selectCycleTarget(CycleTarget.OneWeek)
        saveSchedule()
        assertReviewVisible()

        val scheduledDays = awaitReviewTimeline()
            .filter { day -> !day.assignedForDate.isBefore(todayDate()) }

        assertEquals(7, scheduledDays.size)
        assertTrue(scheduledDays.all { day -> day.assignments.isNotEmpty() })
        assertEquals(todayDate(), scheduledDays.first().assignedForDate)
        assertEquals(todayDate().plusDays(6), scheduledDays.last().assignedForDate)
    }
}
