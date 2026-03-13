package com.quran.tathbeet.ui.features.review

import com.quran.tathbeet.R
import com.quran.tathbeet.domain.model.QuranReadingTarget
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.ui.model.TextSpec

data class ReviewTaskUiState(
    val id: String,
    val title: TextSpec,
    val detail: TextSpec,
    val isDone: Boolean,
    val rating: Int? = null,
    val defaultRating: Int = 3,
    val weight: Double = 1.0,
    val readingTarget: QuranReadingTarget? = null,
)

data class ReviewSectionUiState(
    val id: String,
    val title: TextSpec,
    val status: TextSpec,
    val tasks: List<ReviewTaskUiState>,
)

data class ReviewProgressCardUiState(
    val completedText: String,
    val totalText: String,
    val remainingText: String,
    val progress: Float,
)

enum class ReviewTab {
    Daily,
    FullPlan,
}

enum class ReviewFullPlanSortMode {
    Rating,
    LastMemorized,
    QuranOrder,
}

data class ReviewUiState(
    val isLoading: Boolean = true,
    val selectedTab: ReviewTab = ReviewTab.Daily,
    val fullPlanSortMode: ReviewFullPlanSortMode = ReviewFullPlanSortMode.Rating,
    val progressCard: ReviewProgressCardUiState? = null,
    val sections: List<ReviewSectionUiState> = emptyList(),
    val fullPlanTasks: List<ReviewTaskUiState> = emptyList(),
    val fullPlanScrollToTopNonce: Int = 0,
    val showCycleResetWarningDialog: Boolean = false,
    val showCycleResetDialog: Boolean = false,
    val externalQuranDialog: ReviewExternalQuranDialogUiState? = null,
)

data class ReviewExternalQuranDialogUiState(
    val taskTitle: TextSpec,
    val target: QuranReadingTarget,
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
            weight = assignment.weight,
            readingTarget = assignment.readingTarget,
        )
    }
    val completedWeight = tasks.filter { it.isDone }.sumOf { task -> task.weight }
    val totalWeight = tasks.sumOf { task -> task.weight }
    val remainingWeight = totalWeight - completedWeight
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
        selectedTab = ReviewTab.Daily,
        fullPlanSortMode = ReviewFullPlanSortMode.Rating,
        progressCard = ReviewProgressCardUiState(
            completedText = formatReviewWeight(completedWeight),
            totalText = formatReviewWeight(totalWeight),
            remainingText = formatReviewWeight(remainingWeight),
            progress = if (totalWeight == 0.0) 0f else (completedWeight / totalWeight).toFloat(),
        ),
        sections = listOf(section),
        fullPlanTasks = tasks,
        fullPlanScrollToTopNonce = 0,
        showCycleResetDialog = false,
        externalQuranDialog = null,
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
            val completedWeight = visibleTasks.filter { it.isDone }.sumOf { task -> task.weight }
            val totalWeight = visibleTasks.sumOf { task -> task.weight }
            val remainingWeight = totalWeight - completedWeight

            ReviewUiState(
                isLoading = false,
                selectedTab = ReviewTab.Daily,
                fullPlanSortMode = ReviewFullPlanSortMode.Rating,
                progressCard = ReviewProgressCardUiState(
                    completedText = formatReviewWeight(completedWeight),
                    totalText = formatReviewWeight(totalWeight),
                    remainingText = formatReviewWeight(remainingWeight),
                progress = if (totalWeight == 0.0) 0f else (completedWeight / totalWeight).toFloat(),
            ),
            sections = visibleSections,
            fullPlanTasks = allSections.flatMap { section -> section.tasks },
            fullPlanScrollToTopNonce = 0,
            showCycleResetDialog = showCycleResetDialog,
            externalQuranDialog = null,
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
        weight = 1.0,
        readingTarget = QuranReadingTarget(
            startSurahId = 79,
            startAyah = 1,
            endSurahId = 79,
            endAyah = 46,
        ),
    )
}

internal fun formatReviewWeight(weight: Double): String {
    val normalized = if (weight < 0.0) 0.0 else weight
    val rounded = kotlin.math.round(normalized * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
