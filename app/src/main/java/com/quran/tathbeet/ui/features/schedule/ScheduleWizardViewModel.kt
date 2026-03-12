package com.quran.tathbeet.ui.features.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ScheduleSelection
import com.quran.tathbeet.domain.planner.CoverageSelection
import com.quran.tathbeet.domain.planner.RevisionPlanner
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.QuranCatalogRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.domain.repository.SettingsRepository
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.ui.model.CycleTarget as UiCycleTarget
import com.quran.tathbeet.ui.model.PaceMethod as UiPaceMethod
import com.quran.tathbeet.ui.model.PaceOption as UiPaceOption
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.SelectionCategory as UiSelectionCategory
import com.quran.tathbeet.ui.model.selectionKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScheduleWizardViewModel(
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reviewRepository: ReviewRepository,
    private val settingsRepository: SettingsRepository,
    private val quranCatalogRepository: QuranCatalogRepository,
    private val revisionPlanner: RevisionPlanner,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val quranCatalog = quranCatalogRepository.getCatalog()
    private val _uiState = MutableStateFlow(ScheduleWizardUiState())
    val uiState: StateFlow<ScheduleWizardUiState> = _uiState.asStateFlow()

    private var selectedKeys: Set<String> = emptySet()
    private var loadedAccountId: String? = null

    init {
        viewModelScope.launch {
            profileRepository.observeActiveAccount()
                .filterNotNull()
                .collectLatest { account ->
                    combine(
                        settingsRepository.observeSettings(),
                        scheduleRepository.observeActiveSchedule(account.id),
                    ) { settings, schedule ->
                        Triple(account.id, settings.hasSeenScheduleIntro, schedule)
                    }.collect { (accountId, hasSeenIntro, schedule) ->
                        if (loadedAccountId != accountId) {
                            loadedAccountId = accountId
                            selectedKeys = schedule?.selections
                                ?.sortedBy { it.displayOrder }
                                ?.map { selection ->
                                    selectionKey(
                                        category = UiSelectionCategory.valueOf(selection.category.name),
                                        itemId = selection.itemId,
                                    )
                                }
                                ?.toSet()
                                .orEmpty()
                        }

                        val uiPaceMethod = schedule?.paceMethod?.toUiPaceMethod() ?: UiPaceMethod.CycleTarget
                        val uiCycleTarget = schedule?.cycleTarget?.toUiCycleTarget() ?: UiCycleTarget.OneMonth
                        val uiSelectedPace = if (schedule == null) {
                            recommendedPaceFor(selectedKeys, uiCycleTarget)
                        } else {
                            schedule.manualPace.toUiPaceOption()
                        }
                        val segmentCount = effectiveSegmentCount(selectedKeys)

                        _uiState.value = ScheduleWizardUiState(
                            isLoading = false,
                            hasSeenScheduleIntro = hasSeenIntro,
                            isOnboarding = schedule == null,
                            selectedCategory = _uiState.value.selectedCategory,
                            selectedPool = quranCatalog.resolveSelections(selectedKeys),
                            paceMethod = uiPaceMethod,
                            selectedCycleTarget = uiCycleTarget,
                            selectedPace = uiSelectedPace,
                            segmentCount = segmentCount,
                            cycleLength = calculateCycleLength(segmentCount, uiSelectedPace),
                        )
                    }
                }
        }
    }

    fun markIntroSeen() {
        viewModelScope.launch {
            settingsRepository.markScheduleIntroSeen()
        }
        _uiState.value = _uiState.value.copy(hasSeenScheduleIntro = true)
    }

    fun selectCategory(category: UiSelectionCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun toggleSelection(item: QuranSelectionItem) {
        selectedKeys = selectedKeys.toMutableSet().apply {
            if (!add(item.key)) {
                remove(item.key)
            }
        }
        syncDerivedState()
    }

    fun selectCycleTarget(cycleTarget: UiCycleTarget) {
        val recommendedPace = recommendedPaceFor(selectedKeys, cycleTarget)
        _uiState.value = _uiState.value.copy(
            paceMethod = UiPaceMethod.CycleTarget,
            selectedCycleTarget = cycleTarget,
            selectedPace = recommendedPace,
            cycleLength = calculateCycleLength(
                segmentCount = effectiveSegmentCount(selectedKeys),
                pace = recommendedPace,
            ),
        )
    }

    fun selectManualPace(paceOption: UiPaceOption) {
        _uiState.value = _uiState.value.copy(
            paceMethod = UiPaceMethod.Manual,
            selectedPace = paceOption,
            cycleLength = calculateCycleLength(
                segmentCount = effectiveSegmentCount(selectedKeys),
                pace = paceOption,
            ),
        )
    }

    fun resetToCycleMode() {
        selectCycleTarget(_uiState.value.selectedCycleTarget)
    }

    fun saveSchedule(onSaved: () -> Unit) {
        viewModelScope.launch {
            val activeAccount = profileRepository.observeActiveAccount().filterNotNull().first()
            val selections = quranCatalog.resolveSelections(selectedKeys)
                .sortedBy { it.order }
                .mapIndexed { index, item ->
                    ScheduleSelection(
                        category = item.category.toDomainSelectionCategory(),
                        itemId = item.itemId,
                        displayOrder = index,
                    )
                }

            scheduleRepository.saveSchedule(
                RevisionSchedule(
                    id = "active-${activeAccount.id}",
                    learnerId = activeAccount.id,
                    paceMethod = _uiState.value.paceMethod.toDomainPaceMethod(),
                    cycleTarget = _uiState.value.selectedCycleTarget.toDomainCycleTarget(),
                    manualPace = _uiState.value.selectedPace.toDomainPaceOption(),
                    selections = selections,
                ),
            )
            reviewRepository.refreshForScheduleChange(
                learnerId = activeAccount.id,
                restartDate = timeProvider.today(),
            )
            onSaved()
        }
    }

    private fun syncDerivedState() {
        val segmentCount = effectiveSegmentCount(selectedKeys)
        val pace = if (_uiState.value.paceMethod == UiPaceMethod.CycleTarget) {
            recommendedPaceFor(selectedKeys, _uiState.value.selectedCycleTarget)
        } else {
            _uiState.value.selectedPace
        }

        _uiState.value = _uiState.value.copy(
            selectedPool = quranCatalog.resolveSelections(selectedKeys),
            selectedPace = pace,
            segmentCount = segmentCount,
            cycleLength = calculateCycleLength(segmentCount, pace),
        )
    }

    private fun recommendedPaceFor(
        keys: Set<String>,
        cycleTarget: UiCycleTarget,
    ): UiPaceOption =
        revisionPlanner.recommendPace(
            segmentCount = effectiveSegmentCount(keys),
            cycleTarget = cycleTarget.toDomainCycleTarget(),
        ).toUiPaceOption()

    private fun effectiveSegmentCount(keys: Set<String>): Int {
        val selections = quranCatalog.resolveSelections(keys).map { item ->
            CoverageSelection(
                category = item.category.toDomainSelectionCategory(),
                itemId = item.itemId,
                firstRubId = item.firstRubId,
                lastRubId = item.lastRubId,
            )
        }
        return revisionPlanner.effectiveSegmentCount(selections)
    }

    private fun calculateCycleLength(
        segmentCount: Int,
        pace: UiPaceOption,
    ): Int = ((segmentCount + pace.dailySegments - 1) / pace.dailySegments).coerceAtLeast(1)
}

class ScheduleWizardViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reviewRepository: ReviewRepository,
    private val settingsRepository: SettingsRepository,
    private val quranCatalogRepository: QuranCatalogRepository,
    private val revisionPlanner: RevisionPlanner,
    private val timeProvider: TimeProvider,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleWizardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleWizardViewModel(
                profileRepository = profileRepository,
                scheduleRepository = scheduleRepository,
                reviewRepository = reviewRepository,
                settingsRepository = settingsRepository,
                quranCatalogRepository = quranCatalogRepository,
                revisionPlanner = revisionPlanner,
                timeProvider = timeProvider,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun com.quran.tathbeet.domain.model.PaceMethod.toUiPaceMethod(): UiPaceMethod =
    UiPaceMethod.valueOf(name)

private fun com.quran.tathbeet.domain.model.CycleTarget.toUiCycleTarget(): UiCycleTarget =
    UiCycleTarget.entries.first { it.days == days }

private fun com.quran.tathbeet.domain.model.PaceOption.toUiPaceOption(): UiPaceOption =
    UiPaceOption.entries.first { it.dailySegments == dailySegments }

private fun UiPaceMethod.toDomainPaceMethod(): com.quran.tathbeet.domain.model.PaceMethod =
    com.quran.tathbeet.domain.model.PaceMethod.valueOf(name)

private fun UiCycleTarget.toDomainCycleTarget(): com.quran.tathbeet.domain.model.CycleTarget =
    com.quran.tathbeet.domain.model.CycleTarget.entries.first { it.days == days }

private fun UiPaceOption.toDomainPaceOption(): com.quran.tathbeet.domain.model.PaceOption =
    com.quran.tathbeet.domain.model.PaceOption.entries.first { it.dailySegments == dailySegments }

private fun UiSelectionCategory.toDomainSelectionCategory(): com.quran.tathbeet.domain.model.SelectionCategory =
    com.quran.tathbeet.domain.model.SelectionCategory.valueOf(name)
