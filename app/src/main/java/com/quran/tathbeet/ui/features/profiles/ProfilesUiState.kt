package com.quran.tathbeet.ui.features.profiles

import androidx.annotation.StringRes

data class ProfilesUiState(
    val isLoading: Boolean = true,
    val hasSeenScheduleIntro: Boolean = false,
    val activeProfile: ProfileCardUiState? = null,
    val profiles: List<ProfileCardUiState> = emptyList(),
    val editor: ProfileEditorUiState? = null,
    val deleteConfirmation: ProfileDeleteConfirmationUiState? = null,
)

data class ProfileCardUiState(
    val id: String,
    val name: String,
    val isSelfProfile: Boolean,
    val isShared: Boolean,
    val isActive: Boolean,
    val notificationsEnabled: Boolean,
    @param:StringRes val paceLabelRes: Int?,
    val completionRate: Int,
    val todayCompletedCount: Int,
    val todayTotalCount: Int,
)

data class ProfileEditorUiState(
    val profileId: String?,
    val name: String,
    val canDelete: Boolean,
) {
    val isNew: Boolean
        get() = profileId == null
}

data class ProfileDeleteConfirmationUiState(
    val profileId: String,
    val profileName: String,
)
