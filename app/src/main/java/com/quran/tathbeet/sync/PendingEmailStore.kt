package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PendingEmailStore {
    fun observePendingEmail(): Flow<String?>

    suspend fun getPendingEmail(): String?

    suspend fun setPendingEmail(email: String?)
}

class InMemoryPendingEmailStore : PendingEmailStore {
    private val pendingEmail = MutableStateFlow<String?>(null)

    override fun observePendingEmail(): Flow<String?> = pendingEmail.asStateFlow()

    override suspend fun getPendingEmail(): String? = pendingEmail.value

    override suspend fun setPendingEmail(email: String?) {
        pendingEmail.value = email
    }
}
