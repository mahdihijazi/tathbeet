package com.quran.tathbeet.ui.features.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val SummaryCardPreviewWidth = 411
private const val SummaryCardPreviewHeight = 240

@PreviewTest
@Preview(
    name = "selected_pool_summary_card_default",
    locale = "ar",
    widthDp = SummaryCardPreviewWidth,
    heightDp = SummaryCardPreviewHeight,
    showBackground = true,
)
@Composable
fun SelectedPoolSummaryCardScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        SelectedPoolSummaryCardPreview(
            selectionSummary = stringResource(R.string.quran_juz_title, 30),
            actionEnabled = true,
        )
    }
}

@PreviewTest
@Preview(
    name = "selected_pool_summary_card_default_dark",
    locale = "ar",
    widthDp = SummaryCardPreviewWidth,
    heightDp = SummaryCardPreviewHeight,
    showBackground = true,
)
@Composable
fun SelectedPoolSummaryCardDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        SelectedPoolSummaryCardPreview(
            selectionSummary = stringResource(R.string.quran_juz_title, 30),
            actionEnabled = true,
        )
    }
}

@PreviewTest
@Preview(
    name = "selected_pool_summary_card_disabled",
    locale = "ar",
    widthDp = SummaryCardPreviewWidth,
    heightDp = SummaryCardPreviewHeight,
    showBackground = true,
)
@Composable
fun SelectedPoolSummaryCardDisabledScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        SelectedPoolSummaryCardPreview(
            selectionSummary = stringResource(R.string.pool_selector_empty),
            actionEnabled = false,
        )
    }
}

@PreviewTest
@Preview(
    name = "selected_pool_summary_card_disabled_dark",
    locale = "ar",
    widthDp = SummaryCardPreviewWidth,
    heightDp = SummaryCardPreviewHeight,
    showBackground = true,
)
@Composable
fun SelectedPoolSummaryCardDisabledDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        SelectedPoolSummaryCardPreview(
            selectionSummary = stringResource(R.string.pool_selector_empty),
            actionEnabled = false,
        )
    }
}

@Composable
private fun SelectedPoolSummaryCardPreview(
    selectionSummary: String,
    actionEnabled: Boolean,
) {
    SelectedPoolSummaryCard(
        title = stringResource(R.string.pool_selector_current_selection),
        selectionSummary = selectionSummary,
        actionLabel = stringResource(R.string.action_next),
        actionEnabled = actionEnabled,
        onActionClick = {},
    )
}
