package com.quran.tathbeet.ui.features.debug

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.quran.tathbeet.R
import com.quran.tathbeet.app.ReminderHadithCatalog
import com.quran.tathbeet.app.ReminderNotificationContent
import com.quran.tathbeet.app.ReminderNotificationDebugScenario
import com.quran.tathbeet.app.ReminderNotificationMotivation
import com.quran.tathbeet.app.ReminderNotificationTaskPreview
import com.quran.tathbeet.app.ReminderNotificationTemplates
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.components.TitledCardSection
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun LocalNotificationsDebugScreen(
    sampleProfileName: String,
    sampleTask: ReminderNotificationTaskPreview?,
    reminderScenarios: List<ReminderNotificationDebugScenario>,
    onTriggerReminder: (ReminderNotificationDebugScenario) -> Unit,
) {
    ScreenLayout(
        title = stringResource(R.string.debug_notifications_title),
        subtitle = stringResource(R.string.debug_notifications_subtitle),
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
                sampleTask = sampleTask,
                onTrigger = { onTriggerReminder(scenario) },
            )
        }
    }
}

@Composable
private fun ReminderScenarioCard(
    scenario: ReminderNotificationDebugScenario,
    sampleProfileName: String,
    sampleTask: ReminderNotificationTaskPreview?,
    onTrigger: () -> Unit,
) {
    val context = LocalContext.current
    val preview = previewCopy(
        context = context,
        scenario = scenario,
        sampleProfileName = sampleProfileName,
        sampleTask = sampleTask,
    )

    TitledCardSection(
        title = stringResource(scenario.labelResId),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
        ) {
            Text(
                text = preview.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = preview.body,
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

private fun previewCopy(
    context: Context,
    scenario: ReminderNotificationDebugScenario,
    sampleProfileName: String,
    sampleTask: ReminderNotificationTaskPreview?,
) = ReminderNotificationContent.buildCopy(
    templates = ReminderNotificationTemplates(
        generalTitle = context.getString(R.string.reminder_notification_title),
        namedTitle = context.getString(R.string.reminder_notification_title_for_profile, sampleProfileName),
        taskTitle = context.getString(R.string.reminder_notification_title_with_task),
        fallbackTodayBody = context.getString(R.string.reminder_notification_body_today),
        fallbackRolloverBody = context.getString(R.string.reminder_notification_body_rollover),
        nextTaskTodayBody = context.getString(R.string.reminder_notification_body_next_task),
        nextTaskRolloverBody = context.getString(R.string.reminder_notification_body_next_task_rollover),
        motivationBody = context.getString(R.string.reminder_notification_body_with_motivation),
    ),
    includeProfileName = scenario.includeProfileName,
    nextTask = sampleTask,
    hasRollover = scenario.hasRollover,
    motivation = if (scenario.includesMotivation) {
        ReminderHadithCatalog.notificationEntries[scenario.motivationalEntryIndex].let { hadith ->
            ReminderNotificationMotivation(
                text = context.getString(hadith.textResId),
                source = context.getString(hadith.sourceResId),
            )
        }
    } else {
        null
    },
)
