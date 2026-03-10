package com.quran.tathbeet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quran.tathbeet.data.local.entity.ScheduleSelectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleSelectionDao {
    @Query("SELECT * FROM schedule_selection WHERE schedule_id = :scheduleId ORDER BY display_order")
    fun observeSelections(scheduleId: String): Flow<List<ScheduleSelectionEntity>>

    @Query("SELECT * FROM schedule_selection WHERE schedule_id = :scheduleId ORDER BY display_order")
    suspend fun getSelections(scheduleId: String): List<ScheduleSelectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ScheduleSelectionEntity>)

    @Query("DELETE FROM schedule_selection WHERE schedule_id = :scheduleId")
    suspend fun deleteForSchedule(scheduleId: String)
}
