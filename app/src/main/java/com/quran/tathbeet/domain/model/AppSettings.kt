package com.quran.tathbeet.domain.model

data class AppSettings(
    val hasSeenScheduleIntro: Boolean,
    val globalNotificationsEnabled: Boolean,
    val motivationalMessagesEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
)
