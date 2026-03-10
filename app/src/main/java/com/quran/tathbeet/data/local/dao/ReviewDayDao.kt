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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReviewDayEntity)
}
