package com.quran.tathbeet.ui.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.HeroCard
import com.quran.tathbeet.ui.components.PrototypeScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.prototype.AccountMode
import com.quran.tathbeet.ui.prototype.PrototypeProfile
import com.quran.tathbeet.ui.prototype.PrototypeUiState
import com.quran.tathbeet.ui.prototype.asString
import com.quran.tathbeet.ui.prototype.displayLabelRes

@Composable
fun SettingsScreen(
    uiState: PrototypeUiState,
    activeProfile: PrototypeProfile,
    onGlobalNotificationsChanged: () -> Unit,
    onMotivationalMessagesChanged: () -> Unit,
    onProfileNotificationsChanged: () -> Unit,
    onReminderTimeChanged: () -> Unit,
    onAccountModeChanged: () -> Unit,
) {
    val notificationSettings = listOf(
        Triple(
            stringResource(R.string.settings_global_notifications),
            uiState.globalNotificationsEnabled,
            onGlobalNotificationsChanged,
        ),
        Triple(
            stringResource(R.string.settings_motivational_messages),
            uiState.motivationalMessagesEnabled,
            onMotivationalMessagesChanged,
        ),
        Triple(
            stringResource(R.string.settings_profile_notifications, activeProfile.name.asString()),
            activeProfile.notificationsEnabled,
            onProfileNotificationsChanged,
        ),
    )

    PrototypeScreenLayout(
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_subtitle),
    ) {
        item {
            HeroCard(
                eyebrow = stringResource(R.string.settings_eyebrow),
                title = stringResource(R.string.settings_body_title),
                body = stringResource(R.string.settings_body),
            )
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_notifications_title),
                subtitle = stringResource(R.string.settings_notifications_subtitle),
            )
        }

        items(notificationSettings) { (label, enabled, action) ->
            SettingCard(label = label, enabled = enabled, onToggle = action)
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_reminder_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(stringResource(R.string.settings_reminder_current, uiState.reminderTime))
                    Button(onClick = onReminderTimeChanged) {
                        Text(stringResource(R.string.settings_reminder_change))
                    }
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_account_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(
                            R.string.shared_account_mode,
                            if (uiState.accountMode == AccountMode.Guest) {
                                stringResource(R.string.account_mode_guest)
                            } else {
                                stringResource(R.string.account_mode_account)
                            },
                        ),
                    )
                    Text(stringResource(R.string.shared_sync_state, stringResource(uiState.syncState.displayLabelRes())))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onAccountModeChanged) {
                            Text(stringResource(R.string.settings_toggle_account_mode))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingCard(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
            )
        }
    }
}
