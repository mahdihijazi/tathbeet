package com.quran.tathbeet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quran.tathbeet.data.local.entity.RevisionScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionScheduleDao {
    @Query(
        """
        SELECT * FROM revision_schedule
        WHERE learner_id = :learnerId AND is_active = 1
        LIMIT 1
        """,
    )
    fun observeActiveSchedule(learnerId: String): Flow<RevisionScheduleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RevisionScheduleEntity)

    @Query("UPDATE revision_schedule SET is_active = 0 WHERE learner_id = :learnerId")
    suspend fun clearActiveSchedule(learnerId: String)

    @Query("DELETE FROM revision_schedule WHERE learner_id = :learnerId")
    suspend fun deleteForLearner(learnerId: String)
}
