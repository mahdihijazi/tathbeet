package com.quran.tathbeet.ui.features.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
internal fun ReviewDailyTaskList(
    progressCard: ReviewProgressCardUiState?,
    sections: List<ReviewSectionUiState>,
    onRequestTaskCompletion: (String) -> Unit,
    onUpdateTaskRating: (String, Int) -> Unit,
    onLaunchTaskReading: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("review-sections-list")
            .padding(
                horizontal = TathbeetTokens.spacing.x2Half,
                vertical = TathbeetTokens.spacing.x2Half,
            ),
        contentPadding = PaddingValues(bottom = TathbeetTokens.spacing.x1),
        verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x2),
    ) {
        progressCard?.let { card ->
            item(key = "review-progress-card") {
                ReviewProgressCard(progress = card)
            }
        }
        sections.forEach { section ->
            item(key = "${section.id}-header") {
                ReviewSectionHeader(section = section)
            }
            items(
                items = section.tasks,
                key = { task -> task.id },
            ) { task ->
                ReviewTaskRow(
                    task = task,
                    onCompleteReview = { onRequestTaskCompletion(task.id) },
                    onUpdateRating = { rating -> onUpdateTaskRating(task.id, rating) },
                    onLaunchTaskReading = { onLaunchTaskReading(task.id) },
                )
            }
        }
    }
}

@Composable
internal fun ReviewFullPlanTaskList(
    tasks: List<ReviewTaskUiState>,
    scrollToTopNonce: Int,
    onRequestTaskCompletion: (String) -> Unit,
    onRateTask: (String, Int) -> Unit,
    onLaunchTaskReading: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopNonce) {
        if (scrollToTopNonce > 0) {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("review-full-plan-list")
            .padding(
                horizontal = TathbeetTokens.spacing.x2Half,
                vertical = TathbeetTokens.spacing.x2Half,
            ),
        contentPadding = PaddingValues(bottom = TathbeetTokens.spacing.x1),
        verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x2),
    ) {
        itemsIndexed(
            items = tasks,
            key = { _, task -> task.id },
        ) { index, task ->
            ReviewTaskRow(
                task = task,
                onCompleteReview = { onRequestTaskCompletion(task.id) },
                onUpdateRating = { rating -> onRateTask(task.id, rating) },
                onLaunchTaskReading = { onLaunchTaskReading(task.id) },
                modifier = Modifier.testTag("review-full-plan-position-$index-${task.id}"),
                showRatingAlways = true,
                allowRatingWithoutCompletion = true,
            )
        }
    }
}
