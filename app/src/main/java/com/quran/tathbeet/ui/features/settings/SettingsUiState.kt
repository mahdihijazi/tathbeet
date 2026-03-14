package com.quran.tathbeet.ui.features.settings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val forceDarkTheme: Boolean = false,
    val globalNotificationsEnabled: Boolean = true,
    val motivationalMessagesEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val profiles: List<SettingsProfileUiState> = emptyList(),
)

data class SettingsProfileUiState(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val isSelfProfile: Boolean,
    val notificationsEnabled: Boolean,
    val hasSchedule: Boolean,
)
