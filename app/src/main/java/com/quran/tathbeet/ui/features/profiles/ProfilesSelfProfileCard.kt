package com.quran.tathbeet.ui.features.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.ui.components.AppCard
import com.quran.tathbeet.ui.components.AppKeyValueRow
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.AppSelectionChip
import com.quran.tathbeet.ui.components.AppStatusBadge
import com.quran.tathbeet.ui.components.AppStatusBadgeTone
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
internal fun ProfilesSelfProfileCard(
    profile: ProfileCardUiState,
    account: ProfilesAccountUiState,
    onActivate: () -> Unit,
    onOpenDetails: () -> Unit,
    onToggleNotifications: () -> Unit,
    onRequestEmailLink: () -> Unit,
    onSignOut: () -> Unit,
) {
    val statusText = when (account.status) {
        AuthSessionStatus.SignedOut -> stringResource(R.string.profile_account_state_signed_out)
        AuthSessionStatus.LinkSent -> stringResource(R.string.profile_account_state_link_sent)
        AuthSessionStatus.SignedIn -> stringResource(R.string.profile_account_state_signed_in)
    }
    val detailText = when {
        !account.isRuntimeConfigured -> stringResource(R.string.profile_account_setup_required)
        account.status == AuthSessionStatus.SignedIn -> stringResource(
            R.string.settings_account_email_value,
            account.email.orEmpty(),
        )
        account.status == AuthSessionStatus.LinkSent -> stringResource(
            R.string.settings_account_pending_value,
            account.pendingEmail.orEmpty(),
        )
        else -> stringResource(R.string.profile_account_body_signed_out)
    }
    val syncStatusText = when {
        !account.isRuntimeConfigured -> null
        account.status == AuthSessionStatus.SignedIn -> stringResource(R.string.profile_account_sync_active)
        else -> stringResource(R.string.profile_account_sync_paused)
    }
    val statusTone = when (account.status) {
        AuthSessionStatus.SignedOut -> AppStatusBadgeTone.Neutral
        AuthSessionStatus.LinkSent -> AppStatusBadgeTone.Highlight
        AuthSessionStatus.SignedIn -> AppStatusBadgeTone.Accent
    }
    val scheduleValue = profile.paceLabelRes?.let { labelRes ->
        stringResource(labelRes)
    } ?: stringResource(R.string.profile_schedule_missing)

    AppCard(
        modifier = Modifier.testTag("profiles-card-${profile.id}"),
        onClick = onOpenDetails,
    ) {
        Column(
            modifier = Modifier.padding(TathbeetTokens.spacing.x2Half),
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.half)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.profile_self_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AppStatusBadge(
                            text = statusText,
                            tone = statusTone,
                        )
                        if (profile.isShared) {
                            AppStatusBadge(
                                text = stringResource(R.string.profile_sync_shared),
                                tone = AppStatusBadgeTone.Accent,
                            )
                        }
                    }
                }
                AppSelectionChip(
                    selected = profile.isActive,
                    onClick = onActivate,
                    text = if (profile.isActive) {
                        stringResource(R.string.profile_chip_active)
                    } else {
                        stringResource(R.string.profile_chip_activate)
                    },
                    modifier = Modifier.testTag("profiles-self-activate"),
                )
            }

            Text(
                text = detailText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            syncStatusText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
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
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.profile_notifications_toggle),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    androidx.compose.material3.Switch(
                        checked = profile.notificationsEnabled,
                        onCheckedChange = { onToggleNotifications() },
                        modifier = Modifier.testTag("profiles-notifications-${profile.id}"),
                    )
                }
            }

            if (account.status == AuthSessionStatus.SignedIn) {
                AppSecondaryButton(
                    text = stringResource(R.string.profile_button_manage_account),
                    onClick = onOpenDetails,
                    modifier = Modifier.testTag("profiles-self-manage-account"),
                )
            }

            if (account.status == AuthSessionStatus.SignedIn) {
                AppSecondaryButton(
                    text = stringResource(R.string.settings_account_sign_out),
                    onClick = onSignOut,
                    modifier = Modifier.testTag("profiles-account-sign-out"),
                )
            } else {
                AppPrimaryButton(
                    text = stringResource(R.string.settings_account_request_link),
                    onClick = onRequestEmailLink,
                    modifier = Modifier.testTag("profiles-account-request-link"),
                )
            }
        }
    }
}
