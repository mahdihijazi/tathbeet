package com.quran.tathbeet.domain.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class ReviewAssignment(
    val id: String,
    val learnerId: String,
    val assignedForDate: LocalDate,
    val taskKey: String,
    val title: String,
    val detail: String,
    val rubId: Int,
    val weight: Double,
    val displayOrder: Int,
    val isRollover: Boolean,
    val isDone: Boolean,
    val rating: Int?,
    val completedAt: ZonedDateTime?,
)
