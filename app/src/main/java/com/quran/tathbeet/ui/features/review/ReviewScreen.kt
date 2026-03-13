package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.theme.TathbeetTokens
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReviewTabsPager(
    selectedTab: ReviewTab,
    onTabSelected: (ReviewTab) -> Unit,
    dailyTab: @Composable () -> Unit,
    fullPlanTab: @Composable () -> Unit,
) {
    val tabs = listOf(ReviewTab.Daily, ReviewTab.FullPlan)
    val selectedPage = tabs.indexOf(selectedTab).coerceAtLeast(0)
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = selectedPage,
        pageCount = { tabs.size },
    )

    LaunchedEffect(selectedTab) {
        if (pagerState.settledPage != selectedPage && pagerState.targetPage != selectedPage) {
            pagerState.scrollToPage(selectedPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                tabs.getOrNull(page)?.let { tab ->
                    if (tab != selectedTab) {
                        onTabSelected(tab)
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TathbeetTokens.spacing.x2Half),
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        if (tab != selectedTab) {
                            onTabSelected(tab)
                        }
                        if (pagerState.targetPage != index || pagerState.settledPage != index) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(
                                if (tab == ReviewTab.Daily) {
                                    R.string.review_tab_today
                                } else {
                                    R.string.review_tab_full_plan
                                },
                            ),
                            color = if (pagerState.currentPage == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                    modifier = Modifier.testTag(
                        if (tab == ReviewTab.Daily) {
                            "review-tab-daily"
                        } else {
                            "review-tab-full-plan"
                        },
                    ),
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("review-pager"),
        ) { page ->
            when (tabs[page]) {
                ReviewTab.Daily -> dailyTab()
                ReviewTab.FullPlan -> fullPlanTab()
            }
        }
    }
}

@Composable
fun ReviewScreen(
    uiState: ReviewUiState,
    onTabSelected: (ReviewTab) -> Unit,
    onRequestTaskCompletion: (String) -> Unit,
    onUpdateTaskRating: (String, Int) -> Unit,
    onRateTaskFromFullPlan: (String, Int) -> Unit,
    onLaunchTaskReading: (String) -> Unit,
    onRestartCycle: () -> Unit,
    onDismissCycleResetWarning: () -> Unit,
    onDismissCycleResetDialog: () -> Unit,
    onDismissExternalQuranDialog: () -> Unit,
    onOpenQuranAndroidInstall: () -> Unit,
    onOpenQuranOnWeb: () -> Unit,
) {
    ReviewTabsPager(
        selectedTab = uiState.selectedTab,
        onTabSelected = onTabSelected,
        dailyTab = {
            ReviewDailyTaskList(
                progressCard = uiState.progressCard,
                sections = uiState.sections,
                onRequestTaskCompletion = onRequestTaskCompletion,
                onUpdateTaskRating = onUpdateTaskRating,
                onLaunchTaskReading = onLaunchTaskReading,
            )
        },
        fullPlanTab = {
            ReviewFullPlanTaskList(
                tasks = uiState.fullPlanTasks,
                scrollToTopNonce = uiState.fullPlanScrollToTopNonce,
                onRequestTaskCompletion = onRequestTaskCompletion,
                onRateTask = onRateTaskFromFullPlan,
                onLaunchTaskReading = onLaunchTaskReading,
            )
        },
    )

    if (uiState.showCycleResetDialog) {
        ReviewCycleCompleteDialog(
            onRestartCycle = onRestartCycle,
            onDismiss = onDismissCycleResetDialog,
        )
    }

    if (uiState.showCycleResetWarningDialog) {
        ReviewCycleResetWarningDialog(
            onConfirm = onRestartCycle,
            onDismiss = onDismissCycleResetWarning,
        )
    }

    uiState.externalQuranDialog?.let { dialog ->
        ReviewExternalQuranDialog(
            dialog = dialog,
            onDismiss = onDismissExternalQuranDialog,
            onInstallQuranAndroid = onOpenQuranAndroidInstall,
            onOpenOnWeb = onOpenQuranOnWeb,
        )
    }
}
