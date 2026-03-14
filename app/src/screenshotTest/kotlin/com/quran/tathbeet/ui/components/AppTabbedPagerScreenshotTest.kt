package com.quran.tathbeet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.model.SelectionCategory
import com.quran.tathbeet.ui.theme.TathbeetTheme
import com.quran.tathbeet.ui.theme.TathbeetTokens

private const val TabbedPagerPreviewWidth = 411

@PreviewTest
@Preview(
    name = "app_tabbed_pager_fixed",
    locale = "ar",
    widthDp = TabbedPagerPreviewWidth,
    heightDp = 320,
    showBackground = true,
)
@Composable
fun AppTabbedPagerFixedScreenshot() {
    AppTabbedPagerScreenshotFrame {
        AppTabbedPager(
            tabs = listOf(
                AppTabbedPagerTab(
                    value = "daily",
                    label = stringResource(R.string.review_tab_today),
                ),
                AppTabbedPagerTab(
                    value = "full_plan",
                    label = stringResource(R.string.review_tab_full_plan),
                ),
            ),
            selectedTab = "daily",
            onTabSelected = {},
            rowStyle = AppTabbedPagerStyle.Fixed,
            containerColor = Color.Transparent,
        ) { tab ->
            CardSection {
                androidx.compose.material3.Text(
                    text = if (tab == "daily") {
                        stringResource(R.string.review_tab_today)
                    } else {
                        stringResource(R.string.review_tab_full_plan)
                    },
                )
            }
        }
    }
}

@PreviewTest
@Preview(
    name = "app_tabbed_pager_scrollable",
    locale = "ar",
    widthDp = TabbedPagerPreviewWidth,
    heightDp = 360,
    showBackground = true,
)
@Composable
fun AppTabbedPagerScrollableScreenshot() {
    AppTabbedPagerScreenshotFrame {
        val tabs = SelectionCategory.entries.map { category ->
            AppTabbedPagerTab(
                value = category,
                label = stringResource(category.labelRes),
            )
        }
        AppTabbedPager(
            tabs = tabs,
            selectedTab = SelectionCategory.Juz,
            onTabSelected = {},
            rowStyle = AppTabbedPagerStyle.Scrollable(edgePadding = 0.dp),
        ) { tab ->
            CardSection {
                androidx.compose.material3.Text(
                    text = stringResource(tab.labelRes),
                )
            }
        }
    }
}

@Composable
private fun AppTabbedPagerScreenshotFrame(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TathbeetTokens.spacing.x3),
            ) {
                content()
            }
        }
    }
}
