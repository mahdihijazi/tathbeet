package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ReviewSectionHeader(
    section: ReviewSectionUiState,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TathbeetTokens.spacing.x1Half, bottom = TathbeetTokens.spacing.x1)
            .testTag("review-section-header-${section.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val sectionDone = section.tasks.all { it.isDone }

        Icon(
            imageVector = if (sectionDone) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
            contentDescription = section.status.asString(),
            tint = if (sectionDone) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Text(
            text = section.title.asString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun ReviewTaskCard(
    task: ReviewTaskUiState,
    onToggle: () -> Unit,
) {
    val taskTitle = task.title.asString()
    val toggleLabel = stringResource(R.string.review_toggle_task, taskTitle)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("review-task-${task.id}")
            .padding(vertical = TathbeetTokens.spacing.x1),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = toggleLabel
                }
                .toggleable(
                    value = task.isDone,
                    role = Role.Checkbox,
                    onValueChange = { onToggle() },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.half),
            ) {
                Text(
                    text = taskTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = task.detail.asString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Checkbox(
                checked = task.isDone,
                onCheckedChange = null,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = TathbeetTokens.spacing.x1Half),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        )
    }
}

@Composable
fun ReviewCycleCompleteDialog(
    onRestartCycle: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.review_cycle_complete_title))
        },
        text = {
            Text(text = stringResource(R.string.review_cycle_complete_body))
        },
        confirmButton = {
            TextButton(onClick = onRestartCycle) {
                Text(text = stringResource(R.string.review_cycle_restart))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.review_cycle_dismiss))
            }
        },
    )
}
