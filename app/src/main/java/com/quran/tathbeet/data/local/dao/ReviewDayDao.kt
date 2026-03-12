package com.quran.tathbeet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quran.tathbeet.data.local.entity.ReviewDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDayDao {
    @Query(
        """
        SELECT * FROM review_day
        WHERE learner_id = :learnerId
        ORDER BY assigned_for_date
        """,
    )
    fun observeReviewDays(learnerId: String): Flow<List<ReviewDayEntity>>

    @Query(
        """
        SELECT * FROM review_day
        WHERE learner_id = :learnerId AND assigned_for_date = :assignedForDate
        LIMIT 1
        """,
    )
    fun observeReviewDay(
        learnerId: String,
        assignedForDate: String,
    ): Flow<ReviewDayEntity?>

    @Query(
        """
        SELECT * FROM review_day
        WHERE learner_id = :learnerId AND assigned_for_date = :assignedForDate
        LIMIT 1
        """,
    )
    suspend fun getReviewDay(
        learnerId: String,
        assignedForDate: String,
    ): ReviewDayEntity?

    @Query(
        """
        SELECT * FROM review_day
        WHERE learner_id = :learnerId
        ORDER BY assigned_for_date
        LIMIT 1
        """,
    )
    suspend fun getFirstReviewDay(learnerId: String): ReviewDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReviewDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ReviewDayEntity>)

    @Query(
        """
        DELETE FROM review_day
        WHERE learner_id = :learnerId AND assigned_for_date >= :assignedForDate
        """,
    )
    suspend fun deleteOnOrAfter(
        learnerId: String,
        assignedForDate: String,
    )

    @Query(
        """
        DELETE FROM review_day
        WHERE learner_id = :learnerId
          AND id NOT IN (
              SELECT DISTINCT review_day_id
              FROM review_assignment
              WHERE learner_id = :learnerId
          )
        """,
    )
    suspend fun deleteEmptyDays(learnerId: String)

    @Query("DELETE FROM review_day WHERE learner_id = :learnerId")
    suspend fun deleteForLearner(learnerId: String)
}
