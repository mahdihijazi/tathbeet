package com.quran.tathbeet.domain.model

data class RevisionSchedule(
    val id: String,
    val learnerId: String,
    val paceMethod: PaceMethod,
    val cycleTarget: CycleTarget,
    val manualPace: PaceOption,
    val selections: List<ScheduleSelection>,
)
