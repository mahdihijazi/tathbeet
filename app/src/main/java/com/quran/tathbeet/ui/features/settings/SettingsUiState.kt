package com.quran.tathbeet.ui.features.settings

import com.quran.tathbeet.domain.model.AppThemeMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.System,
    val globalNotificationsEnabled: Boolean = true,
    val motivationalMessagesEnabled: Boolean = true,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
)
