package com.quran.tathbeet.ui.features.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.R
import com.quran.tathbeet.app.QuranExternalLauncher
import com.quran.tathbeet.app.QuranLaunchResult
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
    private val quranExternalLauncher: QuranExternalLauncher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private var currentLearnerId: String? = null
    private var timeline: List<ReviewDay> = emptyList()
    private var showCycleResetWarningDialog: Boolean = false
    private var showCycleResetDialog: Boolean = false
    private var cycleResetDialogDismissed: Boolean = false
    private var externalQuranDialog: ReviewExternalQuranDialogUiState? = null

    init {
        viewModelScope.launch {
            profileRepository.observeActiveAccount()
                .filterNotNull()
                .collectLatest { account ->
                    currentLearnerId = account.id
                    showCycleResetWarningDialog = false
                    showCycleResetDialog = false
                    cycleResetDialogDismissed = false
                    externalQuranDialog = null
                    reviewRepository.ensureAssignmentsForDate(
                        learnerId = account.id,
                        assignedForDate = timeProvider.today(),
                    )
                    reviewRepository.observeReviewTimeline(account.id)
                        .collect { reviewDays ->
                            timeline = reviewDays.sortedBy { it.assignedForDate }
                            publishUiState()
                        }
                }
        }
    }

    fun requestCompleteTask(taskId: String) {
        val task = allAssignments().firstOrNull { it.id == taskId } ?: return
        if (task.isDone) return
        viewModelScope.launch {
            reviewRepository.completeAssignment(task.id, task.rating ?: 3)
        }
    }

    fun updateTaskRating(taskId: String, rating: Int) {
        val task = allAssignments().firstOrNull { it.id == taskId } ?: return
        viewModelScope.launch {
            if (task.isDone) {
                reviewRepository.updateAssignmentRating(task.id, rating)
            } else {
                reviewRepository.completeAssignment(task.id, rating)
            }
        }
    }

    fun launchTaskReading(taskId: String) {
        val task = allAssignments().firstOrNull { it.id == taskId } ?: return
        val readingTarget = task.readingTarget ?: return
        when (quranExternalLauncher.openReadingTarget(readingTarget)) {
            QuranLaunchResult.LaunchedInApp -> {
                externalQuranDialog = null
                publishUiState()
            }
            QuranLaunchResult.ShowFallbackOptions -> {
                externalQuranDialog = ReviewExternalQuranDialogUiState(
                    taskTitle = TextSpec(rawText = task.title),
                    target = readingTarget,
                )
                publishUiState()
            }
        }
    }

    fun dismissExternalQuranDialog() {
        externalQuranDialog = null
        publishUiState()
    }

    fun openQuranAndroidInstall() {
        quranExternalLauncher.openQuranAndroidInstallPage()
        dismissExternalQuranDialog()
    }

    fun openQuranOnWeb() {
        externalQuranDialog?.target?.let(quranExternalLauncher::openReadingTargetOnWeb)
        dismissExternalQuranDialog()
    }

    fun restartCycle() {
        val learnerId = currentLearnerId ?: return
        viewModelScope.launch {
            showCycleResetWarningDialog = false
            showCycleResetDialog = false
            cycleResetDialogDismissed = false
            reviewRepository.restartCycle(
                learnerId = learnerId,
                restartDate = timeProvider.today(),
            )
        }
    }

    fun requestCycleReset() {
        showCycleResetWarningDialog = true
        publishUiState()
    }

    fun dismissCycleResetWarning() {
        showCycleResetWarningDialog = false
        publishUiState()
    }

    fun dismissCycleResetDialog() {
        showCycleResetDialog = false
        cycleResetDialogDismissed = true
        publishUiState()
    }

    private fun publishUiState() {
        val today = timeProvider.today()
        val overdueAssignments = timeline
            .filter { it.assignedForDate.isBefore(today) }
            .flatMap { it.assignments }

        val currentAndFutureDays = timeline
            .filter { !it.assignedForDate.isBefore(today) }
            .sortedBy { it.assignedForDate }
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

            currentAndFutureDays.forEach { day ->
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

        val progressAssignments = overdueAssignments + (currentAndFutureDays
            .firstOrNull { it.assignedForDate == today }
            ?.assignments
            .orEmpty())
        val completedWeight = progressAssignments.filter { it.isDone }.sumOf { assignment -> assignment.weight }
        val totalWeight = progressAssignments.sumOf { assignment -> assignment.weight }
        val allCycleAssignments = overdueAssignments + currentAndFutureDays.flatMap { it.assignments }
        val allDone = allCycleAssignments.isNotEmpty() && allCycleAssignments.all { it.isDone }
        if (!allDone) {
            cycleResetDialogDismissed = false
        }
        showCycleResetDialog = allDone && !cycleResetDialogDismissed

        _uiState.value = ReviewUiState(
            isLoading = false,
            progressCard = ReviewProgressCardUiState(
                completedText = formatReviewWeight(completedWeight),
                totalText = formatReviewWeight(totalWeight),
                remainingText = formatReviewWeight(totalWeight - completedWeight),
                progress = if (totalWeight == 0.0) 0f else (completedWeight / totalWeight).toFloat(),
            ),
            sections = visibleSections,
            showCycleResetWarningDialog = showCycleResetWarningDialog,
            showCycleResetDialog = showCycleResetDialog,
            externalQuranDialog = externalQuranDialog,
        )
    }

    private fun sectionTitleFor(
        date: LocalDate,
        today: LocalDate,
    ): TextSpec = when (date) {
        today -> TextSpec(R.string.review_section_today_title)
        today.plusDays(1) -> TextSpec(R.string.review_section_next_title)
        else -> TextSpec(
            rawText = date.format(
                DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ar")),
            ),
        )
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
            defaultRating = rating ?: 3,
            weight = weight,
            readingTarget = readingTarget,
        )

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
    private val quranExternalLauncher: QuranExternalLauncher,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(
                profileRepository = profileRepository,
                reviewRepository = reviewRepository,
                timeProvider = timeProvider,
                quranExternalLauncher = quranExternalLauncher,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
