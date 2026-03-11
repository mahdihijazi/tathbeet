package com.quran.tathbeet.ui

import android.content.Context
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.quran.tathbeet.R
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.ui.components.AppShell
import com.quran.tathbeet.ui.features.progress.ProgressScreen
import com.quran.tathbeet.ui.features.profiles.ProfilesScreen
import com.quran.tathbeet.ui.features.review.ReviewScreen
import com.quran.tathbeet.ui.features.review.ReviewViewModel
import com.quran.tathbeet.ui.features.review.ReviewViewModelFactory
import com.quran.tathbeet.ui.features.schedule.PoolSelectorScreen
import com.quran.tathbeet.ui.features.schedule.ScheduleIntroScreen
import com.quran.tathbeet.ui.features.schedule.ScheduleScreen
import com.quran.tathbeet.ui.features.schedule.ScheduleWizardViewModel
import com.quran.tathbeet.ui.features.schedule.ScheduleWizardViewModelFactory
import com.quran.tathbeet.ui.features.settings.SettingsScreen
import com.quran.tathbeet.ui.features.shared.SharedProfileScreen
import com.quran.tathbeet.ui.model.AccountMode
import com.quran.tathbeet.ui.model.AppDestination
import com.quran.tathbeet.ui.model.AppProfile
import com.quran.tathbeet.ui.model.AppUiState
import com.quran.tathbeet.ui.model.Guardian
import com.quran.tathbeet.ui.model.SyncState
import com.quran.tathbeet.ui.model.TextSpec
import com.quran.tathbeet.ui.model.activeProfile
import com.quran.tathbeet.ui.model.completionRate
import com.quran.tathbeet.ui.model.displayLabelRes
import com.quran.tathbeet.ui.model.loadQuranCatalog
import com.quran.tathbeet.ui.model.reminderOptions
import com.quran.tathbeet.ui.model.seedAppState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

private const val RouteLaunch = "launch"
private const val RouteScheduleGraph = "schedule_graph"
private const val RouteScheduleIntro = "schedule_intro"
private const val RoutePoolSelector = "pool_selector"
private const val RouteScheduleDose = "schedule_dose"
private const val RouteReview = "review"
private const val RouteProfiles = "profiles"
private const val RouteProgress = "progress"
private const val RouteSettings = "settings"
private const val RouteShared = "shared"

@Composable
fun TathbeetApp(
    appContainer: AppContainer,
) {
    val context = LocalContext.current
    val quranCatalog = remember(context) { loadQuranCatalog(context) }
    var legacyUiState by remember(quranCatalog) { mutableStateOf(seedAppState(context, quranCatalog)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route.toAppDestination()

    LaunchedEffect(appContainer) {
        appContainer.profileRepository.ensureDefaultAccount(
            name = context.getString(R.string.profile_name_self),
        )
    }

    fun mutateLegacy(message: TextSpec? = null, transform: (AppUiState) -> AppUiState) {
        legacyUiState = transform(legacyUiState)
        if (message != null) {
            scope.launch {
                snackbarHostState.showSnackbar(message.resolve(context))
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppShell(
            currentDestination = currentDestination,
            onNavigate = { destination ->
                navController.navigateMain(destination.toRoute())
            },
            onBack = { navController.navigateUp() },
            onReviewPlanAction = {
                navController.navigate(RoutePoolSelector)
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) {
            NavHost(
                navController = navController,
                startDestination = RouteLaunch,
            ) {
                composable(RouteLaunch) {
                    LaunchRoute(
                        appContainer = appContainer,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(RouteLaunch) { inclusive = true }
                            }
                        },
                    )
                }

                navigation(
                    startDestination = RouteScheduleIntro,
                    route = RouteScheduleGraph,
                ) {
                    composable(RouteScheduleIntro) { backStackEntry ->
                        val scheduleGraphEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(RouteScheduleGraph)
                        }
                        val wizardViewModel: ScheduleWizardViewModel = viewModel(
                            viewModelStoreOwner = scheduleGraphEntry,
                            factory = scheduleWizardViewModelFactory(appContainer),
                        )
                        ScheduleIntroScreen(
                            onNext = {
                                wizardViewModel.markIntroSeen()
                                navController.navigate(RoutePoolSelector)
                            },
                        )
                    }
                    composable(RoutePoolSelector) { backStackEntry ->
                        val scheduleGraphEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(RouteScheduleGraph)
                        }
                        val wizardViewModel: ScheduleWizardViewModel = viewModel(
                            viewModelStoreOwner = scheduleGraphEntry,
                            factory = scheduleWizardViewModelFactory(appContainer),
                        )
                        val uiState by wizardViewModel.uiState.collectAsState()
                        if (!uiState.isLoading) {
                            val catalog = appContainer.quranCatalogRepository.getCatalog()
                            PoolSelectorScreen(
                                selectedCategory = uiState.selectedCategory,
                                optionsForCategory = catalog::itemsFor,
                                selectedPool = uiState.selectedPool,
                                showWizardHeader = uiState.isOnboarding,
                                onCategorySelected = wizardViewModel::selectCategory,
                                onToggleSelection = wizardViewModel::toggleSelection,
                                onDone = { navController.navigate(RouteScheduleDose) },
                            )
                        }
                    }
                    composable(RouteScheduleDose) { backStackEntry ->
                        val scheduleGraphEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(RouteScheduleGraph)
                        }
                        val wizardViewModel: ScheduleWizardViewModel = viewModel(
                            viewModelStoreOwner = scheduleGraphEntry,
                            factory = scheduleWizardViewModelFactory(appContainer),
                        )
                        val uiState by wizardViewModel.uiState.collectAsState()
                        if (!uiState.isLoading) {
                            ScheduleScreen(
                                selectedPool = uiState.selectedPool,
                                paceMethod = uiState.paceMethod,
                                selectedCycleTarget = uiState.selectedCycleTarget,
                                selectedPace = uiState.selectedPace,
                                segmentCount = uiState.segmentCount,
                                cycleLength = uiState.cycleLength,
                                showWizardHeader = uiState.isOnboarding,
                                onCycleTargetSelected = wizardViewModel::selectCycleTarget,
                                onPaceSelected = wizardViewModel::selectManualPace,
                                onResetToCycleMode = wizardViewModel::resetToCycleMode,
                                onSaveSchedule = {
                                    wizardViewModel.saveSchedule {
                                        navController.navigate(RouteReview) {
                                            popUpTo(RouteScheduleGraph) { inclusive = true }
                                        }
                                    }
                                },
                            )
                        }
                    }
                }

                composable(RouteReview) {
                    val reviewViewModel: ReviewViewModel = viewModel(
                        factory = ReviewViewModelFactory(
                            profileRepository = appContainer.profileRepository,
                            reviewRepository = appContainer.reviewRepository,
                            timeProvider = appContainer.timeProvider,
                        ),
                    )
                    val uiState by reviewViewModel.uiState.collectAsState()
                    ReviewScreen(
                        uiState = uiState,
                        onRequestTaskCompletion = reviewViewModel::requestCompleteTask,
                        onSelectRating = reviewViewModel::updatePendingRating,
                        onDismissRatingDialog = reviewViewModel::dismissRatingDialog,
                        onRestartCycle = reviewViewModel::restartCycle,
                        onDismissCycleResetDialog = reviewViewModel::dismissCycleResetDialog,
                    )
                }

                composable(RouteProfiles) {
                    ProfilesScreen(
                        uiState = legacyUiState,
                        onProfileSelected = { profileId ->
                            mutateLegacy { it.copy(activeProfileId = profileId) }
                        },
                        onProfileNotificationsToggled = { profileId ->
                            mutateLegacy {
                                it.copy(
                                    profiles = it.profiles.map { profile ->
                                        if (profile.id == profileId) {
                                            profile.copy(notificationsEnabled = !profile.notificationsEnabled)
                                        } else {
                                            profile
                                        }
                                    },
                                )
                            }
                        },
                        onAddChildProfile = {
                            mutateLegacy(TextSpec(R.string.snackbar_child_profile_created)) { state ->
                                state.addProfile()
                            }
                        },
                        onOpenSchedule = { navController.navigate(RoutePoolSelector) },
                        onOpenSharedProfile = { navController.navigate(RouteShared) },
                    )
                }

                composable(RouteProgress) {
                    val activeProfile = legacyUiState.activeProfile
                    ProgressScreen(
                        profile = activeProfile,
                        completionRate = activeProfile.completionRate,
                        onOpenReview = { navController.navigate(RouteReview) },
                    )
                }

                composable(RouteSettings) {
                    SettingsScreen(
                        uiState = legacyUiState,
                        activeProfile = legacyUiState.activeProfile,
                        onGlobalNotificationsChanged = {
                            mutateLegacy { it.copy(globalNotificationsEnabled = !it.globalNotificationsEnabled) }
                        },
                        onMotivationalMessagesChanged = {
                            mutateLegacy { it.copy(motivationalMessagesEnabled = !it.motivationalMessagesEnabled) }
                        },
                        onProfileNotificationsChanged = {
                            mutateLegacy {
                                it.copy(
                                    profiles = it.profiles.map { profile ->
                                        if (profile.id == it.activeProfileId) {
                                            profile.copy(notificationsEnabled = !profile.notificationsEnabled)
                                        } else {
                                            profile
                                        }
                                    },
                                )
                            }
                        },
                        onReminderTimeChanged = {
                            mutateLegacy {
                                val currentIndex = reminderOptions.indexOf(it.reminderTime)
                                val nextIndex = (currentIndex + 1) % reminderOptions.size
                                it.copy(reminderTime = reminderOptions[nextIndex])
                            }
                        },
                        onAccountModeChanged = {
                            mutateLegacy {
                                it.copy(
                                    accountMode = if (it.accountMode == AccountMode.Guest) AccountMode.Google else AccountMode.Guest,
                                    syncState = if (it.accountMode == AccountMode.Guest) {
                                        SyncState.Synced
                                    } else {
                                        SyncState.OfflineReady
                                    },
                                )
                            }
                        },
                    )
                }

                composable(RouteShared) {
                    SharedProfileScreen(
                        profile = legacyUiState.activeProfile,
                        accountMode = legacyUiState.accountMode,
                        syncState = legacyUiState.syncState,
                        onGuardianToggled = { guardian ->
                            mutateLegacy {
                                it.copy(
                                    profiles = it.profiles.map { profile ->
                                        if (profile.id == it.activeProfileId) {
                                            val nextGuardians = profile.guardians.toMutableSet().apply {
                                                if (!add(guardian)) {
                                                    remove(guardian)
                                                }
                                            }
                                            profile.copy(
                                                guardians = nextGuardians,
                                                isShared = nextGuardians.size > 1,
                                            )
                                        } else {
                                            profile
                                        }
                                    },
                                )
                            }
                        },
                        onSimulateSync = {
                            mutateLegacy {
                                it.copy(
                                    syncState = when (it.syncState) {
                                        SyncState.OfflineReady -> SyncState.SyncPending
                                        SyncState.SyncPending -> SyncState.Synced
                                        SyncState.Synced -> SyncState.OfflineReady
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchRoute(
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

private fun scheduleWizardViewModelFactory(
    appContainer: AppContainer,
) = ScheduleWizardViewModelFactory(
    profileRepository = appContainer.profileRepository,
    scheduleRepository = appContainer.scheduleRepository,
    settingsRepository = appContainer.settingsRepository,
    quranCatalogRepository = appContainer.quranCatalogRepository,
    revisionPlanner = appContainer.revisionPlanner,
)

private fun NavHostController.navigateMain(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun AppDestination.toRoute(): String = when (this) {
    AppDestination.Profiles -> RouteProfiles
    AppDestination.ScheduleIntro -> RouteScheduleIntro
    AppDestination.PoolSelector -> RoutePoolSelector
    AppDestination.ScheduleDose -> RouteScheduleDose
    AppDestination.Review -> RouteReview
    AppDestination.Progress -> RouteProgress
    AppDestination.Shared -> RouteShared
    AppDestination.Settings -> RouteSettings
}

private fun String?.toAppDestination(): AppDestination = when (this) {
    RouteProfiles -> AppDestination.Profiles
    RouteScheduleIntro -> AppDestination.ScheduleIntro
    RoutePoolSelector -> AppDestination.PoolSelector
    RouteScheduleDose -> AppDestination.ScheduleDose
    RouteProgress -> AppDestination.Progress
    RouteShared -> AppDestination.Shared
    RouteSettings -> AppDestination.Settings
    else -> AppDestination.Review
}

private fun AppUiState.addProfile(): AppUiState {
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

private fun TextSpec.resolve(context: Context): String =
    rawText ?: context.getString(resId!!, *formatArgs.map { arg ->
        when (arg) {
            is TextSpec -> arg.resolve(context)
            else -> arg
        }
    }.toTypedArray())
