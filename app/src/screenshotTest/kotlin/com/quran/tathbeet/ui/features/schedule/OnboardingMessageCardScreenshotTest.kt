package com.quran.tathbeet.ui.features.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

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
    ThemedScreenshotFrame(darkTheme = false) {
        OnboardingMessageCardPreview()
    }
}

@PreviewTest
@Preview(
    name = "onboarding_message_card_default_dark",
    locale = "ar",
    widthDp = CardPreviewWidth,
    heightDp = CardPreviewHeight,
    showBackground = true,
)
@Composable
fun OnboardingMessageCardDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        OnboardingMessageCardPreview()
    }
}

@Composable
private fun OnboardingMessageCardPreview() {
    OnboardingMessageCard(
        title = stringResource(R.string.schedule_intro_body),
        body = stringResource(R.string.schedule_intro_supporting),
    )
}
