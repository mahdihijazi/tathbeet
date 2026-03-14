package com.quran.tathbeet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.app.AndroidLocalReminderScheduler
import com.quran.tathbeet.ui.TathbeetApp

class MainActivity : ComponentActivity() {
    private var notificationTargetProfileId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationTargetProfileId = intent.notificationTargetProfileId()
        enableEdgeToEdge()
        setContent {
            val appContainer = remember { AppContainer(applicationContext) }
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
    }
}

private fun Intent?.notificationTargetProfileId(): String? =
    takeIf { intent -> intent?.action == AndroidLocalReminderScheduler.ReminderNotificationAction }
        ?.getStringExtra(AndroidLocalReminderScheduler.ReminderProfileIdExtra)
