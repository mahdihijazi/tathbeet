package com.quran.tathbeet.app

interface LocalReminderScheduler {
    suspend fun syncSchedules()

    suspend fun cancelProfile(profileId: String)

    suspend fun handleReminder(profileId: String)
}
