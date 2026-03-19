package com.quran.tathbeet.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quran.tathbeet.BuildConfig
import com.quran.tathbeet.R
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.domain.model.AppSettings
import com.quran.tathbeet.domain.model.AppThemeMode
import com.quran.tathbeet.ui.components.AppShell
import com.quran.tathbeet.ui.features.progress.ProgressScreen
import com.quran.tathbeet.ui.features.progress.ProgressViewModel
import com.quran.tathbeet.ui.features.profiles.ProfilesScreen
import com.quran.tathbeet.ui.features.profiles.ProfilesViewModel
import com.quran.tathbeet.ui.features.review.ReviewScreen
import com.quran.tathbeet.ui.features.review.ReviewViewModel
import com.quran.tathbeet.ui.features.review.ReviewViewModelFactory
import com.quran.tathbeet.ui.features.review.reviewSortActionState
import com.quran.tathbeet.ui.features.schedule.PoolSelectorScreen
import com.quran.tathbeet.ui.features.schedule.ScheduleIntroScreen
import com.quran.tathbeet.ui.features.schedule.ScheduleScreen
import com.quran.tathbeet.ui.features.schedule.ScheduleWizardViewModel
import com.quran.tathbeet.ui.features.shared.SharedProfileScreen
import com.quran.tathbeet.ui.features.shared.SharedProfileViewModel
import com.quran.tathbeet.ui.model.AppDestination
import com.quran.tathbeet.ui.theme.TathbeetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Composable
fun TathbeetApp(
    appContainer: AppContainer,
    notificationTargetProfileId: String? = null,
    onNotificationTargetHandled: () -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route.toAppDestination()
    val activeAccount by appContainer.profileRepository.observeActiveAccount().collectAsState(initial = null)
    val appSettings by appContainer.settingsRepository.observeSettings().collectAsState(initial = AppSettings())
    val darkThemeEnabled = when (appSettings.themeMode) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    var onReviewResetAction by remember { mutableStateOf({}) }
    var reviewSortActionState by remember { mutableStateOf<com.quran.tathbeet.ui.features.review.ReviewSortActionState?>(null) }

    LaunchedEffect(appContainer) {
        appContainer.profileRepository.ensureDefaultAccount(
            name = context.getString(R.string.profile_name_self),
        )
        appContainer.localReminderScheduler.syncSchedules()
    }

    LaunchedEffect(currentDestination) {
        if (currentDestination != AppDestination.Review) {
            onReviewResetAction = {}
            reviewSortActionState = null
        }
    }

    suspend fun navigateToNotificationTarget(profileId: String) {
        appContainer.profileRepository.setActiveAccount(profileId)
        withContext(Dispatchers.Main.immediate) {
            navController.navigate(RouteReview) {
                popUpTo(RouteLaunch) { inclusive = true }
                launchSingleTop = true
            }
            onNotificationTargetHandled()
        }
    }

    LaunchedEffect(notificationTargetProfileId, currentBackStackEntry) {
        if (
            notificationTargetProfileId.isNullOrBlank() ||
            currentBackStackEntry == null ||
            currentBackStackEntry?.destination?.route == RouteLaunch
        ) {
            return@LaunchedEffect
        }
        navigateToNotificationTarget(notificationTargetProfileId)
    }

    TathbeetTheme(darkTheme = darkThemeEnabled) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AppShell(
                currentDestination = currentDestination,
                reviewTitle = activeAccount?.name
                    ?.takeIf { currentDestination == AppDestination.Review }
                    ?.let { profileName -> context.getString(R.string.review_title_for_profile, profileName) },
                reviewSortActionState = reviewSortActionState,
                onNavigate = { destination ->
                    navController.navigateMain(destination.toRoute())
                },
                onBack = { navController.navigateUp() },
                onReviewPlanAction = {
                    navController.navigate(RoutePoolSelector)
                },
                onReviewResetAction = onReviewResetAction,
                onSettingsDebugAction = if (BuildConfig.DEBUG) {
                    {
                        navController.navigate(RouteDebug) {
                            launchSingleTop = true
                        }
                    }
                } else {
                    null
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            ) {
                NavHost(
                    navController = navController,
                    startDestination = RouteLaunch,
                ) {
                composable(RouteLaunch) {
                    if (notificationTargetProfileId.isNullOrBlank()) {
                        LaunchRoute(
                            appContainer = appContainer,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(RouteLaunch) { inclusive = true }
                                }
                            },
                        )
                    } else {
                        LaunchedEffect(notificationTargetProfileId) {
                            navigateToNotificationTarget(notificationTargetProfileId)
                        }
                    }
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
                        val uiState by wizardViewModel.uiState.collectAsState()
                        if (!uiState.isLoading) {
                            ScheduleIntroScreen(
                                profileName = uiState.profileName,
                                onProfileNameChanged = wizardViewModel::updateProfileName,
                                onNext = {
                                    wizardViewModel.continueFromIntro {
                                        navController.navigate(RoutePoolSelector)
                                    }
                                },
                            )
                        }
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
                            quranExternalLauncher = appContainer.quranExternalLauncher,
                        ),
                    )
                    val uiState by reviewViewModel.uiState.collectAsState()
                    SideEffect {
                        onReviewResetAction = reviewViewModel::requestCycleReset
                        reviewSortActionState = uiState.reviewSortActionState(
                            reviewViewModel::selectFullPlanSortMode,
                        )
                    }
                    ReviewScreen(
                        uiState = uiState,
                        onTabSelected = reviewViewModel::selectTab,
                        onRequestTaskCompletion = reviewViewModel::requestCompleteTask,
                        onUpdateTaskRating = reviewViewModel::updateTaskRating,
                        onRateTaskFromFullPlan = reviewViewModel::rateTaskFromFullPlan,
                        onLaunchTaskReading = reviewViewModel::launchTaskReading,
                        onRestartCycle = reviewViewModel::restartCycle,
                        onDismissCycleResetWarning = reviewViewModel::dismissCycleResetWarning,
                        onDismissCycleResetDialog = reviewViewModel::dismissCycleResetDialog,
                        onDismissExternalQuranDialog = reviewViewModel::dismissExternalQuranDialog,
                        onOpenQuranAndroidInstall = reviewViewModel::openQuranAndroidInstall,
                        onOpenQuranOnWeb = reviewViewModel::openQuranOnWeb,
                    )
                }

                composable(RouteProfiles) {
                    val profilesViewModel: ProfilesViewModel = viewModel(
                        factory = profilesViewModelFactory(appContainer),
                    )
                    val uiState by profilesViewModel.uiState.collectAsState()
                    ProfilesScreen(
                        uiState = uiState,
                        onProfileSelected = profilesViewModel::selectProfile,
                        onProfileNotificationsToggled = profilesViewModel::toggleProfileNotifications,
                        onAddProfileRequested = profilesViewModel::showCreateDialog,
                        onEditActiveProfileRequested = profilesViewModel::showEditActiveProfileDialog,
                        onOpenSharedProfile = { profileId ->
                            navController.navigate(sharedProfileRoute(profileId)) {
                                launchSingleTop = true
                            }
                        },
                        onProfileNameChanged = profilesViewModel::updateEditorName,
                        onSaveProfile = {
                            profilesViewModel.saveEditor(
                                onCreated = {
                                    navController.navigate(
                                        if (uiState.hasSeenScheduleIntro) {
                                            RoutePoolSelector
                                        } else {
                                            RouteScheduleIntro
                                        },
                                    )
                                },
                            )
                        },
                        onDismissProfileDialog = profilesViewModel::dismissEditor,
                        onRequestDeleteProfile = profilesViewModel::requestDeleteFromEditor,
                        onDismissDeleteProfile = profilesViewModel::dismissDeleteConfirmation,
                        onConfirmDeleteProfile = profilesViewModel::confirmDelete,
                        onOpenSchedule = { navController.navigate(RoutePoolSelector) },
                    )
                }

                composable(RouteProgress) {
                    val progressViewModel: ProgressViewModel = viewModel(
                        factory = progressViewModelFactory(appContainer),
                    )
                    val uiState by progressViewModel.uiState.collectAsState()
                    ProgressScreen(
                        uiState = uiState,
                        onOpenReview = { navController.navigate(RouteReview) },
                    )
                }

                composable(RouteSettings) {
                    SettingsRoute(appContainer = appContainer)
                }

                if (BuildConfig.DEBUG) {
                    composable(RouteDebug) {
                        DebugToolsRoute(
                            appContainer = appContainer,
                            onOpenLocalNotifications = {
                                navController.navigate(RouteDebugLocalNotifications) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenUiCatalog = {
                                navController.navigate(RouteDebugUiCatalog) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(RouteDebugLocalNotifications) {
                        LocalNotificationsDebugRoute(appContainer = appContainer)
                    }
                    composable(RouteDebugUiCatalog) {
                        UiCatalogDebugRoute()
                    }
                }

                composable(
                    route = RouteSharedProfile,
                    arguments = listOf(
                        navArgument(RouteSharedProfileIdArg) {
                            type = NavType.StringType
                        },
                    ),
                ) { backStackEntry ->
                    val profileId = backStackEntry.arguments?.getString(RouteSharedProfileIdArg)
                        ?: return@composable
                    val sharedProfileViewModel: SharedProfileViewModel = viewModel(
                        factory = sharedProfileViewModelFactory(
                            appContainer = appContainer,
                            selectedProfileId = profileId,
                        ),
                    )
                    val uiState by sharedProfileViewModel.uiState.collectAsState()
                    SharedProfileScreen(
                        uiState = uiState,
                        onEnableSharing = sharedProfileViewModel::enableSharing,
                        onInviteEditor = sharedProfileViewModel::inviteEditor,
                        onRemoveEditor = sharedProfileViewModel::removeEditor,
                        onLeaveProfile = sharedProfileViewModel::leaveProfile,
                    )
                }
            }
        }
    }
}
}
