package com.quran.tathbeet.domain.repository

import com.quran.tathbeet.domain.model.ReviewDay
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeReviewTimeline(learnerId: String): Flow<List<ReviewDay>>

    fun observeReviewDay(
        learnerId: String,
        assignedForDate: LocalDate,
    ): Flow<ReviewDay?>

    suspend fun ensureAssignmentsForDate(
        learnerId: String,
        assignedForDate: LocalDate,
    ): Boolean

    suspend fun completeAssignment(
        assignmentId: String,
        rating: Int,
    )

    suspend fun updateAssignmentRating(
        assignmentId: String,
        rating: Int,
    )

    suspend fun restartCycle(
        learnerId: String,
        restartDate: LocalDate,
    )
}
