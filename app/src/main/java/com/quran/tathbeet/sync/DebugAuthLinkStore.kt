package com.quran.tathbeet.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface DebugAuthLinkStore {
    fun observeLastAuthLink(): Flow<String?>

    suspend fun getLastAuthLink(): String?

    suspend fun setLastAuthLink(link: String?)
}

class InMemoryDebugAuthLinkStore : DebugAuthLinkStore {
    private val lastAuthLink = MutableStateFlow<String?>(null)

    override fun observeLastAuthLink(): Flow<String?> = lastAuthLink.asStateFlow()

    override suspend fun getLastAuthLink(): String? = lastAuthLink.value

    override suspend fun setLastAuthLink(link: String?) {
        lastAuthLink.value = link
    }
}
