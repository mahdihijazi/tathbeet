package com.quran.tathbeet.ui.features.schedule

import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.PaceMethod
import com.quran.tathbeet.ui.model.PaceOption
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.SelectionCategory

data class ScheduleWizardUiState(
    val isLoading: Boolean = true,
    val hasSeenScheduleIntro: Boolean = false,
    val isOnboarding: Boolean = true,
    val selectedCategory: SelectionCategory = SelectionCategory.Surahs,
    val selectedPool: List<QuranSelectionItem> = emptyList(),
    val paceMethod: PaceMethod = PaceMethod.CycleTarget,
    val selectedCycleTarget: CycleTarget = CycleTarget.OneMonth,
    val selectedPace: PaceOption = PaceOption.OneRub,
    val segmentCount: Int = 0,
    val cycleLength: Int = 1,
)
