package com.quran.tathbeet.ui.features.review

data class ReviewSortActionState(
    val selectedMode: ReviewFullPlanSortMode,
    val onModeSelected: (ReviewFullPlanSortMode) -> Unit,
)

fun ReviewUiState.reviewSortActionState(
    onModeSelected: (ReviewFullPlanSortMode) -> Unit,
): ReviewSortActionState? =
    if (selectedTab == ReviewTab.FullPlan) {
        ReviewSortActionState(
            selectedMode = fullPlanSortMode,
            onModeSelected = onModeSelected,
        )
    } else {
        null
    }
