package com.quran.tathbeet.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.quran.tathbeet.R
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.ui.features.progress.ProgressViewModelFactory
import com.quran.tathbeet.ui.features.profiles.ProfilesViewModelFactory
import com.quran.tathbeet.ui.features.schedule.ScheduleWizardViewModelFactory
import com.quran.tathbeet.ui.features.settings.SettingsViewModelFactory
import com.quran.tathbeet.ui.features.shared.SharedProfileViewModelFactory
import com.quran.tathbeet.ui.model.AppUiState
import com.quran.tathbeet.ui.model.AppDestination
import com.quran.tathbeet.ui.model.Guardian
import com.quran.tathbeet.ui.model.TextSpec
import com.quran.tathbeet.ui.model.activeProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal const val RouteLaunch = "launch"
internal const val RouteScheduleGraph = "schedule_graph"
internal const val RouteScheduleIntro = "schedule_intro"
internal const val RoutePoolSelector = "pool_selector"
internal const val RouteScheduleDose = "schedule_dose"
internal const val RouteReview = "review"
internal const val RouteProfiles = "profiles"
internal const val RouteProgress = "progress"
internal const val RouteSettings = "settings"
internal const val RouteDebug = "debug"
internal const val RouteDebugLocalNotifications = "debug_local_notifications"
internal const val RouteDebugUiCatalog = "debug_ui_catalog"
internal const val RouteShared = "shared"
internal const val RouteSharedProfileIdArg = "profileId"
internal const val RouteSharedProfile = "$RouteShared/{$RouteSharedProfileIdArg}"

internal fun sharedProfileRoute(profileId: String): String = "$RouteShared/$profileId"

@Composable
internal fun LaunchRoute(
    appContainer: AppContainer,
    onNavigate: (String) -> Unit,
) {
    LaunchedEffect(appContainer) {
        val settings = appContainer.settingsRepository.observeSettings().first()
        val account = appContainer.profileRepository.observeActiveAccount().filterNotNull().first()
        val schedule = appContainer.scheduleRepository.observeActiveSchedule(account.id).first()
        val route = when {
            schedule != null -> RouteReview
            settings.hasSeenScheduleIntro -> RoutePoolSelector
            else -> RouteScheduleIntro
        }
        withContext(Dispatchers.Main.immediate) {
            onNavigate(route)
        }
    }
}

internal fun scheduleWizardViewModelFactory(
    appContainer: AppContainer,
) = ScheduleWizardViewModelFactory(
    profileRepository = appContainer.profileRepository,
    scheduleRepository = appContainer.scheduleRepository,
    reviewRepository = appContainer.reviewRepository,
    settingsRepository = appContainer.settingsRepository,
    quranCatalogRepository = appContainer.quranCatalogRepository,
    revisionPlanner = appContainer.revisionPlanner,
    timeProvider = appContainer.timeProvider,
    localReminderScheduler = appContainer.localReminderScheduler,
)

internal fun profilesViewModelFactory(
    appContainer: AppContainer,
) = ProfilesViewModelFactory(
    profileRepository = appContainer.profileRepository,
    scheduleRepository = appContainer.scheduleRepository,
    reviewRepository = appContainer.reviewRepository,
    settingsRepository = appContainer.settingsRepository,
    authSessionRepository = appContainer.authSessionRepository,
    timeProvider = appContainer.timeProvider,
    localReminderScheduler = appContainer.localReminderScheduler,
)

internal fun NavHostController.navigateMain(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

internal fun settingsViewModelFactory(
    appContainer: AppContainer,
) = SettingsViewModelFactory(
    settingsRepository = appContainer.settingsRepository,
    localReminderScheduler = appContainer.localReminderScheduler,
)

internal fun progressViewModelFactory(
    appContainer: AppContainer,
) = ProgressViewModelFactory(
    profileRepository = appContainer.profileRepository,
    reviewRepository = appContainer.reviewRepository,
    timeProvider = appContainer.timeProvider,
)

internal fun sharedProfileViewModelFactory(
    appContainer: AppContainer,
    selectedProfileId: String,
) = SharedProfileViewModelFactory(
    selectedProfileId = selectedProfileId,
    profileRepository = appContainer.profileRepository,
    authSessionRepository = appContainer.authSessionRepository,
    cloudSyncManager = appContainer.cloudSyncManager,
)

internal fun AppDestination.toRoute(): String = when (this) {
    AppDestination.Profiles -> RouteProfiles
    AppDestination.ScheduleIntro -> RouteScheduleIntro
    AppDestination.PoolSelector -> RoutePoolSelector
    AppDestination.ScheduleDose -> RouteScheduleDose
    AppDestination.Review -> RouteReview
    AppDestination.Progress -> RouteProgress
    AppDestination.Shared -> RouteShared
    AppDestination.Settings -> RouteSettings
    AppDestination.Debug -> RouteDebug
}

internal fun String?.toAppDestination(): AppDestination = when {
    this == RouteProfiles -> AppDestination.Profiles
    this == RouteScheduleIntro -> AppDestination.ScheduleIntro
    this == RoutePoolSelector -> AppDestination.PoolSelector
    this == RouteScheduleDose -> AppDestination.ScheduleDose
    this == RouteProgress -> AppDestination.Progress
    this == RouteShared || this?.startsWith("$RouteShared/") == true -> AppDestination.Shared
    this == RouteSettings -> AppDestination.Settings
    this == RouteDebug -> AppDestination.Debug
    this == RouteDebugLocalNotifications -> AppDestination.Debug
    this == RouteDebugUiCatalog -> AppDestination.Debug
    else -> AppDestination.Review
}

internal fun AppUiState.addProfile(): AppUiState {
    val nextName = listOf(
        TextSpec(R.string.profile_name_hafsah),
        TextSpec(R.string.profile_name_ibrahim),
        TextSpec(R.string.profile_name_zaynab),
        TextSpec(R.string.profile_name_abdullah),
    ).getOrElse(extraProfileCount) {
        TextSpec(R.string.profile_name_child_generic, listOf(extraProfileCount + 1))
    }
    val newProfileId = "child-$extraProfileCount"
    val newProfile = activeProfile.copy(
        id = newProfileId,
        name = nextName,
        isSelfProfile = false,
        isShared = false,
        guardians = setOf(Guardian.Mother),
        notificationsEnabled = true,
        activityFeed = listOf(
            TextSpec(R.string.feed_new_child_created, listOf(nextName)),
            TextSpec(R.string.feed_new_child_next_step),
        ),
    )
    return copy(
        profiles = profiles + newProfile,
        activeProfileId = newProfileId,
        extraProfileCount = extraProfileCount + 1,
        destination = AppDestination.PoolSelector,
        scheduleReturnDestination = AppDestination.Profiles,
    )
}

internal fun TextSpec.resolve(context: Context): String =
    rawText ?: context.getString(resId!!, *formatArgs.map { arg ->
        when (arg) {
            is TextSpec -> arg.resolve(context)
            else -> arg
        }
    }.toTypedArray())
