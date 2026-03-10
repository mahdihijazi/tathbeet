package com.quran.tathbeet.domain.planner

import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.SelectionCategory

interface RevisionPlanner {
    fun recommendPace(segmentCount: Int, cycleTarget: CycleTarget): PaceOption

    fun resolveEffectiveRubIds(selections: List<CoverageSelection>): List<Int>

    fun effectiveSegmentCount(selections: List<CoverageSelection>): Int =
        resolveEffectiveRubIds(selections).size
}

data class CoverageSelection(
    val category: SelectionCategory,
    val itemId: Int,
    val firstRubId: Int,
    val lastRubId: Int,
)
