package com.quran.tathbeet.ui.features.profiles

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val ProfilesCardPreviewWidth = 411
private const val ProfilesSelfCardPreviewHeight = 560
private const val ProfilesChildCardPreviewHeight = 440

@PreviewTest
@Preview(
    name = "profiles_self_profile_card_signed_in",
    locale = "ar",
    widthDp = ProfilesCardPreviewWidth,
    heightDp = ProfilesSelfCardPreviewHeight,
    showBackground = true,
)
@Composable
fun ProfilesSelfProfileCardSignedInScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        ProfilesSelfProfileCard(
            profile = selfProfile(isActive = true),
            account = signedInAccount(),
            onActivate = {},
            onOpenDetails = {},
            onToggleNotifications = {},
            onRequestEmailLink = {},
            onSignOut = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_self_profile_card_signed_in_dark",
    locale = "ar",
    widthDp = ProfilesCardPreviewWidth,
    heightDp = ProfilesSelfCardPreviewHeight,
    showBackground = true,
)
@Composable
fun ProfilesSelfProfileCardSignedInDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        ProfilesSelfProfileCard(
            profile = selfProfile(isActive = true),
            account = signedInAccount(),
            onActivate = {},
            onOpenDetails = {},
            onToggleNotifications = {},
            onRequestEmailLink = {},
            onSignOut = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_self_profile_card_signed_out",
    locale = "ar",
    widthDp = ProfilesCardPreviewWidth,
    heightDp = ProfilesSelfCardPreviewHeight,
    showBackground = true,
)
@Composable
fun ProfilesSelfProfileCardSignedOutScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        ProfilesSelfProfileCard(
            profile = selfProfile(isActive = true),
            account = signedOutAccount(),
            onActivate = {},
            onOpenDetails = {},
            onToggleNotifications = {},
            onRequestEmailLink = {},
            onSignOut = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_self_profile_card_signed_out_dark",
    locale = "ar",
    widthDp = ProfilesCardPreviewWidth,
    heightDp = ProfilesSelfCardPreviewHeight,
    showBackground = true,
)
@Composable
fun ProfilesSelfProfileCardSignedOutDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        ProfilesSelfProfileCard(
            profile = selfProfile(isActive = true),
            account = signedOutAccount(),
            onActivate = {},
            onOpenDetails = {},
            onToggleNotifications = {},
            onRequestEmailLink = {},
            onSignOut = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_child_profile_card_local",
    locale = "ar",
    widthDp = ProfilesCardPreviewWidth,
    heightDp = ProfilesChildCardPreviewHeight,
    showBackground = true,
)
@Composable
fun ProfilesChildProfileCardLocalScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        ProfileCard(
            profile = childProfile(
                id = "child-local",
                name = "أحمد",
                isShared = false,
                isActive = false,
                syncMode = ProfileSyncMode.LocalOnly,
            ),
            onActivate = {},
            onOpenDetails = {},
            onToggleNotifications = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_child_profile_card_local_dark",
    locale = "ar",
    widthDp = ProfilesCardPreviewWidth,
    heightDp = ProfilesChildCardPreviewHeight,
    showBackground = true,
)
@Composable
fun ProfilesChildProfileCardLocalDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        ProfileCard(
            profile = childProfile(
                id = "child-local",
                name = "أحمد",
                isShared = false,
                isActive = false,
                syncMode = ProfileSyncMode.LocalOnly,
            ),
            onActivate = {},
            onOpenDetails = {},
            onToggleNotifications = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_child_profile_card_shared",
    locale = "ar",
    widthDp = ProfilesCardPreviewWidth,
    heightDp = ProfilesChildCardPreviewHeight,
    showBackground = true,
)
@Composable
fun ProfilesChildProfileCardSharedScreenshot() {
    ThemedScreenshotFrame(darkTheme = false) {
        ProfileCard(
            profile = childProfile(
                id = "child-shared",
                name = "خالد",
                isShared = true,
                isActive = false,
                syncMode = ProfileSyncMode.SharedOwner,
            ),
            onActivate = {},
            onOpenDetails = {},
            onToggleNotifications = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "profiles_child_profile_card_shared_dark",
    locale = "ar",
    widthDp = ProfilesCardPreviewWidth,
    heightDp = ProfilesChildCardPreviewHeight,
    showBackground = true,
)
@Composable
fun ProfilesChildProfileCardSharedDarkScreenshot() {
    ThemedScreenshotFrame(darkTheme = true) {
        ProfileCard(
            profile = childProfile(
                id = "child-shared",
                name = "خالد",
                isShared = true,
                isActive = false,
                syncMode = ProfileSyncMode.SharedOwner,
            ),
            onActivate = {},
            onOpenDetails = {},
            onToggleNotifications = {},
        )
    }
}

private fun selfProfile(isActive: Boolean) = ProfileCardUiState(
    id = "self",
    name = "حسابي",
    isSelfProfile = true,
    isShared = false,
    isActive = isActive,
    notificationsEnabled = true,
    syncMode = ProfileSyncMode.SoloSynced,
    paceLabelRes = null,
    completionRate = 50,
    todayCompletedCount = 0,
    todayTotalCount = 1,
)

private fun signedInAccount() = ProfilesAccountUiState(
    isRuntimeConfigured = true,
    status = AuthSessionStatus.SignedIn,
    email = "mahdi.hijaz@hotmail.com",
)

private fun signedOutAccount() = ProfilesAccountUiState(
    isRuntimeConfigured = true,
    status = AuthSessionStatus.SignedOut,
)

private fun childProfile(
    id: String,
    name: String,
    isShared: Boolean,
    isActive: Boolean,
    syncMode: ProfileSyncMode,
) = ProfileCardUiState(
    id = id,
    name = name,
    isSelfProfile = false,
    isShared = isShared,
    isActive = isActive,
    notificationsEnabled = true,
    syncMode = syncMode,
    paceLabelRes = null,
    completionRate = 0,
    todayCompletedCount = 0,
    todayTotalCount = 0,
)
