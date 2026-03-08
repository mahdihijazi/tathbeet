package com.quran.tathbeet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TathbeetSpacing(
    val half: Dp = 4.dp,
    val x1: Dp = 8.dp,
    val x1Half: Dp = 12.dp,
    val x2: Dp = 16.dp,
    val x2Half: Dp = 20.dp,
    val x3: Dp = 24.dp,
    val x4: Dp = 32.dp,
)

@Immutable
data class TathbeetRadii(
    val sm: Dp = 16.dp,
    val md: Dp = 22.dp,
    val lg: Dp = 28.dp,
    val pill: Dp = 999.dp,
)

private val LocalSpacing = staticCompositionLocalOf { TathbeetSpacing() }
private val LocalRadii = staticCompositionLocalOf { TathbeetRadii() }

object TathbeetTokens {
    val spacing: TathbeetSpacing
        @Composable get() = LocalSpacing.current

    val radii: TathbeetRadii
        @Composable get() = LocalRadii.current
}

internal val appSpacing = TathbeetSpacing()
internal val appRadii = TathbeetRadii()

internal val LocalAppSpacing = LocalSpacing
internal val LocalAppRadii = LocalRadii
