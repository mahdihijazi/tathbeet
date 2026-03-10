package com.quran.tathbeet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_day")
data class ReviewDayEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "learner_id")
    val learnerId: String,
    @ColumnInfo(name = "assigned_for_date")
    val assignedForDate: String,
    @ColumnInfo(name = "completion_rate")
    val completionRate: Int,
)
