package com.quran.tathbeet.ui.features.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.AppKeyValueRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileDetailsSheet(
    profile: ProfileCardUiState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onActivate: (String) -> Unit,
    onOpenSchedule: (String) -> Unit,
    onEditProfile: (String) -> Unit,
    onOpenSharedProfile: (String) -> Unit,
    onRequestDeleteProfile: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = profile.name,
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
            AppKeyValueRow(
                label = stringResource(R.string.profile_goal),
                value = profile.paceLabelRes?.let { stringResource(it) }
                    ?: stringResource(R.string.profile_schedule_missing),
            )
            AppKeyValueRow(
                label = stringResource(R.string.profile_completion_rate),
                value = stringResource(R.string.percentage_value, profile.completionRate),
            )
            AppKeyValueRow(
                label = stringResource(R.string.profile_today_tasks_label),
                value = if (profile.paceLabelRes == null) {
                    stringResource(R.string.profile_today_tasks_empty)
                } else {
                    stringResource(
                        R.string.profile_today_tasks_done,
                        profile.todayCompletedCount,
                        profile.todayTotalCount,
                    )
                },
            )

            if (!profile.isActive) {
                AppPrimaryButton(
                    text = stringResource(R.string.profile_chip_activate),
                    onClick = {
                        onActivate(profile.id)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profiles-detail-activate"),
                )
            } else {
                Text(
                    text = stringResource(R.string.profile_chip_active),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AppSecondaryButton(
                text = stringResource(R.string.profile_button_open_schedule),
                onClick = {
                    onOpenSchedule(profile.id)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profiles-detail-open-schedule"),
            )

            AppSecondaryButton(
                text = stringResource(R.string.profile_button_edit_profile),
                onClick = {
                    onEditProfile(profile.id)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profiles-detail-edit"),
            )

            if (!profile.isSelfProfile) {
                AppSecondaryButton(
                    text = stringResource(R.string.profile_button_manage_sharing),
                    onClick = {
                        onOpenSharedProfile(profile.id)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profiles-detail-share"),
                )
            }

            if (!profile.isSelfProfile && !profile.isShared) {
                AppSecondaryButton(
                    text = stringResource(R.string.action_delete),
                    onClick = {
                        onRequestDeleteProfile(profile.id)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profiles-detail-delete"),
                )
            }
        }
    }
}
