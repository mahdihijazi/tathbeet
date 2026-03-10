package com.quran.tathbeet.domain.repository

import com.quran.tathbeet.domain.model.ReviewDay
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeReviewDay(
        learnerId: String,
        assignedForDate: LocalDate,
    ): Flow<ReviewDay?>

    suspend fun ensureAssignmentsForDate(
        learnerId: String,
        assignedForDate: LocalDate,
    )

    suspend fun toggleAssignmentCompletion(assignmentId: String)
}
