package com.quran.tathbeet.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val ActionPrimary = Color(0xFF2E6772)
internal val OnActionPrimary = Color(0xFFFFFBF4)
internal val ActionSecondary = Color(0xFF2A7C74)
internal val OnActionSecondary = Color(0xFFFFFBF4)
internal val AccentSupport = Color(0xFFE08A2E)
internal val SurfacePrimary = Color(0xFFF1E8D8)
internal val SurfaceSecondary = Color(0xFFF7F0E3)
internal val SurfaceHighlight = Color(0xFFDDF2E6)
internal val SurfaceMuted = Color(0xFFE8E1F4)
internal val SurfaceAccent = Color(0xFFFFE4C4)
internal val ContentPrimary = Color(0xFF1F1B16)
internal val ContentSecondary = Color(0xFF5B544B)
internal val StrokeSubtle = Color(0xFF938777)

internal val ActionPrimaryDark = Color(0xFF8CCFDC)
internal val OnActionPrimaryDark = Color(0xFF00363F)
internal val ActionSecondaryDark = Color(0xFF7DD1C5)
internal val OnActionSecondaryDark = Color(0xFF003731)
internal val AccentSupportDark = Color(0xFFF6BC73)
internal val OnAccentSupportDark = Color(0xFF472A00)
internal val SurfacePrimaryDark = Color(0xFF0F1417)
internal val SurfaceSecondaryDark = Color(0xFF171E22)
internal val SurfaceHighlightDark = Color(0xFF12373D)
internal val SurfaceMutedDark = Color(0xFF2B2938)
internal val SurfaceAccentDark = Color(0xFF42301B)
internal val ContentPrimaryDark = Color(0xFFE7F2F4)
internal val ContentSecondaryDark = Color(0xFFB2C4C9)
internal val StrokeSubtleDark = Color(0xFF77898F)

internal fun tathbeetLightColorScheme(): ColorScheme =
    lightColorScheme(
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

internal fun tathbeetDarkColorScheme(): ColorScheme =
    darkColorScheme(
        primary = ActionPrimaryDark,
        onPrimary = OnActionPrimaryDark,
        primaryContainer = SurfaceHighlightDark,
        onPrimaryContainer = ContentPrimaryDark,
        secondary = ActionSecondaryDark,
        onSecondary = OnActionSecondaryDark,
        secondaryContainer = SurfaceMutedDark,
        onSecondaryContainer = ContentPrimaryDark,
        tertiary = AccentSupportDark,
        onTertiary = OnAccentSupportDark,
        tertiaryContainer = SurfaceAccentDark,
        onTertiaryContainer = ContentPrimaryDark,
        background = SurfacePrimaryDark,
        onBackground = ContentPrimaryDark,
        surface = SurfaceSecondaryDark,
        onSurface = ContentPrimaryDark,
        surfaceVariant = SurfaceMutedDark,
        onSurfaceVariant = ContentSecondaryDark,
        outline = StrokeSubtleDark,
    )
