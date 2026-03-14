package com.quran.tathbeet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val HeaderPreviewWidth = 411
private const val HeaderPreviewHeight = 180

@PreviewTest
@Preview(
    name = "wizard_header_step_1",
    locale = "ar",
    widthDp = HeaderPreviewWidth,
    heightDp = HeaderPreviewHeight,
    showBackground = true,
)
@Composable
fun WizardHeaderStepOneScreenshot() = WizardHeaderPreview(step = 1, darkTheme = false)

@PreviewTest
@Preview(
    name = "wizard_header_step_1_dark",
    locale = "ar",
    widthDp = HeaderPreviewWidth,
    heightDp = HeaderPreviewHeight,
    showBackground = true,
)
@Composable
fun WizardHeaderStepOneDarkScreenshot() = WizardHeaderPreview(step = 1, darkTheme = true)

@PreviewTest
@Preview(
    name = "wizard_header_step_2",
    locale = "ar",
    widthDp = HeaderPreviewWidth,
    heightDp = HeaderPreviewHeight,
    showBackground = true,
)
@Composable
fun WizardHeaderStepTwoScreenshot() = WizardHeaderPreview(step = 2, darkTheme = false)

@PreviewTest
@Preview(
    name = "wizard_header_step_2_dark",
    locale = "ar",
    widthDp = HeaderPreviewWidth,
    heightDp = HeaderPreviewHeight,
    showBackground = true,
)
@Composable
fun WizardHeaderStepTwoDarkScreenshot() = WizardHeaderPreview(step = 2, darkTheme = true)

@PreviewTest
@Preview(
    name = "wizard_header_step_3",
    locale = "ar",
    widthDp = HeaderPreviewWidth,
    heightDp = HeaderPreviewHeight,
    showBackground = true,
)
@Composable
fun WizardHeaderStepThreeScreenshot() = WizardHeaderPreview(step = 3, darkTheme = false)

@PreviewTest
@Preview(
    name = "wizard_header_step_3_dark",
    locale = "ar",
    widthDp = HeaderPreviewWidth,
    heightDp = HeaderPreviewHeight,
    showBackground = true,
)
@Composable
fun WizardHeaderStepThreeDarkScreenshot() = WizardHeaderPreview(step = 3, darkTheme = true)

@Composable
private fun WizardHeaderPreview(
    step: Int,
    darkTheme: Boolean,
) {
    ThemedScreenshotFrame(darkTheme = darkTheme) {
        WizardHeader(
            currentStep = step,
            totalSteps = 3,
        )
    }
}
