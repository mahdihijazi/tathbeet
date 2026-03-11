package com.quran.tathbeet.ui.features.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quran.tathbeet.R
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.ui.model.TextSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReviewViewModel(
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private var mockState = ReviewMockFactory.initialState()

    private val _uiState = MutableStateFlow(mockState.toUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun requestCompleteTask(taskId: String) {
        val task = mockState.allSections.flatMap { it.tasks }.firstOrNull { it.id == taskId } ?: return
        mockState = mockState.copy(
            ratingDialogTaskId = taskId,
            ratingDialogSelected = task.rating ?: 5,
        )
        _uiState.value = mockState.toUiState()
    }

    fun updatePendingRating(rating: Int) {
        val taskId = mockState.ratingDialogTaskId ?: return
        applyCompletion(taskId = taskId, rating = rating)
    }

    fun dismissRatingDialog() {
        val taskId = mockState.ratingDialogTaskId ?: return
        applyCompletion(taskId = taskId, rating = mockState.ratingDialogSelected)
    }

    fun restartCycle() {
        mockState = ReviewMockFactory.initialState()
        _uiState.value = mockState.toUiState()
    }

    fun dismissCycleResetDialog() {
        mockState = mockState.copy(showCycleResetDialog = false)
        _uiState.value = mockState.toUiState()
    }

    private fun applyCompletion(
        taskId: String,
        rating: Int,
    ) {
        val updatedSections = mockState.allSections.map { section ->
            section.copy(
                tasks = section.tasks.map { task ->
                    if (task.id == taskId) {
                        task.copy(
                            isDone = true,
                            rating = rating,
                        )
                    } else {
                        task
                    }
                },
            ).withDerivedStatus()
        }

        val currentVisibleLastIndex = mockState.visibleSectionCount - 1
        val shouldRevealNextSection =
            currentVisibleLastIndex in updatedSections.indices &&
                updatedSections[currentVisibleLastIndex].tasks.all { it.isDone } &&
                mockState.visibleSectionCount < updatedSections.size

        val nextVisibleCount =
            if (shouldRevealNextSection) {
                mockState.visibleSectionCount + 1
            } else {
                mockState.visibleSectionCount
            }

        val allVisibleSectionsComplete = updatedSections.take(nextVisibleCount)
            .all { section -> section.tasks.all { it.isDone } }
        val reachedCycleEnd = nextVisibleCount == updatedSections.size

        mockState = mockState.copy(
            allSections = updatedSections,
            visibleSectionCount = nextVisibleCount,
            showCycleResetDialog = reachedCycleEnd && allVisibleSectionsComplete,
            ratingDialogTaskId = null,
            ratingDialogSelected = 5,
        )
        _uiState.value = mockState.toUiState()
    }

    private fun ReviewSectionUiState.withDerivedStatus(): ReviewSectionUiState =
        copy(
            status = TextSpec(
                if (tasks.all { it.isDone }) {
                    R.string.review_state_done
                } else {
                    R.string.review_state_available_now
                },
            ),
        )
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
