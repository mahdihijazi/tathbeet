package com.quran.tathbeet.ui.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.quran.tathbeet.ui.components.TathbeetBackdrop
import com.quran.tathbeet.ui.theme.TathbeetTheme
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
internal fun ThemedScreenshotFrame(
    darkTheme: Boolean,
    padded: Boolean = true,
    withBackdrop: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme(darkTheme = darkTheme) {
            if (withBackdrop) {
                TathbeetBackdrop {
                    ScreenshotBox(
                        padded = padded,
                        content = content,
                    )
                }
            } else {
                ScreenshotBox(
                    padded = padded,
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun ScreenshotBox(
    padded: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (padded) {
                    Modifier.padding(TathbeetTokens.spacing.x3)
                } else {
                    Modifier
                },
            ),
    ) {
        content()
    }
}
