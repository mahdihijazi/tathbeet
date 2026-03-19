package com.quran.tathbeet.ui.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.domain.model.AppThemeMode
import com.quran.tathbeet.ui.components.AppSelectionChip
import com.quran.tathbeet.ui.components.AppSecondaryButton

@Composable
internal fun ThemeSettingsCard(
    themeMode: AppThemeMode,
    onThemeModeSelected: (AppThemeMode) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_theme_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.settings_theme_supporting),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppSelectionChip(
                    text = stringResource(R.string.settings_theme_mode_system),
                    selected = themeMode == AppThemeMode.System,
                    onClick = { onThemeModeSelected(AppThemeMode.System) },
                    modifier = Modifier.testTag("settings-theme-mode-system"),
                )
                AppSelectionChip(
                    text = stringResource(R.string.settings_theme_mode_light),
                    selected = themeMode == AppThemeMode.Light,
                    onClick = { onThemeModeSelected(AppThemeMode.Light) },
                    modifier = Modifier.testTag("settings-theme-mode-light"),
                )
                AppSelectionChip(
                    text = stringResource(R.string.settings_theme_mode_dark),
                    selected = themeMode == AppThemeMode.Dark,
                    onClick = { onThemeModeSelected(AppThemeMode.Dark) },
                    modifier = Modifier.testTag("settings-theme-mode-dark"),
                )
            }
        }
    }
}

@Composable
internal fun ProfileReminderCard(
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
internal fun ReminderSettingsCard(
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
internal fun SettingsRowDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    )
}

@Composable
internal fun SettingToggleRow(
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
internal fun ReminderTimeRow(
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
                "%02d:%02d".format(hour, minute),
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
