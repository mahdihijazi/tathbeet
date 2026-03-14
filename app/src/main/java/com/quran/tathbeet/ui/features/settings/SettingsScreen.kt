package com.quran.tathbeet.ui.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onForceDarkThemeChanged: () -> Unit,
    onGlobalNotificationsChanged: () -> Unit,
    onMotivationalMessagesChanged: () -> Unit,
    onProfileNotificationsChanged: (String) -> Unit,
    onReminderTimeSelected: (Int, Int) -> Unit,
    onRequestNotificationPermission: () -> Unit,
) {
    if (uiState.isLoading) {
        return
    }

    var showReminderDialog by remember { mutableStateOf(false) }
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
                forceDarkTheme = uiState.forceDarkTheme,
                onToggle = onForceDarkThemeChanged,
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
}

@Composable
internal fun ThemeSettingsCard(
    forceDarkTheme: Boolean,
    onToggle: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        SettingToggleRow(
            label = stringResource(R.string.settings_theme_title),
            supporting = stringResource(R.string.settings_theme_supporting),
            enabled = forceDarkTheme,
            onToggle = onToggle,
            switchTag = "settings-dark-theme-toggle",
        )
    }
}

@Composable
private fun ProfileReminderCard(
    profile: SettingsProfileUiState,
    onToggle: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            profile.isActive -> stringResource(R.string.settings_profile_status_active)
                            profile.isSelfProfile -> stringResource(R.string.settings_profile_status_self)
                            else -> stringResource(R.string.settings_profile_status_additional)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = profile.notificationsEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("settings-profile-toggle-${profile.id}"),
                )
            }
            Text(
                text = if (profile.hasSchedule) {
                    stringResource(R.string.settings_profile_schedule_ready)
                } else {
                    stringResource(R.string.settings_profile_schedule_missing)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReminderSettingsCard(
    uiState: SettingsUiState,
    onGlobalNotificationsChanged: () -> Unit,
    onMotivationalMessagesChanged: () -> Unit,
    onOpenReminderTime: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            SettingToggleRow(
                label = stringResource(R.string.settings_global_notifications),
                supporting = stringResource(R.string.settings_global_notifications_supporting),
                enabled = uiState.globalNotificationsEnabled,
                onToggle = onGlobalNotificationsChanged,
                switchTag = "settings-global-toggle",
            )
            SettingsRowDivider()
            SettingToggleRow(
                label = stringResource(R.string.settings_motivational_messages),
                supporting = stringResource(R.string.settings_motivational_messages_supporting),
                enabled = uiState.motivationalMessagesEnabled,
                onToggle = onMotivationalMessagesChanged,
                switchTag = "settings-motivational-toggle",
            )
            SettingsRowDivider()
            ReminderTimeRow(
                hour = uiState.reminderHour,
                minute = uiState.reminderMinute,
                onOpenReminderTime = onOpenReminderTime,
            )
        }
    }
}

@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    )
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

@Composable
private fun SettingToggleRow(
    label: String,
    supporting: String,
    enabled: Boolean,
    onToggle: () -> Unit,
    switchTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.testTag(switchTag),
        )
    }
}

@Composable
private fun ReminderTimeRow(
    hour: Int,
    minute: Int,
    onOpenReminderTime: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_reminder_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.settings_reminder_current,
                formatReminderTime(hour, minute),
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.settings_reminder_supporting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppSecondaryButton(
            text = stringResource(R.string.settings_reminder_change),
            onClick = onOpenReminderTime,
            modifier = Modifier.testTag("settings-reminder-open"),
        )
    }
}

private fun formatReminderTime(
    hour: Int,
    minute: Int,
): String = "%02d:%02d".format(hour, minute)
