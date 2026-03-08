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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppCard
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.AppPill
import com.quran.tathbeet.ui.components.CardSection
import com.quran.tathbeet.ui.model.AppProfile
import com.quran.tathbeet.ui.model.TextSpec
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.model.dailyProgress
import com.quran.tathbeet.ui.theme.TathbeetTokens

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
            .padding(
                horizontal = TathbeetTokens.spacing.x2Half,
                vertical = TathbeetTokens.spacing.x2Half,
            ),
        verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x2),
    ) {
        ReviewSummaryCard(
            remainingCount = remainingCount,
            rolloverCount = rolloverCount,
            progress = profile.dailyProgress,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = TathbeetTokens.spacing.x1),
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
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
    CardSection(tone = AppCardTone.Highlight) {
        Text(
            text = stringResource(R.string.review_summary_remaining, remainingCount),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (rolloverCount > 0) {
            AppPill(text = stringResource(R.string.review_summary_rollover, rolloverCount))
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
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
    AppCard(
        onClick = onToggle,
        tone = if (isRollover) AppCardTone.Accent else AppCardTone.Default,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TathbeetTokens.spacing.x2Half),
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.half),
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
                AppPill(text = stringResource(R.string.review_rollover_chip))
            }
        }
    }
}

@Composable
private fun ReviewStatusCard(
    completionRate: Int,
    isComplete: Boolean,
) {
    CardSection(tone = AppCardTone.Muted) {
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
