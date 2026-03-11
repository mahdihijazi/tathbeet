package com.quran.tathbeet.ui.features.review

import com.quran.tathbeet.R
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.ui.model.TextSpec

data class ReviewTaskUiState(
    val id: String,
    val title: TextSpec,
    val detail: TextSpec,
    val isDone: Boolean,
    val rating: Int? = null,
    val defaultRating: Int = 3,
)

data class ReviewSectionUiState(
    val id: String,
    val title: TextSpec,
    val status: TextSpec,
    val tasks: List<ReviewTaskUiState>,
)

data class ReviewProgressCardUiState(
    val completedCount: Int,
    val totalCount: Int,
    val remainingCount: Int,
    val progress: Float,
)

data class ReviewUiState(
    val isLoading: Boolean = true,
    val progressCard: ReviewProgressCardUiState? = null,
    val sections: List<ReviewSectionUiState> = emptyList(),
    val showCycleResetDialog: Boolean = false,
)

fun ReviewDay.toUiState(
): ReviewUiState {
    val tasks = assignments.sortedBy { it.displayOrder }.map { assignment ->
        ReviewTaskUiState(
            id = assignment.id,
            title = TextSpec(rawText = assignment.title),
            detail = TextSpec(rawText = assignment.detail),
            isDone = assignment.isDone,
            rating = assignment.rating,
            defaultRating = assignment.rating ?: 3,
        )
    }
    val completedCount = tasks.count { it.isDone }
    val totalCount = tasks.size
    val remainingCount = totalCount - completedCount
    val section = ReviewSectionUiState(
        id = assignedForDate.toString(),
        title = TextSpec(R.string.review_section_today_title),
        status = TextSpec(
            if (tasks.isNotEmpty() && tasks.all { it.isDone }) {
                R.string.review_state_done
            } else {
                R.string.review_state_available_now
            },
        ),
        tasks = tasks,
    )

    return ReviewUiState(
        isLoading = false,
        progressCard = ReviewProgressCardUiState(
            completedCount = completedCount,
            totalCount = totalCount,
            remainingCount = remainingCount,
            progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat(),
        ),
        sections = listOf(section),
        showCycleResetDialog = false,
    )
}

internal data class ReviewMockState(
    val allSections: List<ReviewSectionUiState>,
    val visibleSectionCount: Int,
    val showCycleResetDialog: Boolean,
) {
    fun toUiState(): ReviewUiState =
        allSections.take(visibleSectionCount).let { visibleSections ->
            val visibleTasks = visibleSections.flatMap { it.tasks }
            val completedCount = visibleTasks.count { it.isDone }
            val totalCount = visibleTasks.size
            val remainingCount = totalCount - completedCount

            ReviewUiState(
                isLoading = false,
                progressCard = ReviewProgressCardUiState(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    remainingCount = remainingCount,
                    progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat(),
                ),
                sections = visibleSections,
                showCycleResetDialog = showCycleResetDialog,
            )
        }
}

internal object ReviewMockFactory {

    fun initialState(): ReviewMockState {
        val sections = listOf(
            section(
                id = "rollover",
                titleRes = R.string.review_section_rollover_title,
                tasks = listOf(
                    rubTask(
                        id = "rub-12",
                        rubNumber = 12,
                        juzNumber = 2,
                        hizbNumber = 3,
                        rangeRes = R.string.sample_range_rub_12,
                        isDone = true,
                        rating = 4,
                    ),
                ),
            ),
            section(
                id = "today",
                titleRes = R.string.review_section_today_title,
                tasks = listOf(
                    rubTask(
                        id = "rub-13",
                        rubNumber = 13,
                        juzNumber = 2,
                        hizbNumber = 4,
                        rangeRes = R.string.sample_range_rub_13,
                        isDone = true,
                        rating = 5,
                    ),
                    rubTask(
                        id = "rub-14",
                        rubNumber = 14,
                        juzNumber = 3,
                        hizbNumber = 4,
                        rangeRes = R.string.sample_range_rub_14,
                        isDone = true,
                        rating = 4,
                    ),
                ),
            ),
            section(
                id = "tomorrow",
                titleRes = R.string.review_section_next_title,
                tasks = listOf(
                    rubTask(
                        id = "rub-15",
                        rubNumber = 15,
                        juzNumber = 3,
                        hizbNumber = 4,
                        rangeRes = R.string.sample_range_rub_15,
                        isDone = false,
                    ),
                    rubTask(
                        id = "rub-16",
                        rubNumber = 16,
                        juzNumber = 3,
                        hizbNumber = 4,
                        rangeRes = R.string.sample_range_rub_16,
                        isDone = false,
                    ),
                ),
            ),
            section(
                id = "day-after-tomorrow",
                titleRes = R.string.review_section_day_after_next_title,
                tasks = listOf(
                    rubTask(
                        id = "rub-17",
                        rubNumber = 17,
                        juzNumber = 3,
                        hizbNumber = 5,
                        rangeRes = R.string.sample_range_rub_17,
                        isDone = false,
                    ),
                ),
            ),
            section(
                id = "cycle-last-day",
                titleRes = R.string.review_section_cycle_last_title,
                tasks = listOf(
                    rubTask(
                        id = "rub-18",
                        rubNumber = 18,
                        juzNumber = 3,
                        hizbNumber = 5,
                        rangeRes = R.string.sample_range_rub_18,
                        isDone = false,
                    ),
                ),
            ),
        )

        return ReviewMockState(
            allSections = sections,
            visibleSectionCount = 3,
            showCycleResetDialog = false,
        )
    }

    private fun section(
        id: String,
        titleRes: Int,
        tasks: List<ReviewTaskUiState>,
    ): ReviewSectionUiState =
        ReviewSectionUiState(
            id = id,
            title = TextSpec(titleRes),
            status = TextSpec(
                if (tasks.all { it.isDone }) {
                    R.string.review_state_done
                } else {
                    R.string.review_state_available_now
                },
            ),
            tasks = tasks,
        )

    private fun rubTask(
        id: String,
        rubNumber: Int,
        juzNumber: Int,
        hizbNumber: Int,
        rangeRes: Int,
        isDone: Boolean,
        rating: Int? = null,
        ) = ReviewTaskUiState(
        id = id,
        title = TextSpec(R.string.quran_rub_title, listOf(rubNumber)),
        detail = TextSpec(
            R.string.quran_rub_detail,
            listOf(
                juzNumber,
                hizbNumber,
                TextSpec(rangeRes),
            ),
        ),
        isDone = isDone,
        rating = rating,
        defaultRating = rating ?: 3,
    )
}
