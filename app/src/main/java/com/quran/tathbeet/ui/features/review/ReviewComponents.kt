package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ReviewSectionHeader(
    section: ReviewSectionUiState,
) {
    val sectionDone = section.tasks.all { it.isDone }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TathbeetTokens.spacing.x1Half, bottom = TathbeetTokens.spacing.x1)
            .testTag("review-section-header-${section.id}"),
    ) {
        Text(
            text = section.title.asString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopStart),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = if (sectionDone) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
            contentDescription = section.status.asString(),
            tint = if (sectionDone) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
fun ReviewTaskRow(
    task: ReviewTaskUiState,
    onCompleteReview: () -> Unit,
    onEditRating: () -> Unit,
) {
    val taskTitle = task.title.asString()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("review-task-${task.id}")
            .padding(vertical = TathbeetTokens.spacing.x1),
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
                    text = taskTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = task.detail.asString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (task.isDone && task.rating != null) {
                    CompletedTaskMeta(
                        taskId = task.id,
                        rating = task.rating,
                        onEditRating = onEditRating,
                    )
                }
            }
            if (task.isDone) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = taskTitle,
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                OutlinedButton(
                    onClick = onCompleteReview,
                    modifier = Modifier.testTag("review-complete-${task.id}"),
                    shape = RoundedCornerShape(TathbeetTokens.radii.pill),
                    contentPadding = PaddingValues(
                        horizontal = TathbeetTokens.spacing.x2,
                        vertical = TathbeetTokens.spacing.x1,
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(),
                ) {
                    Text(
                        text = stringResource(R.string.review_mark_done),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = TathbeetTokens.spacing.x1Half),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        )
    }
}

@Composable
private fun CompletedTaskMeta(
    taskId: String,
    rating: Int,
    onEditRating: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .testTag("review-completed-rating-$taskId-$rating")
                .semantics {
                    contentDescription = "rating-$rating"
                },
            horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.half),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (index < rating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                )
            }
        }
        TextButton(
            onClick = onEditRating,
            modifier = Modifier.testTag("review-edit-rating-$taskId"),
        ) {
            Text(text = stringResource(R.string.review_edit_rating))
        }
    }
}

@Composable
fun ReviewRatingDialog(
    selectedRating: Int,
    onSelectRating: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.review_rating_dialog_title))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x2),
            ) {
                Text(text = stringResource(R.string.review_rating_value, selectedRating))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    (1..5).forEach { rating ->
                        Icon(
                            imageVector = if (rating <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = stringResource(R.string.review_rating_value, rating),
                            tint = if (rating <= selectedRating) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier
                                .testTag("review-rating-$rating")
                                .clickable { onSelectRating(rating) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("review-rating-dismiss"),
            ) {
                Text(text = stringResource(R.string.action_close))
            }
        },
    )
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
