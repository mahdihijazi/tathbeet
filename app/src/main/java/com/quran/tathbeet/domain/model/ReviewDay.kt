package com.quran.tathbeet.domain.model

import java.time.LocalDate

data class ReviewDay(
    val learnerId: String,
    val assignedForDate: LocalDate,
    val completionRate: Int,
    val assignments: List<ReviewAssignment>,
)
