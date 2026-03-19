package com.quran.tathbeet.ui.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.app.ReminderHadithCatalog
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.observeActiveAccount()
                .filterNotNull()
                .collectLatest { account ->
                    launch {
                        reviewRepository.ensureAssignmentsForDate(
                            learnerId = account.id,
                            assignedForDate = timeProvider.today(),
                        )
                    }
                    reviewRepository.observeReviewTimeline(account.id)
                        .collect { timeline ->
                            _uiState.value = timeline.toUiState(
                                today = timeProvider.today(),
                                motivationSeed = account.id.hashCode(),
                            )
                        }
                }
        }
    }
}

class ProgressViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val timeProvider: TimeProvider,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgressViewModel(
                profileRepository = profileRepository,
                reviewRepository = reviewRepository,
                timeProvider = timeProvider,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun List<ReviewDay>.toUiState(
    today: LocalDate,
    motivationSeed: Int,
): ProgressUiState {
    val overdueAssignments = filter { day -> day.assignedForDate.isBefore(today) }
        .flatMap { day -> day.assignments }
    val todayAssignments = firstOrNull { day -> day.assignedForDate == today }?.assignments.orEmpty()
    val visibleAssignments = overdueAssignments + todayAssignments
    val weeklyCompletionRates = (0..6).map { offset ->
        val date = today.minusDays((6 - offset).toLong())
        firstOrNull { day -> day.assignedForDate == date }?.completionRate ?: 0
    }
    val hadith = ReminderHadithCatalog.cardEntryFor(motivationSeed)

    return ProgressUiState(
        isLoading = false,
        todayCompleted = visibleAssignments.count { assignment -> assignment.isDone },
        todayTotal = visibleAssignments.size,
        remainingCount = visibleAssignments.count { assignment -> !assignment.isDone },
        completionRate = weeklyCompletionRates.average().toInt(),
        completedDays = weeklyCompletionRates.count { rate -> rate >= 100 },
        weekValues = weeklyCompletionRates.map { rate -> (rate / 100f).coerceIn(0f, 1f) },
        hasRollover = overdueAssignments.isNotEmpty(),
        motivationTextResId = hadith.textResId,
        motivationSourceResId = hadith.sourceResId,
    )
}
