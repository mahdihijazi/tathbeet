package com.quran.tathbeet.ui.features.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.R
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.ui.model.TextSpec
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private var currentLearnerId: String? = null
    private var timeline: List<ReviewDay> = emptyList()
    private var ratingDialogTaskId: String? = null
    private var pendingRating: Int = 5
    private var showCycleResetDialog: Boolean = false

    init {
        viewModelScope.launch {
            profileRepository.observeActiveAccount()
                .filterNotNull()
                .collectLatest { account ->
                    currentLearnerId = account.id
                    ratingDialogTaskId = null
                    pendingRating = 5
                    showCycleResetDialog = false
                    reviewRepository.ensureAssignmentsForDate(
                        learnerId = account.id,
                        assignedForDate = timeProvider.today(),
                    )
                    reviewRepository.observeReviewTimeline(account.id)
                        .collect { reviewDays ->
                            timeline = reviewDays.sortedBy { it.assignedForDate }
                            maybeExtendTimeline()
                            publishUiState()
                        }
                }
        }
    }

    fun requestCompleteTask(taskId: String) {
        val task = allAssignments().firstOrNull { it.id == taskId } ?: return
        ratingDialogTaskId = taskId
        pendingRating = task.rating ?: 5
        publishUiState()
    }

    fun updatePendingRating(rating: Int) {
        val task = currentDialogTask() ?: return
        viewModelScope.launch {
            if (task.isDone) {
                reviewRepository.updateAssignmentRating(task.id, rating)
            } else {
                reviewRepository.completeAssignment(task.id, rating)
            }
            ratingDialogTaskId = null
            pendingRating = 5
            showCycleResetDialog = false
            publishUiState()
        }
    }

    fun dismissRatingDialog() {
        val task = currentDialogTask() ?: return
        viewModelScope.launch {
            if (!task.isDone) {
                reviewRepository.completeAssignment(task.id, pendingRating)
            }
            ratingDialogTaskId = null
            pendingRating = 5
            showCycleResetDialog = false
            publishUiState()
        }
    }

    fun restartCycle() {
        val learnerId = currentLearnerId ?: return
        viewModelScope.launch {
            showCycleResetDialog = false
            ratingDialogTaskId = null
            pendingRating = 5
            reviewRepository.restartCycle(
                learnerId = learnerId,
                restartDate = timeProvider.today(),
            )
        }
    }

    fun dismissCycleResetDialog() {
        showCycleResetDialog = false
        publishUiState()
    }

    private suspend fun maybeExtendTimeline() {
        val learnerId = currentLearnerId ?: return
        val today = timeProvider.today()
        val currentAndFutureDays = timeline
            .filter { !it.assignedForDate.isBefore(today) }
            .sortedBy { it.assignedForDate }

        if (currentAndFutureDays.isEmpty()) {
            return
        }

        val visibleDays = buildVisibleCurrentAndFutureDays(currentAndFutureDays)
        val allVisibleComplete = visibleDays.isNotEmpty() &&
            visibleDays.all { day -> day.assignments.all { it.isDone } }

        if (!allVisibleComplete) {
            showCycleResetDialog = false
            return
        }

        val lastVisibleDate = visibleDays.last().assignedForDate
        val nextDate = lastVisibleDate.plusDays(1)
        val nextExists = currentAndFutureDays.any { it.assignedForDate == nextDate }
        if (nextExists) {
            showCycleResetDialog = false
            return
        }

        val createdNextDay = reviewRepository.ensureAssignmentsForDate(
            learnerId = learnerId,
            assignedForDate = nextDate,
        )
        if (!createdNextDay) {
            showCycleResetDialog = true
        }
    }

    private fun publishUiState() {
        val today = timeProvider.today()
        val overdueAssignments = timeline
            .filter { it.assignedForDate.isBefore(today) }
            .flatMap { it.assignments }

        val currentAndFutureDays = timeline
            .filter { !it.assignedForDate.isBefore(today) }
            .sortedBy { it.assignedForDate }

        val visibleCurrentAndFutureDays = buildVisibleCurrentAndFutureDays(currentAndFutureDays)
        val visibleSections = buildList {
            if (overdueAssignments.isNotEmpty()) {
                add(
                    ReviewSectionUiState(
                        id = "overdue",
                        title = TextSpec(R.string.review_rollover_chip),
                        status = sectionStatusFor(overdueAssignments),
                        tasks = overdueAssignments.sortedBy { it.assignedForDate }
                            .thenByDisplayOrder(),
                    ),
                )
            }

            visibleCurrentAndFutureDays.forEach { day ->
                add(
                    ReviewSectionUiState(
                        id = day.assignedForDate.toString(),
                        title = sectionTitleFor(day.assignedForDate, today),
                        status = sectionStatusFor(day.assignments),
                        tasks = day.assignments.sortedBy { it.displayOrder }
                            .map { assignment -> assignment.toTaskUiState() },
                    ),
                )
            }
        }

        val progressAssignments = overdueAssignments + (visibleCurrentAndFutureDays
            .firstOrNull { it.assignedForDate == today }
            ?.assignments
            .orEmpty())
        val completedCount = progressAssignments.count { it.isDone }
        val totalCount = progressAssignments.size

        _uiState.value = ReviewUiState(
            isLoading = false,
            progressCard = ReviewProgressCardUiState(
                completedCount = completedCount,
                totalCount = totalCount,
                remainingCount = totalCount - completedCount,
                progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat(),
            ),
            sections = visibleSections,
            showCycleResetDialog = showCycleResetDialog,
            ratingDialogTask = ratingDialogTaskId?.let { taskId ->
                visibleSections.flatMap { it.tasks }.firstOrNull { it.id == taskId }
            },
            ratingDialogSelected = pendingRating,
        )
    }

    private fun buildVisibleCurrentAndFutureDays(
        days: List<ReviewDay>,
    ): List<ReviewDay> {
        if (days.isEmpty()) return emptyList()

        val visible = mutableListOf<ReviewDay>()
        var canRevealNext = true
        for (day in days) {
            if (!canRevealNext && visible.isNotEmpty()) break
            visible += day
            canRevealNext = day.assignments.all { it.isDone }
        }
        return visible
    }

    private fun sectionTitleFor(
        date: LocalDate,
        today: LocalDate,
    ): TextSpec = when (date) {
        today -> TextSpec(R.string.review_section_today_title)
        today.plusDays(1) -> TextSpec(R.string.review_section_next_title)
        else -> TextSpec(rawText = date.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ar"))))
    }

    private fun sectionStatusFor(assignments: List<ReviewAssignment>): TextSpec =
        TextSpec(
            if (assignments.isNotEmpty() && assignments.all { it.isDone }) {
                R.string.review_state_done
            } else {
                R.string.review_state_available_now
            },
        )

    private fun ReviewAssignment.toTaskUiState(): ReviewTaskUiState =
        ReviewTaskUiState(
            id = id,
            title = TextSpec(rawText = title),
            detail = TextSpec(rawText = detail),
            isDone = isDone,
            rating = rating,
        )

    private fun currentDialogTask(): ReviewAssignment? =
        ratingDialogTaskId?.let { taskId ->
            allAssignments().firstOrNull { it.id == taskId }
        }

    private fun allAssignments(): List<ReviewAssignment> =
        timeline.flatMap { it.assignments }

    private fun List<ReviewAssignment>.thenByDisplayOrder(): List<ReviewTaskUiState> =
        sortedWith(
            compareBy<ReviewAssignment>({ it.assignedForDate }, { it.displayOrder }),
        ).map { assignment -> assignment.toTaskUiState() }
}

class ReviewViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val timeProvider: TimeProvider,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(
                profileRepository = profileRepository,
                reviewRepository = reviewRepository,
                timeProvider = timeProvider,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
