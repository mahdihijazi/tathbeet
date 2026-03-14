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

open class AppContainer(
    context: Context,
    open val timeProvider: TimeProvider = SystemTimeProvider(),
    database: TathbeetDatabase? = null,
) {
    private val appContext = context.applicationContext

    open val database: TathbeetDatabase =
        database ?: Room.databaseBuilder(
            appContext,
            TathbeetDatabase::class.java,
            "tathbeet.db",
        )
            .addMigrations(TathbeetDatabase.Migration5To6)
            .fallbackToDestructiveMigration()
            .build()

    open val revisionPlanner: RevisionPlanner = DefaultRevisionPlanner()
    open val quranCatalogRepository: QuranCatalogRepository = AssetQuranCatalogRepository(appContext)
    open val quranExternalLauncher: QuranExternalLauncher = AndroidQuranExternalLauncher(appContext)
    open val profileRepository: ProfileRepository = ProfileRepositoryImpl(database = this.database)
    open val scheduleRepository: ScheduleRepository = ScheduleRepositoryImpl(database = this.database)
    open val settingsRepository: SettingsRepository = SettingsRepositoryImpl(database = this.database)
    open val reviewRepository: ReviewRepository = ReviewRepositoryImpl(
        appContext = appContext,
        database = this.database,
        scheduleRepository = scheduleRepository,
        quranCatalogRepository = quranCatalogRepository,
        timeProvider = timeProvider,
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
