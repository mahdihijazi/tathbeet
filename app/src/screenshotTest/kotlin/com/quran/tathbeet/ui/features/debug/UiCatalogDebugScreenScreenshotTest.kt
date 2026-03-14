package com.quran.tathbeet.ui.features.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val CatalogPreviewWidth = 411
private const val CatalogPreviewHeight = 2300

@PreviewTest
@Preview(
    name = "ui_catalog_light",
    locale = "ar",
    widthDp = CatalogPreviewWidth,
    heightDp = CatalogPreviewHeight,
    showBackground = true,
)
@Composable
fun UiCatalogLightScreenshot() {
    ThemedScreenshotFrame(darkTheme = false, padded = false) {
        UiCatalogDebugScreen()
    }
}

@PreviewTest
@Preview(
    name = "ui_catalog_dark",
    locale = "ar",
    widthDp = CatalogPreviewWidth,
    heightDp = CatalogPreviewHeight,
    showBackground = true,
)
@Composable
fun UiCatalogDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true, padded = false) {
        UiCatalogDebugScreen()
    }
}
