package com.quran.tathbeet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revision_schedule")
data class RevisionScheduleEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "learner_id")
    val learnerId: String,
    @ColumnInfo(name = "pace_method")
    val paceMethod: String,
    @ColumnInfo(name = "cycle_target_days")
    val cycleTargetDays: Int,
    @ColumnInfo(name = "manual_pace_segments")
    val manualPaceSegments: Int,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
)
