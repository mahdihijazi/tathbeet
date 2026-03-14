package com.quran.tathbeet.ui.features.review

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.domain.model.QuranReadingTarget
import com.quran.tathbeet.ui.model.TextSpec
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val ReviewPreviewWidth = 411
private const val ReviewHeaderHeight = 120
private const val ReviewRowHeight = 220
private const val ReviewProgressHeight = 240
private const val DialogPreviewHeight = 320

@PreviewTest
@Preview(
    name = "review_progress_card_in_progress",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewProgressHeight,
    showBackground = true,
)
@Composable
fun ReviewProgressCardInProgressScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) { ReviewProgressCardInProgressPreview() }

@PreviewTest
@Preview(
    name = "review_progress_card_in_progress_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewProgressHeight,
    showBackground = true,
)
@Composable
fun ReviewProgressCardInProgressDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) { ReviewProgressCardInProgressPreview() }

@PreviewTest
@Preview(
    name = "review_progress_card_complete",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewProgressHeight,
    showBackground = true,
)
@Composable
fun ReviewProgressCardCompleteScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) { ReviewProgressCardCompletePreview() }

@PreviewTest
@Preview(
    name = "review_progress_card_complete_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewProgressHeight,
    showBackground = true,
)
@Composable
fun ReviewProgressCardCompleteDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) { ReviewProgressCardCompletePreview() }

@PreviewTest
@Preview(
    name = "review_section_header_done",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewHeaderHeight,
    showBackground = true,
)
@Composable
fun ReviewSectionHeaderDoneScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) { ReviewSectionHeaderDonePreview() }

@PreviewTest
@Preview(
    name = "review_section_header_done_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewHeaderHeight,
    showBackground = true,
)
@Composable
fun ReviewSectionHeaderDoneDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) { ReviewSectionHeaderDonePreview() }

@PreviewTest
@Preview(
    name = "review_section_header_available",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewHeaderHeight,
    showBackground = true,
)
@Composable
fun ReviewSectionHeaderAvailableScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) { ReviewSectionHeaderAvailablePreview() }

@PreviewTest
@Preview(
    name = "review_section_header_available_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewHeaderHeight,
    showBackground = true,
)
@Composable
fun ReviewSectionHeaderAvailableDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) { ReviewSectionHeaderAvailablePreview() }

@PreviewTest
@Preview(
    name = "review_task_row_completed",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowCompletedScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) { ReviewTaskRowCompletedPreview() }

@PreviewTest
@Preview(
    name = "review_task_row_completed_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowCompletedDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) { ReviewTaskRowCompletedPreview() }

@PreviewTest
@Preview(
    name = "review_task_row_pending",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowPendingScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) { ReviewTaskRowPendingPreview() }

@PreviewTest
@Preview(
    name = "review_task_row_pending_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowPendingDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) { ReviewTaskRowPendingPreview() }

@PreviewTest
@Preview(
    name = "review_task_row_completed_default_rating",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowCompletedDefaultRatingScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) { ReviewTaskRowCompletedDefaultRatingPreview() }

@PreviewTest
@Preview(
    name = "review_task_row_completed_default_rating_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewRowHeight,
    showBackground = true,
)
@Composable
fun ReviewTaskRowCompletedDefaultRatingDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) { ReviewTaskRowCompletedDefaultRatingPreview() }

@PreviewTest
@Preview(
    name = "review_cycle_complete_dialog",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = DialogPreviewHeight,
    showBackground = true,
)
@Composable
fun ReviewCycleCompleteDialogScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) {
        ReviewCycleCompleteDialog(
            onRestartCycle = {},
            onDismiss = {},
        )
    }

@PreviewTest
@Preview(
    name = "review_cycle_complete_dialog_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = DialogPreviewHeight,
    showBackground = true,
)
@Composable
fun ReviewCycleCompleteDialogDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) {
        ReviewCycleCompleteDialog(
            onRestartCycle = {},
            onDismiss = {},
        )
    }

@PreviewTest
@Preview(
    name = "review_cycle_reset_warning_dialog",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = DialogPreviewHeight,
    showBackground = true,
)
@Composable
fun ReviewCycleResetWarningDialogScreenshot() =
    ReviewScreenshotFrame(darkTheme = false) {
        ReviewCycleResetWarningDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }

@PreviewTest
@Preview(
    name = "review_cycle_reset_warning_dialog_dark",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = DialogPreviewHeight,
    showBackground = true,
)
@Composable
fun ReviewCycleResetWarningDialogDarkScreenshot() =
    ReviewScreenshotFrame(darkTheme = true) {
        ReviewCycleResetWarningDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }

@Composable
private fun ReviewScreenshotFrame(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    ThemedScreenshotFrame(darkTheme = darkTheme) {
        content()
    }
}

@Composable
private fun ReviewProgressCardInProgressPreview() {
    ReviewProgressCard(
        progress = ReviewMockFactory.initialState().toUiState().progressCard
            ?: error("Expected progress card"),
    )
}

@Composable
private fun ReviewProgressCardCompletePreview() {
    ReviewProgressCard(
        progress = ReviewProgressCardUiState(
            completedText = "1",
            totalText = "1",
            remainingText = "0",
            progress = 1f,
        ),
    )
}

@Composable
private fun ReviewSectionHeaderDonePreview() {
    ReviewSectionHeader(
        section = ReviewMockFactory.initialState().toUiState().sections[0],
    )
}

@Composable
private fun ReviewSectionHeaderAvailablePreview() {
    ReviewSectionHeader(
        section = ReviewMockFactory.initialState().toUiState().sections[2],
    )
}

@Composable
private fun ReviewTaskRowCompletedPreview() {
    ReviewTaskRow(
        task = ReviewMockFactory.initialState().toUiState().sections[1].tasks[0],
        onCompleteReview = {},
        onUpdateRating = {},
        onLaunchTaskReading = {},
    )
}

@Composable
private fun ReviewTaskRowPendingPreview() {
    ReviewTaskRow(
        task = ReviewMockFactory.initialState().toUiState().sections[2].tasks[0],
        onCompleteReview = {},
        onUpdateRating = {},
        onLaunchTaskReading = {},
    )
}

@Composable
private fun ReviewTaskRowCompletedDefaultRatingPreview() {
    ReviewTaskRow(
        task = ReviewTaskUiState(
            id = "default-rated-task",
            title = TextSpec(rawText = "من الفاتحة إلى البقرة"),
            detail = TextSpec(rawText = "ربع الحزب 1 · من الفاتحة 1 إلى البقرة 25"),
            isDone = true,
            rating = 3,
            defaultRating = 3,
            readingTarget = QuranReadingTarget(
                startSurahId = 1,
                startAyah = 1,
                endSurahId = 2,
                endAyah = 25,
            ),
        ),
        onCompleteReview = {},
        onUpdateRating = {},
        onLaunchTaskReading = {},
    )
}
