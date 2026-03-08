package com.quran.tathbeet.ui.features.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppCard
import com.quran.tathbeet.ui.components.AppKeyValueRow
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.AppSelectionChip
import com.quran.tathbeet.ui.components.HeroCard
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.model.AppProfile
import com.quran.tathbeet.ui.model.AppUiState
import com.quran.tathbeet.ui.model.Guardian
import com.quran.tathbeet.ui.model.activeProfile
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.model.completionRate
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ProfilesScreen(
    uiState: AppUiState,
    onProfileSelected: (String) -> Unit,
    onProfileNotificationsToggled: (String) -> Unit,
    onAddChildProfile: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenSharedProfile: () -> Unit,
) {
    val activeProfile = uiState.activeProfile

    ScreenLayout(
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
                ) {
                    AppPrimaryButton(
                        text = stringResource(R.string.profile_button_open_schedule),
                        onClick = onOpenSchedule,
                        modifier = Modifier.weight(1f),
                    )
                    if (!activeProfile.isSelfProfile) {
                        AppSecondaryButton(
                            text = stringResource(R.string.profile_button_manage_sharing),
                            onClick = onOpenSharedProfile,
                            modifier = Modifier.weight(1f),
                        )
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
            AppPrimaryButton(
                text = stringResource(R.string.profile_add_child),
                onClick = onAddChildProfile,
            )
        }
    }
}

@Composable
private fun ProfileCard(
    profile: AppProfile,
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

    AppCard(onClick = onSelected) {
        Column(
            modifier = Modifier.padding(TathbeetTokens.spacing.x2Half),
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.half)) {
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
                AppSelectionChip(
                    selected = isActive,
                    onClick = onSelected,
                    text = if (isActive) {
                        stringResource(R.string.profile_chip_active)
                    } else {
                        stringResource(R.string.profile_chip_activate)
                    },
                )
            }
            AppKeyValueRow(
                label = stringResource(R.string.profile_goal),
                value = stringResource(profile.pace.labelRes),
            )
            AppKeyValueRow(
                label = stringResource(R.string.profile_completion_rate),
                value = stringResource(R.string.percentage_value, profile.completionRate),
            )
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
