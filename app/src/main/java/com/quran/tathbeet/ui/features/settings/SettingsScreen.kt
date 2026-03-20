package com.quran.tathbeet.ui.features.settings

import androidx.compose.foundation.layout.Box
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

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    hasNotificationPermission: Boolean,
    onThemeModeSelected: (com.quran.tathbeet.domain.model.AppThemeMode) -> Unit,
    onGlobalNotificationsChanged: () -> Unit,
    onMotivationalMessagesChanged: () -> Unit,
    onReminderTimeSelected: (Int, Int) -> Unit,
    onRequestNotificationPermission: () -> Unit,
) {
    if (uiState.isLoading) {
        return
    }

    var showReminderDialog by remember { mutableStateOf(false) }

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
