package com.quran.tathbeet.ui.features.progress

import com.quran.tathbeet.R

data class ProgressUiState(
    val isLoading: Boolean = true,
    val todayCompleted: Int = 0,
    val todayTotal: Int = 0,
    val remainingCount: Int = 0,
    val completionRate: Int = 0,
    val completedDays: Int = 0,
    val weekValues: List<Float> = List(7) { 0f },
    val hasRollover: Boolean = false,
    val motivationTextResId: Int = R.string.reminder_hadith_card_1_text,
    val motivationSourceResId: Int = R.string.reminder_hadith_card_1_source,
)
