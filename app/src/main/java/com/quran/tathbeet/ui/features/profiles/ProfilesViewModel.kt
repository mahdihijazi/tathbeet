package com.quran.tathbeet.ui.features.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.R
import com.quran.tathbeet.app.LocalReminderScheduler
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.domain.repository.SettingsRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ProfilesViewModel(
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reviewRepository: ReviewRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
    private val localReminderScheduler: LocalReminderScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilesUiState())
    val uiState: StateFlow<ProfilesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                profileRepository.observeAccounts(),
                profileRepository.observeActiveAccount(),
                settingsRepository.observeSettings(),
            ) { accounts, activeAccount, settings ->
                Triple(accounts, activeAccount, settings)
            }.collectLatest { (accounts, activeAccount, settings) ->
                val today = timeProvider.today()
                accounts.forEach { account ->
                    val schedule = scheduleRepository.observeActiveSchedule(account.id).first()
                    if (schedule != null) {
                        reviewRepository.ensureAssignmentsForDate(
                            learnerId = account.id,
                            assignedForDate = today,
                        )
                    }
                }

                val profileSummaryFlows = accounts.map { account ->
                    combine(
                        scheduleRepository.observeActiveSchedule(account.id),
                        reviewRepository.observeReviewTimeline(account.id),
                    ) { schedule, timeline ->
                        account.toCardUiState(
                            schedule = schedule,
                            timeline = timeline,
                            today = today,
                            isActive = account.id == activeAccount?.id,
                        )
                    }
                }

                val summariesFlow = if (profileSummaryFlows.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(profileSummaryFlows) { summaries -> summaries.toList() }
                }

                summariesFlow.collect { profiles ->
                    val sortedProfiles = profiles.sortedWith(
                        compareByDescending<ProfileCardUiState> { it.isActive }
                            .thenBy { it.name },
                    )

                    _uiState.value = ProfilesUiState(
                        isLoading = false,
                        hasSeenScheduleIntro = settings.hasSeenScheduleIntro,
                        activeProfile = sortedProfiles.firstOrNull { it.isActive } ?: sortedProfiles.firstOrNull(),
                        profiles = sortedProfiles,
                        editor = _uiState.value.editor?.let { editor ->
                            if (editor.profileId == null || sortedProfiles.any { it.id == editor.profileId }) {
                                editor
                            } else {
                                null
                            }
                        },
                        deleteConfirmation = _uiState.value.deleteConfirmation?.takeIf { confirmation ->
                            sortedProfiles.any { it.id == confirmation.profileId }
                        },
                    )
                }
            }
        }
    }

    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.setActiveAccount(profileId)
        }
    }

    fun toggleProfileNotifications(profileId: String) {
        val profile = _uiState.value.profiles.firstOrNull { it.id == profileId } ?: return
        viewModelScope.launch {
            profileRepository.updateNotificationsEnabled(
                accountId = profileId,
                enabled = !profile.notificationsEnabled,
            )
            localReminderScheduler.syncSchedules()
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(
            editor = ProfileEditorUiState(
                profileId = null,
                name = "",
                canDelete = false,
            ),
        )
    }

    fun showEditActiveProfileDialog() {
        val activeProfile = _uiState.value.activeProfile ?: return
        _uiState.value = _uiState.value.copy(
            editor = ProfileEditorUiState(
                profileId = activeProfile.id,
                name = activeProfile.name,
                canDelete = !activeProfile.isSelfProfile,
            ),
        )
    }

    fun updateEditorName(name: String) {
        val editor = _uiState.value.editor ?: return
        _uiState.value = _uiState.value.copy(
            editor = editor.copy(name = name),
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(editor = null)
    }

    fun saveEditor(onCreated: () -> Unit = {}) {
        val editor = _uiState.value.editor ?: return
        val trimmedName = editor.name.trim()
        if (trimmedName.isBlank()) {
            return
        }

        viewModelScope.launch {
            if (editor.isNew) {
                profileRepository.createProfile(trimmedName)
                _uiState.value = _uiState.value.copy(editor = null)
                localReminderScheduler.syncSchedules()
                onCreated()
            } else {
                profileRepository.updateAccountName(
                    accountId = editor.profileId!!,
                    name = trimmedName,
                )
                _uiState.value = _uiState.value.copy(editor = null)
                localReminderScheduler.syncSchedules()
            }
        }
    }

    fun requestDeleteFromEditor() {
        val editor = _uiState.value.editor ?: return
        if (editor.profileId == null || !editor.canDelete) {
            return
        }

        _uiState.value = _uiState.value.copy(
            deleteConfirmation = ProfileDeleteConfirmationUiState(
                profileId = editor.profileId,
                profileName = editor.name.trim().ifBlank { editor.name },
            ),
        )
    }

    fun dismissDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(deleteConfirmation = null)
    }

    fun confirmDelete() {
        val confirmation = _uiState.value.deleteConfirmation ?: return
        viewModelScope.launch {
            localReminderScheduler.cancelProfile(confirmation.profileId)
            profileRepository.deleteProfile(confirmation.profileId)
            _uiState.value = _uiState.value.copy(
                editor = null,
                deleteConfirmation = null,
            )
            localReminderScheduler.syncSchedules()
        }
    }

    private fun com.quran.tathbeet.domain.model.LearnerAccount.toCardUiState(
        schedule: RevisionSchedule?,
        timeline: List<ReviewDay>,
        today: LocalDate,
        isActive: Boolean,
    ): ProfileCardUiState {
        val todayAssignments = timeline.firstOrNull { day -> day.assignedForDate == today }?.assignments.orEmpty()
        val lastSevenCompletionRates = (0..6).map { offset ->
            val date = today.minusDays((6 - offset).toLong())
            timeline.firstOrNull { reviewDay -> reviewDay.assignedForDate == date }?.completionRate ?: 0
        }

        return ProfileCardUiState(
            id = id,
            name = name,
            isSelfProfile = isSelfProfile,
            isActive = isActive,
            notificationsEnabled = notificationsEnabled,
            paceLabelRes = schedule?.manualPace?.toLabelRes(),
            completionRate = lastSevenCompletionRates.average().toInt(),
            todayCompletedCount = todayAssignments.count { assignment -> assignment.isDone },
            todayTotalCount = todayAssignments.size,
        )
    }
}

class ProfilesViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reviewRepository: ReviewRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
    private val localReminderScheduler: LocalReminderScheduler,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfilesViewModel(
                profileRepository = profileRepository,
                scheduleRepository = scheduleRepository,
                reviewRepository = reviewRepository,
                settingsRepository = settingsRepository,
                timeProvider = timeProvider,
                localReminderScheduler = localReminderScheduler,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun PaceOption.toLabelRes(): Int = when (this) {
    PaceOption.OneRub -> R.string.pace_one_rub
    PaceOption.OneHizb -> R.string.pace_one_hizb
    PaceOption.OneJuz -> R.string.pace_one_juz
    PaceOption.OneAndHalfJuz -> R.string.pace_one_and_half_juz
    PaceOption.TwoJuz -> R.string.pace_two_juz
    PaceOption.TwoAndHalfJuz -> R.string.pace_two_and_half_juz
    PaceOption.ThreeJuz -> R.string.pace_three_juz
    PaceOption.FourJuz -> R.string.pace_four_juz
    PaceOption.FiveJuz -> R.string.pace_five_juz
}
