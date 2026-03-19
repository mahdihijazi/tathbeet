package com.quran.tathbeet.app

import android.content.Context
import androidx.room.Room
import com.quran.tathbeet.core.time.SystemTimeProvider
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.repository.ProfileRepositoryImpl
import com.quran.tathbeet.data.repository.ReviewRepositoryImpl
import com.quran.tathbeet.data.repository.ScheduleRepositoryImpl
import com.quran.tathbeet.data.repository.SettingsRepositoryImpl
import com.quran.tathbeet.domain.planner.DefaultRevisionPlanner
import com.quran.tathbeet.domain.planner.RevisionPlanner
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.QuranCatalogRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.domain.repository.SettingsRepository
import com.quran.tathbeet.quran.AssetQuranCatalogRepository
import com.quran.tathbeet.sync.CloudBackend
import com.quran.tathbeet.sync.CloudBackendFactory
import com.quran.tathbeet.sync.AuthSessionRepository
import com.quran.tathbeet.sync.AuthSessionRepositoryImpl
import com.quran.tathbeet.sync.CloudSyncStore
import com.quran.tathbeet.sync.CloudSyncManager
import com.quran.tathbeet.sync.CloudSyncRealtimeCoordinator
import com.quran.tathbeet.sync.DebugAuthLinkStore
import com.quran.tathbeet.sync.DefaultCloudBackendFactory
import com.quran.tathbeet.sync.ProfileSyncCoordinator
import com.quran.tathbeet.sync.RepositoryBackedSyncReviewRepository
import com.quran.tathbeet.sync.RepositoryBackedSyncScheduleRepository
import com.quran.tathbeet.sync.RoomCloudProfileMirror
import com.quran.tathbeet.sync.RoomSyncProfileRepository
import com.quran.tathbeet.sync.SharedPreferencesPendingEmailStore
import com.quran.tathbeet.sync.SharedPreferencesDebugAuthLinkStore
import com.quran.tathbeet.sync.SyncStartupCoordinator
import com.quran.tathbeet.sync.SyncingProfileRepository
import com.quran.tathbeet.sync.SyncingReviewRepository
import com.quran.tathbeet.sync.SyncingScheduleRepository

open class AppContainer(
    context: Context,
    open val timeProvider: TimeProvider = SystemTimeProvider(),
    private val cloudBackendFactory: CloudBackendFactory = DefaultCloudBackendFactory,
    database: TathbeetDatabase? = null,
) {
    private val appContext = context.applicationContext

    open val database: TathbeetDatabase =
        database ?: Room.databaseBuilder(
            appContext,
            TathbeetDatabase::class.java,
            "tathbeet.db",
        )
            .addMigrations(
                TathbeetDatabase.Migration5To6,
                TathbeetDatabase.Migration6To7,
                TathbeetDatabase.Migration7To8,
            )
            .fallbackToDestructiveMigration()
            .build()

    open val revisionPlanner: RevisionPlanner = DefaultRevisionPlanner()
    open val quranCatalogRepository: QuranCatalogRepository = AssetQuranCatalogRepository(appContext)
    open val quranExternalLauncher: QuranExternalLauncher = AndroidQuranExternalLauncher(appContext)
    open val cloudBackend: CloudBackend = cloudBackendFactory.create(appContext)
    private val localProfileRepository = ProfileRepositoryImpl(database = this.database)
    private val localScheduleRepository = ScheduleRepositoryImpl(database = this.database)
    private val localSettingsRepository = SettingsRepositoryImpl(database = this.database)
    private val localReviewRepository = ReviewRepositoryImpl(
        appContext = appContext,
        database = this.database,
        scheduleRepository = localScheduleRepository,
        quranCatalogRepository = quranCatalogRepository,
        timeProvider = timeProvider,
    )
    private val roomCloudProfileMirror = RoomCloudProfileMirror(database = this.database)
    open val authSessionRepository: AuthSessionRepository = AuthSessionRepositoryImpl(
        authClient = cloudBackend.authClient,
        pendingEmailStore = SharedPreferencesPendingEmailStore(appContext),
    )
    open val debugAuthLinkStore: DebugAuthLinkStore = SharedPreferencesDebugAuthLinkStore(appContext)
    open val cloudSyncStore: CloudSyncStore = cloudBackend.cloudSyncStore
    open val profileSyncCoordinator: ProfileSyncCoordinator = ProfileSyncCoordinator(
        profileRepository = RoomSyncProfileRepository(database = this.database),
        scheduleRepository = RepositoryBackedSyncScheduleRepository(localScheduleRepository),
        reviewRepository = RepositoryBackedSyncReviewRepository(localReviewRepository),
        cloudSyncStore = cloudSyncStore,
    )
    open val cloudSyncManager: CloudSyncManager = CloudSyncManager(
        authSessionRepository = authSessionRepository,
        profileSyncCoordinator = profileSyncCoordinator,
        profileRepository = RoomSyncProfileRepository(database = this.database),
        cloudSyncStore = cloudSyncStore,
        localMirror = roomCloudProfileMirror,
    )
    open val profileRepository: ProfileRepository = SyncingProfileRepository(
        delegate = localProfileRepository,
        syncManager = cloudSyncManager,
    )
    open val scheduleRepository: ScheduleRepository = SyncingScheduleRepository(
        delegate = localScheduleRepository,
        syncManager = cloudSyncManager,
    )
    open val settingsRepository: SettingsRepository = localSettingsRepository
    open val reviewRepository: ReviewRepository = SyncingReviewRepository(
        delegate = localReviewRepository,
        syncManager = cloudSyncManager,
        database = this.database,
    )
    open val syncStartupCoordinator: SyncStartupCoordinator = SyncStartupCoordinator(
        authSessionRepository = authSessionRepository,
        cloudSyncManager = cloudSyncManager,
    )
    open val cloudSyncRealtimeCoordinator: CloudSyncRealtimeCoordinator = CloudSyncRealtimeCoordinator(
        authSessionRepository = authSessionRepository,
        profileRepository = profileRepository,
        cloudSyncStore = cloudSyncStore,
        cloudSyncManager = cloudSyncManager,
    )
    open val localReminderScheduler: LocalReminderScheduler = AndroidLocalReminderScheduler(
        context = appContext,
        timeProvider = timeProvider,
        profileRepository = profileRepository,
        scheduleRepository = scheduleRepository,
        settingsRepository = settingsRepository,
        reviewRepository = reviewRepository,
    )
    open val debugNotificationController: DebugNotificationController =
        (localReminderScheduler as? DebugNotificationController) ?: NoOpDebugNotificationController
}
