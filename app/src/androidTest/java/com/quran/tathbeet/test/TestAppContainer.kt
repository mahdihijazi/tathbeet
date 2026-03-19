package com.quran.tathbeet.test

import android.content.Context
import androidx.room.Room
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.app.DebugNotificationController
import com.quran.tathbeet.app.LocalReminderScheduler
import com.quran.tathbeet.app.ReminderNotificationDebugScenario
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.sync.AuthUser
import com.quran.tathbeet.sync.CloudBackend
import com.quran.tathbeet.sync.CloudBackendFactory
import com.quran.tathbeet.sync.DisabledCloudSyncStore
import com.quran.tathbeet.sync.EmailLinkAuthClient
import com.quran.tathbeet.sync.InMemoryDebugAuthLinkStore
import com.quran.tathbeet.sync.DebugAuthLinkStore
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestAppContainer(
    context: Context,
) : AppContainer(
    context = context,
    timeProvider = object : TimeProvider {
        override fun today(): LocalDate = LocalDate.of(2026, 3, 10)

        override fun now(): ZonedDateTime =
            ZonedDateTime.of(2026, 3, 10, 8, 0, 0, 0, ZoneId.of("UTC"))

        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    },
    database = Room.inMemoryDatabaseBuilder(context, TathbeetDatabase::class.java)
        .allowMainThreadQueries()
        .build(),
    cloudBackendFactory = testCloudBackendFactory,
) {
    val recordingQuranExternalLauncher = RecordingQuranExternalLauncher()
    val recordingReminderScheduler = RecordingLocalReminderScheduler(
        enabledProfilesProvider = {
            val settings = settingsRepository.observeSettings().first()
            if (!settings.globalNotificationsEnabled) {
                emptyList()
            } else {
                profileRepository.observeAccounts().first()
                    .filter { account ->
                        account.notificationsEnabled &&
                            scheduleRepository.observeActiveSchedule(account.id).first() != null
                    }
                    .map { account -> account.id }
            }
        },
    )
    val recordingDebugNotificationController = RecordingDebugNotificationController()
    val recordingDebugAuthLinkStore = InMemoryDebugAuthLinkStore()

    override val quranExternalLauncher = recordingQuranExternalLauncher
    override val localReminderScheduler: LocalReminderScheduler = recordingReminderScheduler
    override val debugNotificationController: DebugNotificationController =
        recordingDebugNotificationController
    override val debugAuthLinkStore: DebugAuthLinkStore = recordingDebugAuthLinkStore

    private companion object {
        val testCloudBackendFactory = CloudBackendFactory {
            CloudBackend(
                authClient = RecordingEmailLinkAuthClient(),
                cloudSyncStore = DisabledCloudSyncStore,
            )
        }
    }
}

private class RecordingEmailLinkAuthClient : EmailLinkAuthClient {
    private val currentUser = MutableStateFlow<AuthUser?>(null)

    override val isConfigured: Boolean = true

    override fun observeCurrentUser(): Flow<AuthUser?> = currentUser.asStateFlow()

    override suspend fun sendSignInLink(email: String) {
        Unit
    }

    override fun isSignInLink(link: String): Boolean = link.startsWith("https://")

    override suspend fun signInWithEmailLink(
        email: String,
        link: String,
    ): AuthUser {
        val user = AuthUser(
            uid = "uid-${email.substringBefore('@')}",
            email = email,
        )
        currentUser.value = user
        return user
    }

    override suspend fun signOut() {
        currentUser.value = null
    }
}

class RecordingLocalReminderScheduler(
    private val enabledProfilesProvider: suspend () -> List<String>,
) : LocalReminderScheduler {
    val scheduledProfiles = mutableListOf<List<String>>()
    val cancelledProfiles = mutableListOf<String>()

    override suspend fun syncSchedules() {
        scheduledProfiles += enabledProfilesProvider()
    }

    override suspend fun cancelProfile(profileId: String) {
        cancelledProfiles += profileId
    }

    override suspend fun handleReminder(profileId: String) = Unit
}

class RecordingDebugNotificationController : DebugNotificationController {
    val triggeredScenarios = mutableListOf<ReminderNotificationDebugScenario>()

    override val reminderScenarios: List<ReminderNotificationDebugScenario> =
        ReminderNotificationDebugScenario.entries

    override suspend fun triggerReminderNotification(scenario: ReminderNotificationDebugScenario) {
        triggeredScenarios += scenario
    }
}
