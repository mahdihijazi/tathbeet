package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.HeroCard
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.model.AppProfile
import com.quran.tathbeet.ui.model.TextSpec
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.model.dailyProgress

@Composable
fun ReviewScreen(
    profile: AppProfile,
    completionRate: Int,
    onToggleTask: (String) -> Unit,
    onCompleteDay: () -> Unit,
    onResetDay: () -> Unit,
    onOpenSchedule: () -> Unit,
) {
    ScreenLayout(
        title = stringResource(R.string.review_title),
        subtitle = stringResource(R.string.review_subtitle),
    ) {
        item {
            HeroCard(
                eyebrow = stringResource(R.string.review_eyebrow),
                title = stringResource(
                    R.string.review_remaining_count,
                    profile.reviewTasks.count { !it.isDone },
                ),
                body = stringResource(R.string.review_remaining_body),
            ) {
                LinearProgressIndicator(
                    progress = { profile.dailyProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.review_tasks_title),
                subtitle = stringResource(R.string.review_tasks_subtitle),
            )
        }

        items(profile.reviewTasks, key = { it.id }) { task ->
            ReviewTaskCard(
                title = task.title,
                detail = task.detail,
                isDone = task.isDone,
                isRollover = task.isRollover,
                onToggle = { onToggleTask(task.id) },
            )
        }

        item {
            ReviewFooterCard(
                completionRate = completionRate,
                isComplete = profile.reviewTasks.all { it.isDone },
                onCompleteDay = onCompleteDay,
                onResetDay = onResetDay,
                onOpenSchedule = onOpenSchedule,
            )
        }
    }
}

@Composable
private fun ReviewTaskCard(
    title: TextSpec,
    detail: TextSpec,
    isDone: Boolean,
    isRollover: Boolean,
    onToggle: () -> Unit,
) {
    Card(onClick = onToggle) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggle() },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title.asString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = detail.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isRollover) {
                Text(
                    text = stringResource(R.string.review_rollover_chip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun ReviewFooterCard(
    completionRate: Int,
    isComplete: Boolean,
    onCompleteDay: () -> Unit,
    onResetDay: () -> Unit,
    onOpenSchedule: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.review_status_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.review_status_rate, completionRate),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (isComplete) {
                    stringResource(R.string.review_status_done)
                } else {
                    stringResource(R.string.review_status_open)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onCompleteDay) {
                    Text(stringResource(R.string.review_complete_remaining))
                }
                Button(onClick = onResetDay) {
                    Text(stringResource(R.string.review_reset))
                }
            }
            Button(onClick = onOpenSchedule) {
                Text(stringResource(R.string.review_adjust_schedule))
            }
        }
    }
}
