package com.quran.tathbeet.ui.features.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ProfilesScreen(
    uiState: ProfilesUiState,
    onProfileSelected: (String) -> Unit,
    onProfileNotificationsToggled: (String) -> Unit,
    onAddProfileRequested: () -> Unit,
    onEditActiveProfileRequested: () -> Unit,
    onOpenSharedProfile: (String) -> Unit,
    onProfileNameChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onDismissProfileDialog: () -> Unit,
    onRequestDeleteProfile: () -> Unit,
    onDismissDeleteProfile: () -> Unit,
    onConfirmDeleteProfile: () -> Unit,
    onOpenSchedule: () -> Unit,
) {
    val activeProfile = uiState.activeProfile ?: return

    ScreenLayout(
        title = stringResource(R.string.profile_screen_title),
        subtitle = stringResource(R.string.profile_screen_subtitle),
    ) {
        item {
            HeroCard(
                eyebrow = stringResource(R.string.profile_active_eyebrow),
                title = activeProfile.name,
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
                    AppSecondaryButton(
                        text = stringResource(R.string.profile_button_edit_profile),
                        onClick = onEditActiveProfileRequested,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profiles-edit-active"),
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.profile_section_title),
            )
        }

        items(uiState.profiles, key = { it.id }) { profile ->
            ProfileCard(
                profile = profile,
                onSelected = { onProfileSelected(profile.id) },
                onToggleNotifications = { onProfileNotificationsToggled(profile.id) },
                onOpenSharedProfile = onOpenSharedProfile,
            )
        }

        item {
            AppPrimaryButton(
                text = stringResource(R.string.profile_add_child),
                onClick = onAddProfileRequested,
                modifier = Modifier.testTag("profiles-add-button"),
            )
        }
    }

    uiState.editor?.let { editor ->
        ProfileEditorDialog(
            editor = editor,
            onNameChanged = onProfileNameChanged,
            onSave = onSaveProfile,
            onDismiss = onDismissProfileDialog,
            onRequestDelete = onRequestDeleteProfile,
        )
    }

    uiState.deleteConfirmation?.let { confirmation ->
        ProfileDeleteDialog(
            confirmation = confirmation,
            onDismiss = onDismissDeleteProfile,
            onConfirm = onConfirmDeleteProfile,
        )
    }
}

@Composable
private fun ProfileCard(
    profile: ProfileCardUiState,
    onSelected: () -> Unit,
    onToggleNotifications: () -> Unit,
    onOpenSharedProfile: (String) -> Unit,
) {
    val scheduleValue = profile.paceLabelRes?.let { labelRes ->
        stringResource(labelRes)
    } ?: stringResource(R.string.profile_schedule_missing)

    AppCard(
        modifier = Modifier.testTag("profiles-card-${profile.id}"),
        onClick = onSelected,
    ) {
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
                }
                AppSelectionChip(
                    selected = profile.isActive,
                    onClick = onSelected,
                    text = if (profile.isActive) {
                        stringResource(R.string.profile_chip_active)
                    } else {
                        stringResource(R.string.profile_chip_activate)
                    },
                )
            }
            AppKeyValueRow(
                label = stringResource(R.string.profile_goal),
                value = scheduleValue,
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
                    text = if (profile.paceLabelRes == null) {
                        stringResource(R.string.profile_today_tasks_empty)
                    } else {
                        stringResource(
                            R.string.profile_today_tasks_done,
                            profile.todayCompletedCount,
                            profile.todayTotalCount,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.profile_notifications_toggle),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = profile.notificationsEnabled,
                        onCheckedChange = { onToggleNotifications() },
                        modifier = Modifier.testTag("profiles-notifications-${profile.id}"),
                    )
                }
            }
            if (!profile.isSelfProfile) {
                AppSecondaryButton(
                    text = stringResource(R.string.profile_button_manage_sharing),
                    onClick = {
                        onSelected()
                        onOpenSharedProfile(profile.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profiles-open-shared-${profile.id}"),
                )
            }
        }
    }
}

@Composable
private fun ProfileEditorDialog(
    editor: ProfileEditorUiState,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    if (editor.isNew) {
                        R.string.profile_dialog_add_title
                    } else {
                        R.string.profile_dialog_edit_title
                    },
                ),
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x2),
            ) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = onNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profiles-editor-name-input"),
                    label = {
                        Text(text = stringResource(R.string.schedule_profile_name_label))
                    },
                    singleLine = true,
                )
                if (editor.canDelete) {
                    AppSecondaryButton(
                        text = stringResource(R.string.action_delete),
                        onClick = onRequestDelete,
                        modifier = Modifier.testTag("profiles-editor-delete"),
                    )
                }
            }
        },
        confirmButton = {
            AppSecondaryButton(
                text = stringResource(
                    if (editor.isNew) {
                        R.string.action_create
                    } else {
                        R.string.action_save
                    },
                ),
                onClick = onSave,
                modifier = Modifier.testTag("profiles-editor-save"),
                enabled = editor.name.isNotBlank(),
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
private fun ProfileDeleteDialog(
    confirmation: ProfileDeleteConfirmationUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.profile_dialog_delete_title))
        },
        text = {
            Text(
                text = stringResource(R.string.profile_dialog_delete_body, confirmation.profileName),
            )
        },
        confirmButton = {
            AppSecondaryButton(
                text = stringResource(R.string.action_delete),
                onClick = onConfirm,
                modifier = Modifier.testTag("profiles-delete-confirm"),
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
