package com.quran.tathbeet.ui.components

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
fun WizardHeaderStepOneScreenshot() {
    WizardHeaderScreenshotFrame {
        WizardHeader(
            currentStep = 1,
            totalSteps = 3,
        )
    }
}

@PreviewTest
@Preview(
    name = "wizard_header_step_2",
    locale = "ar",
    widthDp = HeaderPreviewWidth,
    heightDp = HeaderPreviewHeight,
    showBackground = true,
)
@Composable
fun WizardHeaderStepTwoScreenshot() {
    WizardHeaderScreenshotFrame {
        WizardHeader(
            currentStep = 2,
            totalSteps = 3,
        )
    }
}

@PreviewTest
@Preview(
    name = "wizard_header_step_3",
    locale = "ar",
    widthDp = HeaderPreviewWidth,
    heightDp = HeaderPreviewHeight,
    showBackground = true,
)
@Composable
fun WizardHeaderStepThreeScreenshot() {
    WizardHeaderScreenshotFrame {
        WizardHeader(
            currentStep = 3,
            totalSteps = 3,
        )
    }
}

@Composable
private fun WizardHeaderScreenshotFrame(content: @Composable () -> Unit) {
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
