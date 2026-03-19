package com.quran.tathbeet.ui.features.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.InfoActionCard
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader

private const val VisibleProfileThreshold = 5

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    hasNotificationPermission: Boolean,
    onThemeModeSelected: (com.quran.tathbeet.domain.model.AppThemeMode) -> Unit,
    onGlobalNotificationsChanged: () -> Unit,
    onMotivationalMessagesChanged: () -> Unit,
    onProfileNotificationsChanged: (String) -> Unit,
    onReminderTimeSelected: (Int, Int) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestEmailLink: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    if (uiState.isLoading) {
        return
    }

    var showReminderDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showAllProfiles by remember { mutableStateOf(false) }
    val visibleProfiles = if (showAllProfiles || uiState.profiles.size <= VisibleProfileThreshold) {
        uiState.profiles
    } else {
        uiState.profiles.take(VisibleProfileThreshold)
    }
    val hiddenProfileCount = (uiState.profiles.size - visibleProfiles.size).coerceAtLeast(0)

    ScreenLayout(
        title = stringResource(R.string.settings_title),
        subtitle = "",
    ) {
        item {
            ThemeSettingsCard(
                themeMode = uiState.themeMode,
                onThemeModeSelected = onThemeModeSelected,
            )
        }

        if (!hasNotificationPermission) {
            item {
                InfoActionCard(
                    title = stringResource(R.string.settings_permission_title),
                ) {
                    Text(stringResource(R.string.settings_permission_body))
                    AppPrimaryButton(
                        text = stringResource(R.string.settings_permission_action),
                        onClick = onRequestNotificationPermission,
                        modifier = Modifier.testTag("settings-request-permission"),
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_notifications_title),
                subtitle = stringResource(R.string.settings_notifications_subtitle),
            )
        }

        item {
            ReminderSettingsCard(
                uiState = uiState,
                onGlobalNotificationsChanged = onGlobalNotificationsChanged,
                onMotivationalMessagesChanged = onMotivationalMessagesChanged,
                onOpenReminderTime = { showReminderDialog = true },
            )
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_account_title),
                subtitle = stringResource(R.string.settings_account_subtitle),
            )
        }

        item {
            SettingsAccountCard(
                account = uiState.account,
                onRequestEmailLink = { showEmailDialog = true },
                onSignOut = onSignOut,
            )
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_profiles_title),
                subtitle = stringResource(R.string.settings_profiles_subtitle),
            )
        }

        items(visibleProfiles, key = { it.id }) { profile ->
            ProfileReminderCard(
                profile = profile,
                onToggle = { onProfileNotificationsChanged(profile.id) },
            )
        }

        if (hiddenProfileCount > 0) {
            item {
                AppSecondaryButton(
                    text = stringResource(
                        if (showAllProfiles) {
                            R.string.settings_profiles_show_less
                        } else {
                            R.string.settings_profiles_show_more
                        },
                        hiddenProfileCount,
                    ),
                    onClick = { showAllProfiles = !showAllProfiles },
                    modifier = Modifier.testTag("settings-profiles-expand"),
                )
            }
        }
    }

    if (showReminderDialog) {
        ReminderTimeDialog(
            selectedHour = uiState.reminderHour,
            selectedMinute = uiState.reminderMinute,
            onDismiss = { showReminderDialog = false },
            onSelected = { hour, minute ->
                onReminderTimeSelected(hour, minute)
                showReminderDialog = false
            },
        )
    }

    if (showEmailDialog) {
        EmailLinkDialog(
            onDismiss = { showEmailDialog = false },
            onConfirm = { email ->
                onRequestEmailLink(email)
                showEmailDialog = false
            },
        )
    }
}

@Composable
private fun ReminderTimeDialog(
    selectedHour: Int,
    selectedMinute: Int,
    onDismiss: () -> Unit,
    onSelected: (Int, Int) -> Unit,
) {
    var editedHour by remember(selectedHour) { mutableStateOf(selectedHour) }
    var editedMinute by remember(selectedMinute) { mutableStateOf(selectedMinute) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.settings_reminder_dialog_title))
        },
        text = {
            Box(modifier = Modifier.testTag("settings-time-input")) {
                ReminderTimeEditor(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    onValueChanged = { hour, minute ->
                        editedHour = hour
                        editedMinute = minute
                    },
                )
            }
        },
        confirmButton = {
            AppPrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    onSelected(editedHour, editedMinute)
                },
                modifier = Modifier.testTag("settings-time-save"),
            )
        },
        dismissButton = {
            AppSecondaryButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
            )
        },
    )
}

private fun formatReminderTime(
    hour: Int,
    minute: Int,
): String = "%02d:%02d".format(hour, minute)
