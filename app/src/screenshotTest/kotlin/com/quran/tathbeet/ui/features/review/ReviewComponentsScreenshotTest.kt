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
private const val ReviewDialogHeight = 320

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
            onEditRating = {},
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
            onEditRating = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "review_rating_dialog_default",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewDialogHeight,
    showBackground = true,
)
@Composable
fun ReviewRatingDialogDefaultScreenshot() {
    ReviewScreenshotBox {
        ReviewRatingDialog(
            selectedRating = 5,
            onSelectRating = {},
            onDismiss = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "review_cycle_complete_dialog",
    locale = "ar",
    widthDp = ReviewPreviewWidth,
    heightDp = ReviewDialogHeight,
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
