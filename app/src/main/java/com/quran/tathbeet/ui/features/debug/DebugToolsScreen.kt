package com.quran.tathbeet.ui.features.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.TitledCardSection
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun DebugToolsScreen(
    onOpenLocalNotifications: () -> Unit,
    onOpenUiCatalog: () -> Unit,
) {
    ScreenLayout(
        title = stringResource(R.string.debug_tools_title),
        subtitle = stringResource(R.string.debug_tools_subtitle),
    ) {
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
