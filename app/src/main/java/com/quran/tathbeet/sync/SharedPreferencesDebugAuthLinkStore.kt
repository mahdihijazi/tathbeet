package com.quran.tathbeet.sync

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesDebugAuthLinkStore(
    context: Context,
) : DebugAuthLinkStore {
    private val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
    private val lastAuthLink = MutableStateFlow(preferences.getString(LastAuthLinkKey, null))

    override fun observeLastAuthLink(): Flow<String?> = lastAuthLink.asStateFlow()

    override suspend fun getLastAuthLink(): String? = lastAuthLink.value

    override suspend fun setLastAuthLink(link: String?) {
        lastAuthLink.value = link
        preferences.edit()
            .putString(LastAuthLinkKey, link)
            .apply()
    }

    private companion object {
        const val PreferenceName = "debug_auth_link"
        const val LastAuthLinkKey = "last_auth_link"
    }
}
