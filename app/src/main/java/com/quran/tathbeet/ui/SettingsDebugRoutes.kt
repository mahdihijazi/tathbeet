package com.quran.tathbeet.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quran.tathbeet.R
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.app.ReminderNotificationContent
import com.quran.tathbeet.ui.features.debug.DebugToolsScreen
import com.quran.tathbeet.ui.features.debug.LocalNotificationsDebugScreen
import com.quran.tathbeet.ui.features.debug.UiCatalogDebugScreen
import com.quran.tathbeet.ui.features.settings.SettingsScreen
import com.quran.tathbeet.ui.features.settings.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
internal fun SettingsRoute(
    appContainer: AppContainer,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = settingsViewModelFactory(appContainer),
    )
    val uiState by settingsViewModel.uiState.collectAsState()
    var permissionRefreshToken by remember { mutableStateOf(0) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        permissionRefreshToken++
        scope.launch {
            appContainer.localReminderScheduler.syncSchedules()
        }
    }
    val hasNotificationPermission = remember(context, permissionRefreshToken) {
        context.hasNotificationPermission()
    }
    SettingsScreen(
        uiState = uiState,
        hasNotificationPermission = hasNotificationPermission,
        onThemeModeSelected = settingsViewModel::selectThemeMode,
        onGlobalNotificationsChanged = settingsViewModel::toggleGlobalNotifications,
        onMotivationalMessagesChanged = settingsViewModel::toggleMotivationalMessages,
        onReminderTimeSelected = settingsViewModel::selectReminderTime,
        onRequestNotificationPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
    )
}

@Composable
internal fun DebugToolsRoute(
    appContainer: AppContainer,
    onOpenLocalNotifications: () -> Unit,
    onOpenUiCatalog: () -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lastAuthLink by appContainer.debugAuthLinkStore.observeLastAuthLink().collectAsState(initial = null)
    var authLinkDraft by remember(lastAuthLink) { mutableStateOf(lastAuthLink.orEmpty()) }

    DebugToolsScreen(
        authLink = authLinkDraft,
        onAuthLinkChanged = { authLinkDraft = it },
        onCopyAuthLink = {
            if (authLinkDraft.isNotBlank()) {
                clipboardManager.setText(AnnotatedString(authLinkDraft))
            }
        },
        onOpenAuthLink = {
            if (authLinkDraft.isNotBlank()) {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW).setData(android.net.Uri.parse(authLinkDraft)),
                )
            }
        },
        onOpenLocalNotifications = onOpenLocalNotifications,
        onOpenUiCatalog = onOpenUiCatalog,
    )
}

@Composable
internal fun LocalNotificationsDebugRoute(
    appContainer: AppContainer,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeAccount by appContainer.profileRepository.observeActiveAccount().collectAsState(initial = null)
    val activeTimelineState = activeAccount
        ?.id
        ?.let(appContainer.reviewRepository::observeReviewTimeline)
        ?.collectAsState(initial = emptyList())

    LocalNotificationsDebugScreen(
        sampleProfileName = activeAccount?.name ?: context.getString(R.string.profile_name_self),
        sampleTask = ReminderNotificationContent.nextAvailableTask(
            timeline = activeTimelineState?.value.orEmpty(),
            today = appContainer.timeProvider.today(),
        ),
        reminderScenarios = appContainer.debugNotificationController.reminderScenarios,
        onTriggerReminder = { scenario ->
            scope.launch {
                appContainer.debugNotificationController.triggerReminderNotification(scenario)
            }
        },
    )
}

@Composable
internal fun UiCatalogDebugRoute(
) {
    UiCatalogDebugScreen()
}
