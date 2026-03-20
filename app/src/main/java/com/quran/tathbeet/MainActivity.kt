package com.quran.tathbeet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.quran.tathbeet.BuildConfig
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.app.AndroidLocalReminderScheduler
import com.quran.tathbeet.sync.resolveAuthLink
import com.quran.tathbeet.ui.TathbeetApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var notificationTargetProfileId by mutableStateOf<String?>(null)
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationTargetProfileId = intent.notificationTargetProfileId()
        appContainer = AppContainer(applicationContext)
        appContainer.cloudSyncRealtimeCoordinator.bind(lifecycleScope)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            TathbeetApp(
                appContainer = appContainer,
                notificationTargetProfileId = notificationTargetProfileId,
                onNotificationTargetHandled = { notificationTargetProfileId = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationTargetProfileId = intent.notificationTargetProfileId()
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        lifecycleScope.launch {
            val authLink = intent.authLink()
            storeDebugAuthLink(authLink)
            appContainer.syncStartupCoordinator.handleIncomingAuthLink(authLink)
        }
    }

    private suspend fun storeDebugAuthLink(authLink: String?) {
        if (BuildConfig.DEBUG && !authLink.isNullOrBlank()) {
            appContainer.debugAuthLinkStore.setLastAuthLink(authLink)
        }
    }
}

private fun Intent?.notificationTargetProfileId(): String? =
    takeIf { intent -> intent?.action == AndroidLocalReminderScheduler.ReminderNotificationAction }
        ?.getStringExtra(AndroidLocalReminderScheduler.ReminderProfileIdExtra)

private fun Intent?.authLink(): String? =
    takeIf { intent -> intent?.action == Intent.ACTION_VIEW }
        ?.data
        ?.toString()
        ?.let(::resolveAuthLink)
