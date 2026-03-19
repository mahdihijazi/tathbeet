package com.quran.tathbeet.ui.features.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.sync.CloudProfileMemberRole
import com.quran.tathbeet.ui.components.BodyTextCard
import com.quran.tathbeet.ui.components.HeroCard
import com.quran.tathbeet.ui.components.InfoActionCard
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader

@Composable
fun SharedProfileScreen(
    uiState: SharedProfileUiState,
    onEnableSharing: () -> Unit,
    onInviteEditor: (String) -> Unit,
    onRemoveEditor: (String) -> Unit,
    onLeaveProfile: () -> Unit,
) {
    var showInviteDialog by remember { mutableStateOf(false) }

    ScreenLayout(
        title = stringResource(R.string.shared_title),
        subtitle = stringResource(R.string.shared_subtitle),
    ) {
        item {
            HeroCard(
                eyebrow = stringResource(R.string.shared_eyebrow),
                title = uiState.profileName,
                body = when (uiState.authStatus) {
                    AuthSessionStatus.SignedIn -> stringResource(
                        R.string.shared_body_signed_in,
                        uiState.signedInEmail.orEmpty(),
                    )
                    AuthSessionStatus.LinkSent -> stringResource(R.string.shared_body_link_sent)
                    AuthSessionStatus.SignedOut -> stringResource(R.string.shared_body_signed_out)
                },
            )
        }

        item {
            InfoActionCard(
                title = stringResource(R.string.shared_state_title),
            ) {
                Text(
                    text = stringResource(
                        R.string.shared_sync_mode,
                        stringResource(uiState.syncMode.labelRes()),
                    ),
                )
                uiState.banner?.let { banner ->
                    Text(text = stringResource(banner.labelRes()))
                }
                when {
                    uiState.canEnableSharing -> {
                        Button(onClick = onEnableSharing) {
                            Text(stringResource(R.string.shared_enable_action))
                        }
                    }
                    uiState.canInviteEditors -> {
                        Button(onClick = { showInviteDialog = true }) {
                            Text(stringResource(R.string.shared_invite_action))
                        }
                    }
                    uiState.canLeaveProfile -> {
                        OutlinedButton(onClick = onLeaveProfile) {
                            Text(stringResource(R.string.shared_leave_action))
                        }
                    }
                }
            }
        }

        if (uiState.members.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.shared_members_title),
                    subtitle = stringResource(R.string.shared_members_subtitle),
                )
            }

            items(uiState.members.size) { index ->
                val member = uiState.members[index]
                InfoActionCard(
                    title = member.email,
                ) {
                    Text(
                        text = stringResource(
                            if (member.role == CloudProfileMemberRole.Owner) {
                                R.string.shared_role_owner
                            } else {
                                R.string.shared_role_editor
                            },
                        ),
                    )
                    if (member.isCurrentUser) {
                        Text(stringResource(R.string.shared_member_you))
                    }
                    if (uiState.canInviteEditors && member.role == CloudProfileMemberRole.Editor) {
                        OutlinedButton(onClick = { onRemoveEditor(member.email) }) {
                            Text(stringResource(R.string.shared_remove_editor_action))
                        }
                    }
                }
            }
        } else {
            item {
                BodyTextCard(
                    text = stringResource(
                        if (uiState.isSelfProfile || uiState.syncMode == ProfileSyncMode.LocalOnly) {
                            R.string.shared_empty_state_local
                        } else {
                            R.string.shared_empty_state_waiting
                        },
                    ),
                )
            }
        }
    }

    if (showInviteDialog) {
        InviteEditorDialog(
            onDismiss = { showInviteDialog = false },
            onConfirm = { email ->
                onInviteEditor(email)
                showInviteDialog = false
            },
        )
    }
}

@Composable
private fun InviteEditorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shared_invite_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.shared_invite_dialog_body))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.shared_invite_email_label)) },
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onConfirm(email) }) {
                    Text(stringResource(R.string.shared_invite_confirm))
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.shared_invite_cancel))
            }
        },
    )
}

private fun ProfileSyncMode.labelRes(): Int = when (this) {
    ProfileSyncMode.LocalOnly -> R.string.shared_sync_mode_local
    ProfileSyncMode.SoloSynced -> R.string.shared_sync_mode_solo
    ProfileSyncMode.SharedOwner -> R.string.shared_sync_mode_owner
    ProfileSyncMode.SharedEditor -> R.string.shared_sync_mode_editor
}

private fun SharedProfileBanner.labelRes(): Int = when (this) {
    SharedProfileBanner.SharedEnabled -> R.string.shared_banner_enabled
    SharedProfileBanner.InviteSent -> R.string.shared_banner_invite_sent
    SharedProfileBanner.EditorRemoved -> R.string.shared_banner_editor_removed
    SharedProfileBanner.LeftProfile -> R.string.shared_banner_left_profile
    SharedProfileBanner.EditorsMustBeRemovedFirst -> R.string.shared_banner_remove_editors_first
    SharedProfileBanner.SignInRequired -> R.string.shared_banner_sign_in_required
    SharedProfileBanner.ShareUnavailable -> R.string.shared_banner_unavailable
}
