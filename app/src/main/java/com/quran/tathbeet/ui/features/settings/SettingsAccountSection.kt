package com.quran.tathbeet.ui.features.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
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
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton

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
