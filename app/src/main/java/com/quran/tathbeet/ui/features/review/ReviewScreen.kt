package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppTabbedPager
import com.quran.tathbeet.ui.components.AppTabbedPagerStyle
import com.quran.tathbeet.ui.components.AppTabbedPagerTab
import com.quran.tathbeet.ui.theme.TathbeetTokens

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
    val tabs = listOf(
        AppTabbedPagerTab(
            value = ReviewTab.Daily,
            label = stringResource(R.string.review_tab_today),
            tabTestTag = "review-tab-daily",
        ),
        AppTabbedPagerTab(
            value = ReviewTab.FullPlan,
            label = stringResource(R.string.review_tab_full_plan),
            tabTestTag = "review-tab-full-plan",
        ),
    )

    AppTabbedPager(
        tabs = tabs,
        selectedTab = uiState.selectedTab,
        onTabSelected = onTabSelected,
        rowStyle = AppTabbedPagerStyle.Fixed,
        containerColor = Color.Transparent,
        tabsModifier = Modifier.padding(horizontal = TathbeetTokens.spacing.x2Half),
        pagerTestTag = "review-pager",
    ) { tab ->
        when (tab) {
            ReviewTab.Daily -> {
                ReviewDailyTaskList(
                    progressCard = uiState.progressCard,
                    sections = uiState.sections,
                    onRequestTaskCompletion = onRequestTaskCompletion,
                    onUpdateTaskRating = onUpdateTaskRating,
                    onLaunchTaskReading = onLaunchTaskReading,
                )
            }

            ReviewTab.FullPlan -> {
                ReviewFullPlanTaskList(
                    tasks = uiState.fullPlanTasks,
                    scrollToTopNonce = uiState.fullPlanScrollToTopNonce,
                    onRequestTaskCompletion = onRequestTaskCompletion,
                    onRateTask = onRateTaskFromFullPlan,
                    onLaunchTaskReading = onLaunchTaskReading,
                )
            }
        }
    }

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
