package com.quran.tathbeet.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class AppTabbedPagerTab<T>(
    val value: T,
    val label: String,
    val tabTestTag: String? = null,
)

sealed interface AppTabbedPagerStyle {
    data object Fixed : AppTabbedPagerStyle

    data class Scrollable(
        val edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    ) : AppTabbedPagerStyle
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> AppTabbedPager(
    tabs: List<AppTabbedPagerTab<T>>,
    selectedTab: T,
    onTabSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    rowStyle: AppTabbedPagerStyle = AppTabbedPagerStyle.Fixed,
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    tabsModifier: Modifier = Modifier,
    pagerModifier: Modifier = Modifier,
    pagerTestTag: String? = null,
    verticalSpacing: Dp = Dp.Unspecified,
    pageContent: @Composable (T) -> Unit,
) {
    val selectedPage = tabs.indexOfFirst { it.value == selectedTab }.coerceAtLeast(0)
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = selectedPage,
        pageCount = { tabs.size },
    )

    LaunchedEffect(selectedTab, tabs) {
        val page = tabs.indexOfFirst { it.value == selectedTab }.coerceAtLeast(0)
        if (pagerState.settledPage != page && pagerState.targetPage != page) {
            pagerState.scrollToPage(page)
        }
    }

    LaunchedEffect(pagerState, tabs, selectedTab) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                tabs.getOrNull(page)?.value?.let { tab ->
                    if (tab != selectedTab) {
                        onTabSelected(tab)
                    }
                }
            }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = if (verticalSpacing == Dp.Unspecified) {
            Arrangement.Top
        } else {
            Arrangement.spacedBy(verticalSpacing)
        },
    ) {
        when (rowStyle) {
            AppTabbedPagerStyle.Fixed -> {
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = containerColor,
                    modifier = tabsModifier.fillMaxWidth(),
                ) {
                    tabs.forEachIndexed { index, tab ->
                        AppTabbedPagerTab(
                            tab = tab,
                            selected = pagerState.currentPage == index,
                            onClick = {
                                if (tab.value != selectedTab) {
                                    onTabSelected(tab.value)
                                }
                                if (pagerState.targetPage != index || pagerState.settledPage != index) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            },
                        )
                    }
                }
            }

            is AppTabbedPagerStyle.Scrollable -> {
                PrimaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = containerColor,
                    edgePadding = rowStyle.edgePadding,
                    modifier = tabsModifier.fillMaxWidth(),
                ) {
                    tabs.forEachIndexed { index, tab ->
                        AppTabbedPagerTab(
                            tab = tab,
                            selected = pagerState.currentPage == index,
                            onClick = {
                                if (tab.value != selectedTab) {
                                    onTabSelected(tab.value)
                                }
                                if (pagerState.targetPage != index || pagerState.settledPage != index) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .then(
                    if (pagerTestTag != null) {
                        pagerModifier.testTag(pagerTestTag)
                    } else {
                        pagerModifier
                    },
                ),
        ) { page ->
            tabs.getOrNull(page)?.value?.let { tab ->
                pageContent(tab)
            }
        }
    }
}

@Composable
private fun <T> AppTabbedPagerTab(
    tab: AppTabbedPagerTab<T>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = if (tab.tabTestTag != null) {
            Modifier.testTag(tab.tabTestTag)
        } else {
            Modifier
        },
        text = {
            Text(
                text = tab.label,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        },
    )
}
