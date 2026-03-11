package com.quran.tathbeet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_assignment")
data class ReviewAssignmentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "review_day_id")
    val reviewDayId: String,
    @ColumnInfo(name = "learner_id")
    val learnerId: String,
    @ColumnInfo(name = "assigned_for_date")
    val assignedForDate: String,
    @ColumnInfo(name = "rub_id")
    val rubId: Int,
    val title: String,
    val detail: String,
    @ColumnInfo(name = "display_order")
    val displayOrder: Int,
    @ColumnInfo(name = "is_rollover")
    val isRollover: Boolean,
    @ColumnInfo(name = "is_done")
    val isDone: Boolean,
    val rating: Int?,
    @ColumnInfo(name = "completed_at")
    val completedAt: String?,
)
