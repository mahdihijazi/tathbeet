package com.quran.tathbeet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.model.SelectionCategory
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

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
    ThemedScreenshotFrame(darkTheme = false) {
        AppTabbedPagerFixedPreview()
    }
}

@PreviewTest
@Preview(
    name = "app_tabbed_pager_fixed_dark",
    locale = "ar",
    widthDp = TabbedPagerPreviewWidth,
    heightDp = 320,
    showBackground = true,
)
@Composable
fun AppTabbedPagerFixedDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        AppTabbedPagerFixedPreview()
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
    ThemedScreenshotFrame(darkTheme = false) {
        AppTabbedPagerScrollablePreview()
    }
}

@PreviewTest
@Preview(
    name = "app_tabbed_pager_scrollable_dark",
    locale = "ar",
    widthDp = TabbedPagerPreviewWidth,
    heightDp = 360,
    showBackground = true,
)
@Composable
fun AppTabbedPagerScrollableDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        AppTabbedPagerScrollablePreview()
    }
}

@Composable
private fun AppTabbedPagerFixedPreview() {
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

@Composable
private fun AppTabbedPagerScrollablePreview() {
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
