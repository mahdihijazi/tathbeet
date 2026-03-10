package com.quran.tathbeet.ui.features.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
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

    init {
        viewModelScope.launch {
            profileRepository.observeActiveAccount()
                .filterNotNull()
                .collectLatest { account ->
                    reviewRepository.ensureAssignmentsForDate(
                        learnerId = account.id,
                        assignedForDate = timeProvider.today(),
                    )
                    reviewRepository.observeReviewDay(
                        learnerId = account.id,
                        assignedForDate = timeProvider.today(),
                    ).collect { reviewDay ->
                        _uiState.value = if (reviewDay == null) {
                            ReviewUiState(isLoading = false)
                        } else {
                            ReviewUiState(
                                isLoading = false,
                                completionRate = reviewDay.completionRate,
                                tasks = reviewDay.assignments.map { assignment ->
                                    ReviewTaskUiState(
                                        id = assignment.id,
                                        title = assignment.title,
                                        detail = assignment.detail,
                                        isDone = assignment.isDone,
                                        isRollover = assignment.isRollover,
                                    )
                                },
                            )
                        }
                    }
                }
        }
    }

    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            reviewRepository.toggleAssignmentCompletion(taskId)
        }
    }
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
