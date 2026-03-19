package com.quran.tathbeet.data.repository

import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.local.entity.AppSettingsEntity
import com.quran.tathbeet.domain.model.AppSettings
import com.quran.tathbeet.domain.model.AppThemeMode
import com.quran.tathbeet.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val database: TathbeetDatabase,
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> =
        database.appSettingsDao()
            .observeSettings()
            .map { entity ->
                AppSettings(
                    hasSeenScheduleIntro = entity?.hasSeenScheduleIntro ?: false,
                    globalNotificationsEnabled = entity?.globalNotificationsEnabled ?: true,
                    motivationalMessagesEnabled = entity?.motivationalMessagesEnabled ?: true,
                    reminderHour = entity?.reminderHour ?: DEFAULT_REMINDER_HOUR,
                    reminderMinute = entity?.reminderMinute ?: DEFAULT_REMINDER_MINUTE,
                    themeMode = entity?.themeMode?.toAppThemeMode()
                        ?: if (entity?.forceDarkTheme == true) {
                            AppThemeMode.Dark
                        } else {
                            AppThemeMode.System
                        },
                )
            }

    override suspend fun markScheduleIntroSeen() {
        updateSettings { entity -> entity.copy(hasSeenScheduleIntro = true) }
    }

    override suspend fun setGlobalNotificationsEnabled(enabled: Boolean) {
        updateSettings { entity -> entity.copy(globalNotificationsEnabled = enabled) }
    }

    override suspend fun setMotivationalMessagesEnabled(enabled: Boolean) {
        updateSettings { entity -> entity.copy(motivationalMessagesEnabled = enabled) }
    }

    override suspend fun setReminderTime(
        hour: Int,
        minute: Int,
    ) {
        updateSettings { entity ->
            entity.copy(
                reminderHour = hour,
                reminderMinute = minute,
            )
        }
    }

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        updateSettings {
            entity ->
            entity.copy(
                themeMode = themeMode.name,
                forceDarkTheme = themeMode == AppThemeMode.Dark,
            )
        }
    }

    private suspend fun updateSettings(
        transform: (AppSettingsEntity) -> AppSettingsEntity,
    ) {
        val current = database.appSettingsDao().getSettings() ?: defaultEntity()
        database.appSettingsDao().upsert(transform(current))
    }

    private fun defaultEntity() = AppSettingsEntity(
        hasSeenScheduleIntro = false,
        globalNotificationsEnabled = true,
        motivationalMessagesEnabled = true,
        reminderHour = DEFAULT_REMINDER_HOUR,
        reminderMinute = DEFAULT_REMINDER_MINUTE,
        themeMode = AppThemeMode.System.name,
        forceDarkTheme = false,
    )

    companion object {
        const val DEFAULT_REMINDER_HOUR = 19
        const val DEFAULT_REMINDER_MINUTE = 0
    }
}

private fun String.toAppThemeMode(): AppThemeMode =
    runCatching { AppThemeMode.valueOf(this) }
        .getOrDefault(AppThemeMode.System)
