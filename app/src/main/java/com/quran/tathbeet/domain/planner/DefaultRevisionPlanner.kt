package com.quran.tathbeet.domain.planner

import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.PaceOption
import kotlin.math.ceil

class DefaultRevisionPlanner : RevisionPlanner {

    override fun recommendPace(
        segmentCount: Int,
        cycleTarget: CycleTarget,
    ): PaceOption {
        val requiredSegmentsPerDay = ceil(segmentCount.toFloat() / cycleTarget.days)
            .toInt()
            .coerceAtLeast(1)

        return PaceOption.entries.firstOrNull { it.dailySegments >= requiredSegmentsPerDay }
            ?: PaceOption.entries.last()
    }

    override fun resolveEffectiveRubIds(selections: List<CoverageSelection>): List<Int> =
        selections
            .flatMap { selection -> selection.firstRubId..selection.lastRubId }
            .distinct()
            .sorted()
}
