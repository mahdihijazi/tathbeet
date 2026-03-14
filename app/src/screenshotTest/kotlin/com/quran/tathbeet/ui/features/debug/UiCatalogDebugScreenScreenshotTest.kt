package com.quran.tathbeet.ui.features.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.theme.TathbeetTheme

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
    UiCatalogScreenshotFrame(darkTheme = false) {
        UiCatalogDebugScreen(
            darkThemeEnabled = false,
            onDarkThemeChanged = {},
        )
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
    UiCatalogScreenshotFrame(darkTheme = true) {
        UiCatalogDebugScreen(
            darkThemeEnabled = true,
            onDarkThemeChanged = {},
        )
    }
}

@Composable
private fun UiCatalogScreenshotFrame(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme(darkTheme = darkTheme) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
