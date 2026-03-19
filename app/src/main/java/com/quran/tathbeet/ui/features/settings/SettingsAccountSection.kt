package com.quran.tathbeet.ui.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.BuildConfig
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton

@Composable
fun SettingsAccountCard(
    account: SettingsAccountUiState,
    onRequestEmailLink: () -> Unit,
    onSignOut: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = when (account.status) {
                    AuthSessionStatus.SignedOut -> stringResource(R.string.settings_account_state_signed_out)
                    AuthSessionStatus.LinkSent -> stringResource(R.string.settings_account_state_link_sent)
                    AuthSessionStatus.SignedIn -> stringResource(R.string.settings_account_state_signed_in)
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = when {
                    !account.isRuntimeConfigured -> stringResource(R.string.settings_account_setup_required)
                    account.status == AuthSessionStatus.SignedIn -> stringResource(
                        R.string.settings_account_email_value,
                        account.email.orEmpty(),
                    )
                    account.status == AuthSessionStatus.LinkSent -> stringResource(
                        R.string.settings_account_pending_value,
                        account.pendingEmail.orEmpty(),
                    )
                    else -> stringResource(R.string.settings_account_body)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (BuildConfig.DEBUG) {
                account.debugSyncedProfileId?.let { debugSyncedProfileId ->
                    Text(
                        text = stringResource(
                            R.string.settings_account_debug_sync_profile,
                            debugSyncedProfileId,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("settings-debug-sync-profile-id"),
                    )
                }
            }
            if (account.status == AuthSessionStatus.SignedIn) {
                AppSecondaryButton(
                    text = stringResource(R.string.settings_account_sign_out),
                    onClick = onSignOut,
                    modifier = Modifier.testTag("settings-account-sign-out"),
                )
            } else {
                AppPrimaryButton(
                    text = stringResource(R.string.settings_account_request_link),
                    onClick = onRequestEmailLink,
                    modifier = Modifier.testTag("settings-account-request-link"),
                )
            }
        }
    }
}

@Composable
fun EmailLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_account_dialog_title)) },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings-account-email-input"),
                label = { Text(text = stringResource(R.string.settings_account_email_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            AppPrimaryButton(
                text = stringResource(R.string.settings_account_send_link),
                onClick = { onConfirm(email) },
                modifier = Modifier.testTag("settings-account-send-link"),
                enabled = email.isNotBlank(),
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
