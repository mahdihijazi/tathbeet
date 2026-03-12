package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.theme.TathbeetTheme
import com.quran.tathbeet.ui.theme.TathbeetTokens

private const val ReviewPreviewWidth = 411
private const val ReviewHeaderHeight = 120
private const val ReviewRowHeight = 220
private const val ReviewProgressHeight = 240

@PreviewTest
@Preview(
    name = "review_progress_card_in_progress",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewProgressHeight,
    showBackground = true,
)
@Composable
fun ReviewProgressCardInProgressScreenshot() {
    ReviewScreenshotBox {
        ReviewProgressCard(
            progress = ReviewMockFactory.initialState().toUiState().progressCard
                ?: error("Expected progress card"),
        )
    }
}

@PreviewTest
@Preview(
    name = "review_progress_card_complete",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewProgressHeight,
    showBackground = true,
)
@Composable
fun ReviewProgressCardCompleteScreenshot() {
    ReviewScreenshotBox {
        ReviewProgressCard(
            progress = ReviewProgressCardUiState(
                completedText = "1",
                totalText = "1",
                remainingText = "0",
                progress = 1f,
            ),
        )
    }
}

@PreviewTest
@Preview(
    name = "review_section_header_done",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewHeaderHeight,
    showBackground = true,
)
@Composable
fun ReviewSectionHeaderDoneScreenshot() {
    ReviewScreenshotBox {
        ReviewSectionHeader(
            section = ReviewMockFactory.initialState().toUiState().sections[0],
        )
    }
}

@PreviewTest
@Preview(
    name = "review_section_header_available",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewHeaderHeight,
    showBackground = true,
)
@Composable
fun ReviewSectionHeaderAvailableScreenshot() {
    ReviewScreenshotBox {
        ReviewSectionHeader(
            section = ReviewMockFactory.initialState().toUiState().sections[2],
        )
    }
}

@PreviewTest
@Preview(
    name = "review_task_row_completed",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowCompletedScreenshot() {
    ReviewScreenshotBox {
        ReviewTaskRow(
            task = ReviewMockFactory.initialState().toUiState().sections[1].tasks[0],
            onCompleteReview = {},
            onUpdateRating = {},
            onLaunchTaskReading = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "review_task_row_pending",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowPendingScreenshot() {
    ReviewScreenshotBox {
        ReviewTaskRow(
            task = ReviewMockFactory.initialState().toUiState().sections[2].tasks[0],
            onCompleteReview = {},
            onUpdateRating = {},
            onLaunchTaskReading = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "review_task_row_completed_default_rating",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowCompletedDefaultRatingScreenshot() {
    ReviewScreenshotBox {
        ReviewTaskRow(
            task = ReviewTaskUiState(
                id = "default-rated-task",
                title = com.quran.tathbeet.ui.model.TextSpec(rawText = "من الفاتحة إلى البقرة"),
                detail = com.quran.tathbeet.ui.model.TextSpec(rawText = "ربع الحزب 1 · من الفاتحة 1 إلى البقرة 25"),
                isDone = true,
                rating = 3,
                defaultRating = 3,
            ),
            onCompleteReview = {},
            onUpdateRating = {},
            onLaunchTaskReading = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "review_cycle_complete_dialog",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = 320,
    showBackground = true,
)
@Composable
fun ReviewCycleCompleteDialogScreenshot() {
    ReviewScreenshotBox {
        ReviewCycleCompleteDialog(
            onRestartCycle = {},
            onDismiss = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "review_cycle_reset_warning_dialog",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = 320,
    showBackground = true,
)
@Composable
fun ReviewCycleResetWarningDialogScreenshot() {
    ReviewScreenshotBox {
        ReviewCycleResetWarningDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Composable
private fun ReviewScreenshotBox(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TathbeetTokens.spacing.x3),
            ) {
                content()
            }
        }
    }
}
