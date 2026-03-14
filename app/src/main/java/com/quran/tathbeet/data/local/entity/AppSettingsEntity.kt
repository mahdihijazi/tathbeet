package com.quran.tathbeet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val hasSeenScheduleIntro: Boolean,
    val globalNotificationsEnabled: Boolean,
    val motivationalMessagesEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val forceDarkTheme: Boolean,
)
