package com.quran.tathbeet.ui.features.progress

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val ProgressPreviewWidth = 411
private const val ProgressPreviewHeight = 1500

@PreviewTest
@Preview(
    name = "progress_screen_empty",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenEmptyScreenshot() =
    ProgressScreenshotFrame(darkTheme = false) { ProgressPreviewFactory.emptyState() }

@PreviewTest
@Preview(
    name = "progress_screen_empty_dark",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenEmptyDarkScreenshot() =
    ProgressScreenshotFrame(darkTheme = true) { ProgressPreviewFactory.emptyState() }

@PreviewTest
@Preview(
    name = "progress_screen_partial",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenPartialScreenshot() =
    ProgressScreenshotFrame(darkTheme = false) { ProgressPreviewFactory.partialState() }

@PreviewTest
@Preview(
    name = "progress_screen_partial_dark",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenPartialDarkScreenshot() =
    ProgressScreenshotFrame(darkTheme = true) { ProgressPreviewFactory.partialState() }

@PreviewTest
@Preview(
    name = "progress_screen_complete",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenCompleteScreenshot() =
    ProgressScreenshotFrame(darkTheme = false) { ProgressPreviewFactory.completeState() }

@PreviewTest
@Preview(
    name = "progress_screen_complete_dark",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenCompleteDarkScreenshot() =
    ProgressScreenshotFrame(darkTheme = true) { ProgressPreviewFactory.completeState() }

@Composable
private fun ProgressScreenshotFrame(
    darkTheme: Boolean,
    state: () -> ProgressUiState,
) {
    ThemedScreenshotFrame(
        darkTheme = darkTheme,
        padded = false,
        withBackdrop = true,
    ) {
        ProgressScreen(
            uiState = state(),
            onOpenReview = {},
        )
    }
}

private object ProgressPreviewFactory {
    fun emptyState(): ProgressUiState =
        ProgressUiState(
            isLoading = false,
            todayCompleted = 0,
            todayTotal = 4,
            remainingCount = 4,
            completionRate = 0,
            completedDays = 0,
            weekValues = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
        )

    fun partialState(): ProgressUiState =
        ProgressUiState(
            isLoading = false,
            todayCompleted = 2,
            todayTotal = 5,
            remainingCount = 3,
            completionRate = 71,
            completedDays = 2,
            weekValues = listOf(0.4f, 0.65f, 0.8f, 1f, 1f, 0.5f, 0.6f),
            hasRollover = true,
        )

    fun completeState(): ProgressUiState =
        ProgressUiState(
            isLoading = false,
            todayCompleted = 3,
            todayTotal = 3,
            remainingCount = 0,
            completionRate = 96,
            completedDays = 5,
            weekValues = listOf(1f, 0.8f, 1f, 1f, 0.9f, 1f, 1f),
        )
}
