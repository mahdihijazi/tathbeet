package com.quran.tathbeet.domain.model

data class AppSettings(
    val hasSeenScheduleIntro: Boolean = false,
    val globalNotificationsEnabled: Boolean = true,
    val motivationalMessagesEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val forceDarkTheme: Boolean = false,
)
