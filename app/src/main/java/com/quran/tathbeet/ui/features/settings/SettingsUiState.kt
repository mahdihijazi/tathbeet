package com.quran.tathbeet.ui.features.settings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val globalNotificationsEnabled: Boolean = true,
    val motivationalMessagesEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    val reminderOptions: List<ReminderTimeOptionUiState> = emptyList(),
    val profiles: List<SettingsProfileUiState> = emptyList(),
)

data class ReminderTimeOptionUiState(
    val hour: Int,
    val minute: Int,
    val label: String,
)

data class SettingsProfileUiState(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val isSelfProfile: Boolean,
    val notificationsEnabled: Boolean,
    val hasSchedule: Boolean,
)
