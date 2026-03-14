package com.quran.tathbeet.ui.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val SettingsThemeCardPreviewWidth = 411
private const val SettingsThemeCardPreviewHeight = 220

@PreviewTest
@Preview(
    name = "settings_theme_card",
    locale = "ar",
    widthDp = SettingsThemeCardPreviewWidth,
    heightDp = SettingsThemeCardPreviewHeight,
    showBackground = true,
)
@Composable
fun SettingsThemeCardScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        ThemeSettingsCard(
            forceDarkTheme = false,
            onToggle = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "settings_theme_card_dark",
    locale = "ar",
    widthDp = SettingsThemeCardPreviewWidth,
    heightDp = SettingsThemeCardPreviewHeight,
    showBackground = true,
)
@Composable
fun SettingsThemeCardDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        ThemeSettingsCard(
            forceDarkTheme = true,
            onToggle = {},
        )
    }
}
