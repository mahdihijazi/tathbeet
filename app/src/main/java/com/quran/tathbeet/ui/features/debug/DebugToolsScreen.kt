package com.quran.tathbeet.ui.features.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.TitledCardSection
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun DebugToolsScreen(
    authLink: String,
    onAuthLinkChanged: (String) -> Unit,
    onCopyAuthLink: () -> Unit,
    onOpenAuthLink: () -> Unit,
    onOpenLocalNotifications: () -> Unit,
    onOpenUiCatalog: () -> Unit,
) {
    ScreenLayout(
        title = stringResource(R.string.debug_tools_title),
        subtitle = stringResource(R.string.debug_tools_subtitle),
    ) {
        item {
            AuthLinkDebugCard(
                authLink = authLink,
                onAuthLinkChanged = onAuthLinkChanged,
                onCopyAuthLink = onCopyAuthLink,
                onOpenAuthLink = onOpenAuthLink,
            )
        }

        item {
            DebugEntryCard(
                title = stringResource(R.string.debug_tools_local_notifications_title),
                subtitle = stringResource(R.string.debug_tools_local_notifications_subtitle),
                action = stringResource(R.string.debug_tools_local_notifications_action),
                testTag = "debug-open-local-notifications",
                onOpen = onOpenLocalNotifications,
            )
        }

        item {
            DebugEntryCard(
                title = stringResource(R.string.debug_tools_ui_catalog_title),
                subtitle = stringResource(R.string.debug_tools_ui_catalog_subtitle),
                action = stringResource(R.string.debug_tools_ui_catalog_action),
                testTag = "debug-open-ui-catalog",
                onOpen = onOpenUiCatalog,
            )
        }
    }
}

@Composable
private fun AuthLinkDebugCard(
    authLink: String,
    onAuthLinkChanged: (String) -> Unit,
    onCopyAuthLink: () -> Unit,
    onOpenAuthLink: () -> Unit,
) {
    TitledCardSection(
        title = stringResource(R.string.debug_tools_auth_link_title),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
        ) {
            Text(
                text = stringResource(R.string.debug_tools_auth_link_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = authLink,
                onValueChange = onAuthLinkChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debug-auth-link-input"),
                label = {
                    Text(stringResource(R.string.debug_tools_auth_link_label))
                },
                placeholder = {
                    Text(stringResource(R.string.debug_tools_auth_link_placeholder))
                },
                minLines = 2,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
            ) {
                AppSecondaryButton(
                    text = stringResource(R.string.debug_tools_auth_link_copy_action),
                    onClick = onCopyAuthLink,
                    enabled = authLink.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("debug-copy-auth-link"),
                )
                AppPrimaryButton(
                    text = stringResource(R.string.debug_tools_auth_link_open_action),
                    onClick = onOpenAuthLink,
                    enabled = authLink.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("debug-open-auth-link"),
                )
            }
        }
    }
}

@Composable
private fun DebugEntryCard(
    title: String,
    subtitle: String,
    action: String,
    testTag: String,
    onOpen: () -> Unit,
) {
    TitledCardSection(
        title = title,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
        ) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppPrimaryButton(
                text = action,
                onClick = onOpen,
                modifier = Modifier.testTag(testTag),
            )
        }
    }
}
