package com.quran.tathbeet.ui.features.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.components.TathbeetBackdrop
import com.quran.tathbeet.ui.theme.TathbeetTheme

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
fun ProgressScreenEmptyScreenshot() {
    ProgressScreenshotBox {
        ProgressScreen(
            uiState = ProgressPreviewFactory.emptyState(),
            onOpenReview = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "progress_screen_partial",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenPartialScreenshot() {
    ProgressScreenshotBox {
        ProgressScreen(
            uiState = ProgressPreviewFactory.partialState(),
            onOpenReview = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "progress_screen_complete",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenCompleteScreenshot() {
    ProgressScreenshotBox {
        ProgressScreen(
            uiState = ProgressPreviewFactory.completeState(),
            onOpenReview = {},
        )
    }
}

@Composable
private fun ProgressScreenshotBox(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme {
            TathbeetBackdrop {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
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
