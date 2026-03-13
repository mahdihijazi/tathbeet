package com.quran.tathbeet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.model.AppDestination
import com.quran.tathbeet.ui.features.review.ReviewFullPlanSortMode
import com.quran.tathbeet.ui.features.review.ReviewSortActionState

@Composable
fun AppShell(
    currentDestination: AppDestination,
    reviewTitle: String?,
    reviewSortActionState: ReviewSortActionState?,
    onNavigate: (AppDestination) -> Unit,
    onBack: () -> Unit,
    onReviewPlanAction: () -> Unit,
    onReviewResetAction: () -> Unit,
    onSettingsDebugAction: (() -> Unit)?,
    snackbarHost: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    TathbeetBackdrop {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.12f),
            topBar = {
                if (currentDestination != AppDestination.ScheduleIntro) {
                    AppTopBar(
                        currentDestination = currentDestination,
                        reviewTitle = reviewTitle,
                        reviewSortActionState = reviewSortActionState,
                        onBack = onBack,
                        onReviewPlanAction = onReviewPlanAction,
                        onReviewResetAction = onReviewResetAction,
                        onSettingsDebugAction = onSettingsDebugAction,
                    )
                }
            },
            bottomBar = {
                if (currentDestination in mainDestinations) {
                    AppBottomBar(
                        currentDestination = currentDestination,
                        onNavigate = onNavigate,
                    )
                }
            },
            snackbarHost = snackbarHost,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                content()
            }
        }
    }
}

private val mainDestinations = listOf(
    AppDestination.Profiles,
    AppDestination.Review,
    AppDestination.Progress,
    AppDestination.Settings,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentDestination: AppDestination,
    reviewTitle: String?,
    reviewSortActionState: ReviewSortActionState?,
    onBack: () -> Unit,
    onReviewPlanAction: () -> Unit,
    onReviewResetAction: () -> Unit,
    onSettingsDebugAction: (() -> Unit)?,
) {
    var showSortMenu by remember(reviewSortActionState) { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.18f),
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
            if (currentDestination != AppDestination.ScheduleIntro) {
                Text(
                    text = reviewTitle
                        ?.takeIf { currentDestination == AppDestination.Review }
                        ?: stringResource(currentDestination.titleRes),
                )
            }
        },
        navigationIcon = {
            if (currentDestination !in mainDestinations && currentDestination != AppDestination.ScheduleIntro) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.content_back),
                    )
                }
            }
        },
        actions = {
            if (currentDestination == AppDestination.Review) {
                reviewSortActionState?.let { sortAction ->
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Sort,
                            contentDescription = stringResource(R.string.content_sort_full_plan),
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                    ) {
                        ReviewFullPlanSortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(
                                            when (mode) {
                                                ReviewFullPlanSortMode.Rating -> R.string.review_sort_rating
                                                ReviewFullPlanSortMode.LastMemorized -> R.string.review_sort_last_memorized
                                                ReviewFullPlanSortMode.QuranOrder -> R.string.review_sort_quran_order
                                            },
                                        ),
                                    )
                                },
                                leadingIcon = {
                                    if (mode == sortAction.selectedMode) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                        )
                                    }
                                },
                                onClick = {
                                    showSortMenu = false
                                    sortAction.onModeSelected(mode)
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = onReviewResetAction) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = stringResource(R.string.content_reset_cycle),
                    )
                }
                IconButton(onClick = onReviewPlanAction) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.content_edit_plan),
                    )
                }
            }
            if (currentDestination == AppDestination.Settings && onSettingsDebugAction != null) {
                IconButton(
                    onClick = onSettingsDebugAction,
                    modifier = Modifier.testTag("settings-open-debug-tools"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BugReport,
                        contentDescription = stringResource(R.string.content_open_debug_tools),
                    )
                }
            }
        },
    )
}

@Composable
private fun AppBottomBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
) {
    NavigationBar {
        listOf(
            AppDestination.Profiles to Icons.Outlined.Groups,
            AppDestination.Review to Icons.AutoMirrored.Outlined.MenuBook,
            AppDestination.Progress to Icons.Outlined.BarChart,
            AppDestination.Settings to Icons.Outlined.Settings,
        ).forEach { (destination, icon) ->
            NavigationBarItem(
                selected = destination == currentDestination,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(destination.titleRes),
                    )
                },
                label = {
                    Text(text = stringResource(destination.subtitleRes))
                },
            )
        }
    }
}

@Composable
fun TathbeetBackdrop(
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = (-24).dp)
                .size(220.dp)
                .background(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-88).dp, y = 100.dp)
                .size(260.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 84.dp, y = 92.dp)
                .size(240.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                ),
        )
        content()
    }
}
