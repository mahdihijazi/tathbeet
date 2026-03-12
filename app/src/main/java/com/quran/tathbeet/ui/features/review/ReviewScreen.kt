package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ReviewScreen(
    uiState: ReviewUiState,
    onRequestTaskCompletion: (String) -> Unit,
    onUpdateTaskRating: (String, Int) -> Unit,
    onLaunchTaskReading: (String) -> Unit,
    onRestartCycle: () -> Unit,
    onDismissCycleResetWarning: () -> Unit,
    onDismissCycleResetDialog: () -> Unit,
    onDismissExternalQuranDialog: () -> Unit,
    onOpenQuranAndroidInstall: () -> Unit,
    onOpenQuranOnWeb: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("review-sections-list")
            .padding(
                horizontal = TathbeetTokens.spacing.x2Half,
                vertical = TathbeetTokens.spacing.x2Half,
            ),
        contentPadding = PaddingValues(bottom = TathbeetTokens.spacing.x1),
        verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x2),
    ) {
        uiState.progressCard?.let { progressCard ->
            item(key = "review-progress-card") {
                ReviewProgressCard(progress = progressCard)
            }
        }
        uiState.sections.forEach { section ->
            item(key = "${section.id}-header") {
                ReviewSectionHeader(section = section)
            }
            items(
                items = section.tasks,
                key = { task -> task.id },
            ) { task ->
                ReviewTaskRow(
                    task = task,
                    onCompleteReview = { onRequestTaskCompletion(task.id) },
                    onUpdateRating = { rating -> onUpdateTaskRating(task.id, rating) },
                    onLaunchTaskReading = { onLaunchTaskReading(task.id) },
                )
            }
        }
    }

    if (uiState.showCycleResetDialog) {
        ReviewCycleCompleteDialog(
            onRestartCycle = onRestartCycle,
            onDismiss = onDismissCycleResetDialog,
        )
    }

    if (uiState.showCycleResetWarningDialog) {
        ReviewCycleResetWarningDialog(
            onConfirm = onRestartCycle,
            onDismiss = onDismissCycleResetWarning,
        )
    }

    uiState.externalQuranDialog?.let { dialog ->
        ReviewExternalQuranDialog(
            dialog = dialog,
            onDismiss = onDismissExternalQuranDialog,
            onInstallQuranAndroid = onOpenQuranAndroidInstall,
            onOpenOnWeb = onOpenQuranOnWeb,
        )
    }
}
