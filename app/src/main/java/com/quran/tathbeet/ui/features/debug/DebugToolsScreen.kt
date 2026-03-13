package com.quran.tathbeet.ui.features.debug

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.items
import com.quran.tathbeet.R
import com.quran.tathbeet.app.ReminderHadithCatalog
import com.quran.tathbeet.app.ReminderNotificationDebugScenario
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.components.TitledCardSection

@Composable
fun DebugToolsScreen(
    sampleProfileName: String,
    reminderScenarios: List<ReminderNotificationDebugScenario>,
    onTriggerReminder: (ReminderNotificationDebugScenario) -> Unit,
) {
    ScreenLayout(
        title = stringResource(R.string.debug_tools_title),
        subtitle = stringResource(R.string.debug_tools_subtitle),
    ) {
        item {
            SectionHeader(
                title = stringResource(R.string.debug_notifications_title),
                subtitle = stringResource(R.string.debug_notifications_subtitle),
            )
        }

        items(reminderScenarios, key = { it.id }) { scenario ->
            ReminderScenarioCard(
                scenario = scenario,
                sampleProfileName = sampleProfileName,
                onTrigger = { onTriggerReminder(scenario) },
            )
        }
    }
}

@Composable
private fun ReminderScenarioCard(
    scenario: ReminderNotificationDebugScenario,
    sampleProfileName: String,
    onTrigger: () -> Unit,
) {
    val context = LocalContext.current
    TitledCardSection(
        title = stringResource(scenario.labelResId),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(com.quran.tathbeet.ui.theme.TathbeetTokens.spacing.x1)) {
            Text(
                text = previewTitle(
                    context = context,
                    scenario = scenario,
                    sampleProfileName = sampleProfileName,
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = previewBody(
                    context = context,
                    scenario = scenario,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppPrimaryButton(
                text = stringResource(R.string.debug_notifications_trigger),
                onClick = onTrigger,
                modifier = Modifier.testTag("debug-notification-trigger-${scenario.id}"),
            )
        }
    }
}

private fun previewTitle(
    context: Context,
    scenario: ReminderNotificationDebugScenario,
    sampleProfileName: String,
): String =
    if (scenario.includeProfileName) {
        context.getString(R.string.reminder_notification_title_for_profile, sampleProfileName)
    } else {
        context.getString(R.string.reminder_notification_title)
    }

private fun previewBody(
    context: Context,
    scenario: ReminderNotificationDebugScenario,
): String {
    val baseBody = context.getString(
        if (scenario.hasRollover) {
            R.string.reminder_notification_body_rollover
        } else {
            R.string.reminder_notification_body_today
        },
    )
    if (!scenario.includesMotivation) {
        return baseBody
    }

    val hadith = ReminderHadithCatalog.notificationEntries[scenario.motivationalEntryIndex]
    return context.getString(
        R.string.reminder_notification_body_with_motivation,
        baseBody,
        context.getString(hadith.textResId),
        context.getString(hadith.sourceResId),
    )
}
