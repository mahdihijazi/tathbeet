package com.quran.tathbeet.domain.repository

import com.quran.tathbeet.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>

    suspend fun markScheduleIntroSeen()

    suspend fun setGlobalNotificationsEnabled(enabled: Boolean)

    suspend fun setMotivationalMessagesEnabled(enabled: Boolean)

    suspend fun setReminderTime(
        hour: Int,
        minute: Int,
    )

    suspend fun setForceDarkTheme(enabled: Boolean)
}
