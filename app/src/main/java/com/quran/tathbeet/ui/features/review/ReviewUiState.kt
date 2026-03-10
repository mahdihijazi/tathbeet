package com.quran.tathbeet.ui.features.review

data class ReviewTaskUiState(
    val id: String,
    val title: String,
    val detail: String,
    val isDone: Boolean,
    val isRollover: Boolean,
)

data class ReviewUiState(
    val isLoading: Boolean = true,
    val completionRate: Int = 0,
    val tasks: List<ReviewTaskUiState> = emptyList(),
) {
    val remainingCount: Int
        get() = tasks.count { !it.isDone }

    val rolloverCount: Int
        get() = tasks.count { it.isRollover && !it.isDone }

    val progress: Float
        get() = if (tasks.isEmpty()) 0f else tasks.count { it.isDone }.toFloat() / tasks.size
}
