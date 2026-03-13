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

private const val CardPreviewWidth = 411
private const val CardPreviewHeight = 360

@PreviewTest
@Preview(
    name = "onboarding_message_card_default",
    locale = "ar",
    widthDp = CardPreviewWidth,
    heightDp = CardPreviewHeight,
    showBackground = true,
)
@Composable
fun OnboardingMessageCardScreenshot() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TathbeetTokens.spacing.x3),
            ) {
                OnboardingMessageCard(
                    title = stringResource(R.string.schedule_intro_body),
                    body = stringResource(R.string.schedule_intro_supporting),
                )
            }
        }
    }
}
