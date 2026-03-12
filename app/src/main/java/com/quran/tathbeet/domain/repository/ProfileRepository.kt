package com.quran.tathbeet.domain.repository

import com.quran.tathbeet.domain.model.LearnerAccount
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeAccounts(): Flow<List<LearnerAccount>>

    fun observeActiveAccount(): Flow<LearnerAccount?>

    suspend fun ensureDefaultAccount(name: String)

    suspend fun createProfile(name: String): LearnerAccount

    suspend fun updateAccountName(accountId: String, name: String)

    suspend fun updateNotificationsEnabled(
        accountId: String,
        enabled: Boolean,
    )

    suspend fun deleteProfile(accountId: String)

    suspend fun setActiveAccount(accountId: String)
}
