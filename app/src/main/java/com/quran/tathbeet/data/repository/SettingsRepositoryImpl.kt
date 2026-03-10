package com.quran.tathbeet.data.repository

import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.local.entity.AppSettingsEntity
import com.quran.tathbeet.domain.model.AppSettings
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
                )
            }

    override suspend fun markScheduleIntroSeen() {
        database.appSettingsDao().upsert(
            AppSettingsEntity(
                hasSeenScheduleIntro = true,
            ),
        )
    }
}
