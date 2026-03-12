package com.quran.tathbeet.ui.features.review

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.TitledCardSection
import com.quran.tathbeet.ui.model.asString
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ReviewProgressCard(
    progress: ReviewProgressCardUiState,
) {
    TitledCardSection(
        title = stringResource(R.string.review_progress_title),
        tone = AppCardTone.Muted,
        modifier = Modifier.testTag("review-progress-card"),
    ) {
        Text(
            text = stringResource(
                R.string.review_progress_ratio,
                progress.completedText,
                progress.totalText,
            ),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.review_progress_completed_label),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { progress.progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = TathbeetTokens.spacing.x1Half),
        )
        Text(
            text = stringResource(R.string.review_progress_remaining, progress.remainingText),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

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
                .align(AbsoluteAlignment.TopRight),
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
            modifier = Modifier.align(AbsoluteAlignment.TopLeft),
        )
    }
}

@Composable
fun ReviewTaskRow(
    task: ReviewTaskUiState,
    onCompleteReview: () -> Unit,
    onUpdateRating: (Int) -> Unit,
    onLaunchTaskReading: () -> Unit,
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
            verticalAlignment = Alignment.Top,
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
                        onUpdateRating = onUpdateRating,
                    )
                }
            }
            Column(
                horizontalAlignment = AbsoluteAlignment.Left,
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
            ) {
                task.readingTarget?.let {
                    ReviewLaunchIconButton(
                        onClick = onLaunchTaskReading,
                        modifier = Modifier.testTag("review-launch-${task.id}"),
                    )
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
                            textAlign = TextAlign.Center,
                        )
                    }
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
    onUpdateRating: (Int) -> Unit,
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
                OutlinedButton(
                    onClick = { onUpdateRating(index + 1) },
                    modifier = Modifier.testTag("review-inline-rating-$taskId-${index + 1}"),
                    contentPadding = PaddingValues(all = TathbeetTokens.spacing.half),
                    shape = RoundedCornerShape(TathbeetTokens.radii.sm),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = ButtonDefaults.outlinedButtonBorder().brush,
                    ),
                ) {
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
        }
    }
}

@Composable
fun ReviewCycleCompleteDialog(
    onRestartCycle: () -> Unit,
    onDismiss: () -> Unit,
) {
    ReviewConfirmationDialog(
        titleRes = R.string.review_cycle_complete_title,
        bodyRes = R.string.review_cycle_complete_body,
        confirmRes = R.string.review_cycle_restart,
        dismissRes = R.string.review_cycle_dismiss,
        onConfirm = onRestartCycle,
        onDismiss = onDismiss,
    )
}

@Composable
fun ReviewCycleResetWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ReviewConfirmationDialog(
        titleRes = R.string.review_cycle_reset_title,
        bodyRes = R.string.review_cycle_reset_body,
        confirmRes = R.string.review_cycle_reset_confirm,
        dismissRes = R.string.review_cycle_reset_cancel,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ReviewConfirmationDialog(
    titleRes: Int,
    bodyRes: Int,
    confirmRes: Int,
    dismissRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(titleRes))
        },
        text = {
            Text(text = stringResource(bodyRes))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(confirmRes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(dismissRes))
            }
        },
    )
}

@Composable
fun ReviewExternalQuranDialog(
    dialog: ReviewExternalQuranDialogUiState,
    onDismiss: () -> Unit,
    onInstallQuranAndroid: () -> Unit,
    onOpenOnWeb: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.review_external_quran_dialog_title))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
            ) {
                Text(text = stringResource(R.string.review_external_quran_dialog_body))
                Text(
                    text = dialog.taskTitle.asString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onInstallQuranAndroid) {
                Text(text = stringResource(R.string.review_external_quran_install_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onOpenOnWeb) {
                Text(text = stringResource(R.string.review_external_quran_web_action))
            }
        },
    )
}
