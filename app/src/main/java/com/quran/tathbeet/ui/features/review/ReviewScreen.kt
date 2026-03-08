package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.model.AppProfile
import com.quran.tathbeet.ui.model.TextSpec
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.model.dailyProgress

@Composable
fun ReviewScreen(
    profile: AppProfile,
    completionRate: Int,
    onToggleTask: (String) -> Unit,
) {
    val remainingCount = profile.reviewTasks.count { !it.isDone }
    val rolloverCount = profile.reviewTasks.count { it.isRollover && !it.isDone }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ReviewSummaryCard(
            remainingCount = remainingCount,
            rolloverCount = rolloverCount,
            progress = profile.dailyProgress,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(profile.reviewTasks, key = { it.id }) { task ->
                ReviewTaskCard(
                    title = task.title,
                    detail = task.detail,
                    isDone = task.isDone,
                    isRollover = task.isRollover,
                    onToggle = { onToggleTask(task.id) },
                )
            }
        }

        ReviewStatusCard(
            completionRate = completionRate,
            isComplete = profile.reviewTasks.all { it.isDone },
        )
    }
}

@Composable
private fun ReviewSummaryCard(
    remainingCount: Int,
    rolloverCount: Int,
    progress: Float,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.review_summary_remaining, remainingCount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (rolloverCount > 0) {
                Text(
                    text = stringResource(R.string.review_summary_rollover, rolloverCount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
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
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title.asString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = detail.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { onToggle() },
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
private fun ReviewStatusCard(
    completionRate: Int,
    isComplete: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
        }
    }
}
