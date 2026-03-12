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
    @ColumnInfo(name = "task_key")
    val taskKey: String,
    @ColumnInfo(name = "rub_id")
    val rubId: Int,
    @ColumnInfo(name = "start_surah_id")
    val startSurahId: Int?,
    @ColumnInfo(name = "start_ayah")
    val startAyah: Int?,
    @ColumnInfo(name = "end_surah_id")
    val endSurahId: Int?,
    @ColumnInfo(name = "end_ayah")
    val endAyah: Int?,
    val title: String,
    val detail: String,
    val weight: Double,
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
