package com.quran.tathbeet.ui

import android.content.Context
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppShell
import com.quran.tathbeet.ui.features.progress.ProgressScreen
import com.quran.tathbeet.ui.features.profiles.ProfilesScreen
import com.quran.tathbeet.ui.features.review.ReviewScreen
import com.quran.tathbeet.ui.features.schedule.PoolSelectorScreen
import com.quran.tathbeet.ui.features.schedule.ScheduleScreen
import com.quran.tathbeet.ui.features.schedule.ScheduleIntroScreen
import com.quran.tathbeet.ui.features.settings.SettingsScreen
import com.quran.tathbeet.ui.features.shared.SharedProfileScreen
import com.quran.tathbeet.ui.model.AccountMode
import com.quran.tathbeet.ui.model.AppDestination
import com.quran.tathbeet.ui.model.AppProfile
import com.quran.tathbeet.ui.model.AppUiState
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.Guardian
import com.quran.tathbeet.ui.model.PaceMethod
import com.quran.tathbeet.ui.model.SyncState
import com.quran.tathbeet.ui.model.TextSpec
import com.quran.tathbeet.ui.model.activeProfile
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.model.completionRate
import com.quran.tathbeet.ui.model.cycleLength
import com.quran.tathbeet.ui.model.dailyProgress
import com.quran.tathbeet.ui.model.displayLabelRes
import com.quran.tathbeet.ui.model.generateTasksForProfile
import com.quran.tathbeet.ui.model.loadQuranCatalog
import com.quran.tathbeet.ui.model.poolSegmentCount
import com.quran.tathbeet.ui.model.recommendedPace
import com.quran.tathbeet.ui.model.reminderOptions
import com.quran.tathbeet.ui.model.scheduleWizardStartDestination
import com.quran.tathbeet.ui.model.seedAppState
import com.quran.tathbeet.ui.model.updateActiveProfile
import kotlinx.coroutines.launch

@Composable
fun TathbeetApp() {
    val context = LocalContext.current
    val quranCatalog = remember(context) { loadQuranCatalog(context) }
    var uiState by remember(quranCatalog) { mutableStateOf(seedAppState(quranCatalog)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun mutate(message: TextSpec? = null, transform: (AppUiState) -> AppUiState) {
        uiState = transform(uiState)
        if (message != null) {
            scope.launch {
                snackbarHostState.showSnackbar(message.resolve(context))
            }
        }
    }

    val activeProfile = uiState.activeProfile
    val activePoolSelections = quranCatalog.resolveSelections(activeProfile.selectedPoolKeys)

    fun openScheduleWizard(from: AppDestination) {
        mutate {
            it.copy(
                destination = it.scheduleWizardStartDestination(),
                scheduleReturnDestination = from,
            )
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AppShell(
            uiState = uiState,
            onNavigate = { destination ->
                mutate { it.copy(destination = destination) }
            },
            onBack = {
                mutate {
                    when (it.destination) {
                        AppDestination.ScheduleIntro -> it
                        AppDestination.PoolSelector -> {
                            if (it.hasCompletedScheduleOnboarding) {
                                it.copy(destination = it.scheduleReturnDestination)
                            } else {
                                it.copy(destination = AppDestination.ScheduleIntro)
                            }
                        }
                        AppDestination.ScheduleDose -> it.copy(destination = AppDestination.PoolSelector)
                        else -> it.copy(destination = AppDestination.Review)
                    }
                }
            },
            onAccountAction = {
                mutate(
                    if (uiState.accountMode == AccountMode.Guest) {
                        TextSpec(R.string.snackbar_account_enabled)
                    } else {
                        TextSpec(R.string.snackbar_guest_mode)
                    },
                ) {
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
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) {
            when (uiState.destination) {
                AppDestination.Profiles -> ProfilesScreen(
                    uiState = uiState,
                    onProfileSelected = { profileId ->
                        mutate { it.copy(activeProfileId = profileId) }
                    },
                    onProfileNotificationsToggled = { profileId ->
                        mutate {
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
                        mutate(TextSpec(R.string.snackbar_child_profile_created)) { state ->
                            state.addProfile()
                        }
                    },
                    onOpenSchedule = {
                        openScheduleWizard(AppDestination.Profiles)
                    },
                    onOpenSharedProfile = {
                        mutate { it.copy(destination = AppDestination.Shared) }
                    },
                )

                AppDestination.ScheduleIntro -> ScheduleIntroScreen(
                    onNext = {
                        mutate {
                            it.copy(
                                destination = AppDestination.PoolSelector,
                                hasSeenScheduleIntro = true,
                            )
                        }
                    },
                )

                AppDestination.PoolSelector -> PoolSelectorScreen(
                    selectedCategory = uiState.activeSelectionCategory,
                    optionsForCategory = quranCatalog::itemsFor,
                    selectedPool = activePoolSelections,
                    onCategorySelected = { category ->
                        mutate { it.copy(activeSelectionCategory = category) }
                    },
                    onToggleSelection = { option ->
                        mutate {
                            it.updateActiveProfile { profile ->
                                val nextPool = profile.selectedPoolKeys.toMutableSet().apply {
                                    if (!add(option.key)) {
                                        remove(option.key)
                                    }
                                }
                                profile.copy(selectedPoolKeys = nextPool)
                            }
                        }
                    },
                    onDone = {
                        mutate { it.copy(destination = AppDestination.ScheduleDose) }
                    },
                )

                AppDestination.ScheduleDose -> ScheduleScreen(
                    selectedPool = activePoolSelections,
                    paceMethod = activeProfile.paceMethod,
                    selectedCycleTarget = activeProfile.cycleTarget,
                    selectedPace = activeProfile.pace,
                    segmentCount = uiState.poolSegmentCount(quranCatalog),
                    cycleLength = uiState.cycleLength(quranCatalog),
                    onCycleTargetSelected = { cycleTarget ->
                        mutate {
                            val nextSegmentCount = it.poolSegmentCount(quranCatalog)
                            it.updateActiveProfile { profile ->
                                profile.copy(
                                    paceMethod = PaceMethod.CycleTarget,
                                    cycleTarget = cycleTarget,
                                    pace = recommendedPace(nextSegmentCount, cycleTarget),
                                )
                            }
                        }
                    },
                    onPaceSelected = { pace ->
                        mutate {
                            it.updateActiveProfile { profile ->
                                profile.copy(
                                    paceMethod = PaceMethod.Manual,
                                    pace = pace,
                                )
                            }
                        }
                    },
                    onResetToCycleMode = {
                        mutate {
                            val nextSegmentCount = it.poolSegmentCount(quranCatalog)
                            it.updateActiveProfile { profile ->
                                profile.copy(
                                    paceMethod = PaceMethod.CycleTarget,
                                    pace = recommendedPace(nextSegmentCount, profile.cycleTarget),
                                )
                            }
                        }
                    },
                    onSaveSchedule = {
                        mutate(TextSpec(R.string.snackbar_schedule_updated)) {
                            it.updateActiveProfile { profile ->
                                val refreshed = profile.copy(reviewTasks = generateTasksForProfile(profile, quranCatalog))
                                refreshed.copy(
                                    weekCompletion = refreshed.weekCompletion.dropLast(1) + refreshed.dailyProgress,
                                )
                            }.copy(
                                destination = AppDestination.Review,
                                hasCompletedScheduleOnboarding = true,
                                syncState = if (it.accountMode == AccountMode.Google) {
                                    SyncState.SyncPending
                                } else {
                                    SyncState.OfflineReady
                                },
                            )
                        }
                    },
                )

                AppDestination.Review -> ReviewScreen(
                    profile = activeProfile,
                    completionRate = activeProfile.completionRate,
                    onToggleTask = { taskId ->
                        mutate {
                            it.updateActiveProfile { profile ->
                                val nextTasks = profile.reviewTasks.map { task ->
                                    if (task.id == taskId) task.copy(isDone = !task.isDone) else task
                                }
                                profile.copy(
                                    reviewTasks = nextTasks,
                                    weekCompletion = profile.weekCompletion.dropLast(1) + (
                                        if (nextTasks.isEmpty()) 0f else nextTasks.count { task -> task.isDone }.toFloat() / nextTasks.size
                                    ),
                                )
                            }
                        }
                    },
                    onCompleteDay = {
                        mutate(TextSpec(R.string.snackbar_review_done)) {
                            it.updateActiveProfile { profile ->
                                val nextTasks = profile.reviewTasks.map { task -> task.copy(isDone = true) }
                                profile.copy(
                                    reviewTasks = nextTasks,
                                    weekCompletion = profile.weekCompletion.dropLast(1) + 1f,
                                )
                            }
                        }
                    },
                    onResetDay = {
                        mutate(TextSpec(R.string.snackbar_review_reset)) {
                            it.updateActiveProfile { profile ->
                                val nextTasks = generateTasksForProfile(profile, quranCatalog).mapIndexed { index, task ->
                                    task.copy(isDone = index == 0)
                                }
                                profile.copy(
                                    reviewTasks = nextTasks,
                                    weekCompletion = profile.weekCompletion.dropLast(1) + (
                                        nextTasks.count { task -> task.isDone }.toFloat() / nextTasks.size
                                    ),
                                )
                            }
                        }
                    },
                    onOpenSchedule = {
                        openScheduleWizard(AppDestination.Review)
                    },
                )

                AppDestination.Progress -> ProgressScreen(
                    profile = activeProfile,
                    completionRate = activeProfile.completionRate,
                    onOpenReview = {
                        mutate { it.copy(destination = AppDestination.Review) }
                    },
                )

                AppDestination.Shared -> SharedProfileScreen(
                    profile = activeProfile,
                    accountMode = uiState.accountMode,
                    syncState = uiState.syncState,
                    onGuardianToggled = { guardian ->
                        mutate {
                            it.updateActiveProfile { profile ->
                                val nextGuardians = profile.guardians.toMutableSet().apply {
                                    if (!add(guardian)) remove(guardian)
                                }
                                profile.copy(
                                    guardians = nextGuardians,
                                    isShared = nextGuardians.size > 1,
                                )
                            }
                        }
                    },
                    onSimulateSync = {
                        if (uiState.accountMode == AccountMode.Guest) {
                            mutate(TextSpec(R.string.snackbar_create_account_for_sharing)) { it }
                        } else {
                            mutate(TextSpec(R.string.snackbar_sync_simulated)) {
                                it.copy(
                                    syncState = when (it.syncState) {
                                        SyncState.OfflineReady -> SyncState.SyncPending
                                        SyncState.SyncPending -> SyncState.Synced
                                        SyncState.Synced -> SyncState.SyncPending
                                    },
                                ).updateActiveProfile { profile ->
                                    profile.copy(
                                        activityFeed = listOf(
                                            TextSpec(R.string.feed_sync_now, listOf(profile.name.resolve(context))),
                                            TextSpec(R.string.feed_shared_mother_time),
                                            TextSpec(R.string.feed_shared_father_done),
                                        ),
                                    )
                                }
                            }
                        }
                    },
                )

                AppDestination.Settings -> SettingsScreen(
                    uiState = uiState,
                    activeProfile = activeProfile,
                    onGlobalNotificationsChanged = {
                        mutate { it.copy(globalNotificationsEnabled = !it.globalNotificationsEnabled) }
                    },
                    onMotivationalMessagesChanged = {
                        mutate { it.copy(motivationalMessagesEnabled = !it.motivationalMessagesEnabled) }
                    },
                    onProfileNotificationsChanged = {
                        mutate {
                            it.updateActiveProfile { profile -> profile.copy(notificationsEnabled = !profile.notificationsEnabled) }
                        }
                    },
                    onReminderTimeChanged = {
                        mutate {
                            val currentIndex = reminderOptions.indexOf(it.reminderTime)
                            val nextIndex = (currentIndex + 1) % reminderOptions.size
                            it.copy(reminderTime = reminderOptions[nextIndex])
                        }
                    },
                    onAccountModeChanged = {
                        mutate {
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
        }
    }
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
        destination = scheduleWizardStartDestination(),
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
