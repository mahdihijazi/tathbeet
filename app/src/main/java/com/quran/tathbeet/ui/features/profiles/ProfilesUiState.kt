package com.quran.tathbeet.ui.features.profiles

import androidx.annotation.StringRes
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.sync.AuthSessionStatus

data class ProfilesUiState(
    val isLoading: Boolean = true,
    val account: ProfilesAccountUiState = ProfilesAccountUiState(),
    val hasSeenScheduleIntro: Boolean = false,
    val activeProfile: ProfileCardUiState? = null,
    val profiles: List<ProfileCardUiState> = emptyList(),
    val editor: ProfileEditorUiState? = null,
    val deleteConfirmation: ProfileDeleteConfirmationUiState? = null,
)

data class ProfilesAccountUiState(
    val isRuntimeConfigured: Boolean = false,
    val status: AuthSessionStatus = AuthSessionStatus.SignedOut,
    val email: String? = null,
    val pendingEmail: String? = null,
)

data class ProfileCardUiState(
    val id: String,
    val name: String,
    val isSelfProfile: Boolean,
    val isShared: Boolean,
    val isActive: Boolean,
    val notificationsEnabled: Boolean,
    val syncMode: ProfileSyncMode = ProfileSyncMode.LocalOnly,
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
