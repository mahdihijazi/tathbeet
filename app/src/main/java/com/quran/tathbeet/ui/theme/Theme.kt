package com.quran.tathbeet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = WarmWhite,
    primaryContainer = Sand,
    onPrimaryContainer = Ink,
    secondary = Olive,
    onSecondary = WarmWhite,
    secondaryContainer = Mist,
    onSecondaryContainer = Ink,
    tertiary = Clay,
    onTertiary = WarmWhite,
    tertiaryContainer = Color(0xFFFFE2D0),
    onTertiaryContainer = Ink,
    background = WarmWhite,
    onBackground = Ink,
    surface = Color(0xFFFFF8EE),
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Color(0xFF5A564E),
    outline = Color(0xFF8A8478),
)

@Composable
fun TathbeetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = TathbeetTypography,
        content = content,
    )
}
