package com.quran.tathbeet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quran.tathbeet.data.local.entity.ReviewAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewAssignmentDao {
    @Query(
        """
        SELECT * FROM review_assignment
        WHERE learner_id = :learnerId AND assigned_for_date = :assignedForDate
        ORDER BY display_order
        """,
    )
    fun observeAssignments(
        learnerId: String,
        assignedForDate: String,
    ): Flow<List<ReviewAssignmentEntity>>

    @Query(
        """
        SELECT * FROM review_assignment
        WHERE learner_id = :learnerId AND assigned_for_date = :assignedForDate
        ORDER BY display_order
        """,
    )
    suspend fun getAssignments(
        learnerId: String,
        assignedForDate: String,
    ): List<ReviewAssignmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ReviewAssignmentEntity>)

    @Query("UPDATE review_assignment SET is_done = CASE WHEN is_done = 1 THEN 0 ELSE 1 END, completed_at = :completedAt WHERE id = :assignmentId")
    suspend fun toggleCompletion(
        assignmentId: String,
        completedAt: String?,
    )

    @Query("SELECT COUNT(*) FROM review_assignment WHERE review_day_id = :reviewDayId")
    suspend fun countAssignments(reviewDayId: String): Int

    @Query("SELECT COUNT(*) FROM review_assignment WHERE review_day_id = :reviewDayId AND is_done = 1")
    suspend fun countCompletedAssignments(reviewDayId: String): Int
}
