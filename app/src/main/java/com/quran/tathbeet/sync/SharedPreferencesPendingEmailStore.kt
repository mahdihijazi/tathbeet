package com.quran.tathbeet.sync

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesPendingEmailStore(
    context: Context,
) : PendingEmailStore {
    private val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
    private val pendingEmail = MutableStateFlow(preferences.getString(PendingEmailKey, null))

    override fun observePendingEmail(): Flow<String?> = pendingEmail.asStateFlow()

    override suspend fun getPendingEmail(): String? = pendingEmail.value

    override suspend fun setPendingEmail(email: String?) {
        pendingEmail.value = email
        preferences.edit()
            .putString(PendingEmailKey, email)
            .apply()
    }

    private companion object {
        const val PreferenceName = "auth_session"
        const val PendingEmailKey = "pending_email"
    }
}
