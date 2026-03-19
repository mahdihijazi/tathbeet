package com.quran.tathbeet.ui.features.shared

import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.sync.CloudProfileMemberRole

data class SharedProfileUiState(
    val isLoading: Boolean = true,
    val profileId: String? = null,
    val profileName: String = "",
    val isSelfProfile: Boolean = false,
    val syncMode: ProfileSyncMode = ProfileSyncMode.LocalOnly,
    val authStatus: AuthSessionStatus = AuthSessionStatus.SignedOut,
    val signedInEmail: String? = null,
    val canEnableSharing: Boolean = false,
    val canInviteEditors: Boolean = false,
    val canLeaveProfile: Boolean = false,
    val members: List<SharedProfileMemberUiState> = emptyList(),
    val banner: SharedProfileBanner? = null,
)

data class SharedProfileMemberUiState(
    val email: String,
    val role: CloudProfileMemberRole,
    val isCurrentUser: Boolean,
)

enum class SharedProfileBanner {
    SharedEnabled,
    InviteSent,
    EditorRemoved,
    LeftProfile,
    EditorsMustBeRemovedFirst,
    SignInRequired,
    ShareUnavailable,
}
