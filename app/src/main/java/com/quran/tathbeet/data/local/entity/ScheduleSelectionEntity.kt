package com.quran.tathbeet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_selection")
data class ScheduleSelectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "schedule_id")
    val scheduleId: String,
    val category: String,
    @ColumnInfo(name = "item_id")
    val itemId: Int,
    @ColumnInfo(name = "display_order")
    val displayOrder: Int,
)
