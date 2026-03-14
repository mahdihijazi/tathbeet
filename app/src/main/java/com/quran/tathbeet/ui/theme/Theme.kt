package com.quran.tathbeet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun TathbeetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val themeSpec = resolveTathbeetThemeSpec(darkTheme)
    ApplySystemBarStyle(useDarkSystemBarIcons = themeSpec.useDarkSystemBarIcons)

    CompositionLocalProvider(
        LocalAppSpacing provides appSpacing,
        LocalAppRadii provides appRadii,
    ) {
        MaterialTheme(
            colorScheme = themeSpec.colorScheme,
            typography = TathbeetTypography,
            shapes = TathbeetShapes,
            content = content,
        )
    }
}

@Composable
private fun ApplySystemBarStyle(
    useDarkSystemBarIcons: Boolean,
) {
    val view = LocalView.current
    if (view.isInEditMode) {
        return
    }

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = useDarkSystemBarIcons
            isAppearanceLightNavigationBars = useDarkSystemBarIcons
        }
    }
}
