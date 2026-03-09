package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.theme.TathbeetTheme
import com.quran.tathbeet.ui.theme.TathbeetTokens

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
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TathbeetTokens.spacing.x3),
            ) {
                SelectedPoolSummaryCard(
                    title = stringResource(R.string.pool_selector_current_selection),
                    selectionSummary = stringResource(R.string.quran_juz_title, 30),
                    actionLabel = stringResource(R.string.action_next),
                    onActionClick = {},
                )
            }
        }
    }
}
