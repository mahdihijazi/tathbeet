package com.quran.tathbeet.ui.features.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import com.quran.tathbeet.ui.prototype.Guardian
import com.quran.tathbeet.ui.prototype.PrototypeProfile
import com.quran.tathbeet.ui.prototype.PrototypeUiState
import com.quran.tathbeet.ui.prototype.activeProfile
import com.quran.tathbeet.ui.prototype.asString
import com.quran.tathbeet.ui.prototype.completionRate

@Composable
fun ProfilesScreen(
    uiState: PrototypeUiState,
    onProfileSelected: (String) -> Unit,
    onProfileNotificationsToggled: (String) -> Unit,
    onAddChildProfile: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenSharedProfile: () -> Unit,
) {
    val activeProfile = uiState.activeProfile

    PrototypeScreenLayout(
        title = stringResource(R.string.profile_screen_title),
        subtitle = stringResource(R.string.profile_screen_subtitle),
    ) {
        item {
            HeroCard(
                eyebrow = stringResource(R.string.profile_active_eyebrow),
                title = activeProfile.name.asString(),
                body = stringResource(R.string.profile_active_body),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(onClick = onOpenSchedule) {
                        Text(stringResource(R.string.profile_button_open_schedule))
                    }
                    if (!activeProfile.isSelfProfile) {
                        Button(onClick = onOpenSharedProfile) {
                            Text(stringResource(R.string.profile_button_manage_sharing))
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.profile_section_title),
                subtitle = stringResource(R.string.profile_section_subtitle),
            )
        }

        items(uiState.profiles, key = { it.id }) { profile ->
            ProfileCard(
                profile = profile,
                isActive = profile.id == activeProfile.id,
                onSelected = { onProfileSelected(profile.id) },
                onToggleNotifications = { onProfileNotificationsToggled(profile.id) },
            )
        }

        item {
            Button(onClick = onAddChildProfile) {
                Text(stringResource(R.string.profile_add_child))
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: PrototypeProfile,
    isActive: Boolean,
    onSelected: () -> Unit,
    onToggleNotifications: () -> Unit,
) {
    val guardianNames = profile.guardians.map { guardian ->
        if (guardian == Guardian.Mother) {
            stringResource(R.string.guardian_mother)
        } else {
            stringResource(R.string.guardian_father)
        }
    }
    val sharedWithLabel = if (profile.isShared) {
        stringResource(R.string.profile_shared_with, guardianNames.joinToString(separator = "، "))
    } else {
        stringResource(R.string.profile_local_only)
    }

    Card(onClick = onSelected) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = profile.name.asString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (profile.isSelfProfile) {
                            stringResource(R.string.profile_type_self)
                        } else {
                            stringResource(R.string.profile_type_child)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilterChip(
                    selected = isActive,
                    onClick = onSelected,
                    label = {
                        Text(
                            if (isActive) {
                                stringResource(R.string.profile_chip_active)
                            } else {
                                stringResource(R.string.profile_chip_activate)
                            },
                        )
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.profile_goal),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(stringResource(profile.pace.labelRes))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.profile_completion_rate),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(stringResource(R.string.percentage_value, profile.completionRate))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.profile_today_tasks_done,
                        profile.reviewTasks.count { it.isDone },
                        profile.reviewTasks.size,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = profile.notificationsEnabled,
                    onCheckedChange = { onToggleNotifications() },
                )
            }
            Text(
                text = sharedWithLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
