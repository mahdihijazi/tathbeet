package com.quran.tathbeet.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

internal data class TathbeetThemeSpec(
    val colorScheme: ColorScheme,
    val useDarkSystemBarIcons: Boolean,
)

internal fun resolveTathbeetThemeSpec(
    darkTheme: Boolean,
): TathbeetThemeSpec {
    val colorScheme = resolveTathbeetColorScheme(darkTheme)

    return TathbeetThemeSpec(
        colorScheme = colorScheme,
        useDarkSystemBarIcons = prefersDarkSystemBarIcons(colorScheme.background),
    )
}

private fun resolveTathbeetColorScheme(
    darkTheme: Boolean,
): ColorScheme =
    if (darkTheme) {
        tathbeetDarkColorScheme()
    } else {
        tathbeetLightColorScheme()
    }

internal fun prefersDarkSystemBarIcons(
    backgroundColor: Color,
): Boolean = backgroundColor.luminance() > 0.5f
