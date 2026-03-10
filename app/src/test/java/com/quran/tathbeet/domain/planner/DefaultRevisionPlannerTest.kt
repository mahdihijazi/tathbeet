package com.quran.tathbeet.domain.planner

import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.SelectionCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultRevisionPlannerTest {

    private val planner = DefaultRevisionPlanner()

    @Test
    fun `recommend pace rounds up to next practical milestone`() {
        val recommended = planner.recommendPace(
            segmentCount = 35,
            cycleTarget = CycleTarget.OneMonth,
        )

        assertEquals(PaceOption.OneHizb, recommended)
    }

    @Test
    fun `effective coverage drops fully contained inner selection`() {
        val effectiveRubIds = planner.resolveEffectiveRubIds(
            listOf(
                CoverageSelection(
                    category = SelectionCategory.Surahs,
                    itemId = 2,
                    firstRubId = 1,
                    lastRubId = 20,
                ),
                CoverageSelection(
                    category = SelectionCategory.Rub,
                    itemId = 5,
                    firstRubId = 5,
                    lastRubId = 5,
                ),
            ),
        )

        assertEquals((1..20).toList(), effectiveRubIds)
    }

    @Test
    fun `effective coverage counts partial overlap once`() {
        val effectiveRubIds = planner.resolveEffectiveRubIds(
            listOf(
                CoverageSelection(
                    category = SelectionCategory.Surahs,
                    itemId = 2,
                    firstRubId = 1,
                    lastRubId = 20,
                ),
                CoverageSelection(
                    category = SelectionCategory.Rub,
                    itemId = 21,
                    firstRubId = 20,
                    lastRubId = 21,
                ),
            ),
        )

        assertEquals((1..21).toList(), effectiveRubIds)
    }
}
