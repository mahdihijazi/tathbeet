package com.quran.tathbeet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColors = lightColorScheme(
    primary = ActionPrimary,
    onPrimary = OnActionPrimary,
    primaryContainer = SurfaceHighlight,
    onPrimaryContainer = ContentPrimary,
    secondary = ActionSecondary,
    onSecondary = OnActionSecondary,
    secondaryContainer = SurfaceMuted,
    onSecondaryContainer = ContentPrimary,
    tertiary = AccentSupport,
    onTertiary = OnActionPrimary,
    tertiaryContainer = SurfaceAccent,
    onTertiaryContainer = ContentPrimary,
    background = SurfacePrimary,
    onBackground = ContentPrimary,
    surface = SurfaceSecondary,
    onSurface = ContentPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = ContentSecondary,
    outline = StrokeSubtle,
)

@Composable
fun TathbeetTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppSpacing provides appSpacing,
        LocalAppRadii provides appRadii,
    ) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = TathbeetTypography,
            shapes = TathbeetShapes,
            content = content,
        )
    }
}
