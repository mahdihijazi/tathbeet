package com.quran.tathbeet.feature.profiles

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.R
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.sync.AuthSessionStatus
import com.quran.tathbeet.ui.features.profiles.ProfilesAccountUiState
import com.quran.tathbeet.ui.features.profiles.ProfileCardUiState
import com.quran.tathbeet.ui.features.profiles.ProfilesScreen
import com.quran.tathbeet.ui.features.profiles.ProfilesUiState
import com.quran.tathbeet.ui.theme.TathbeetTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfilesScreenShareActionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun child_profile_card_shows_manage_account_action() {
        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
                        account = ProfilesAccountUiState(),
                        activeProfile = profile(
                            id = "self",
                            name = "حسابي",
                            isSelfProfile = true,
                            isActive = true,
                        ),
                        profiles = listOf(
                            profile(
                                id = "self",
                                name = "حسابي",
                                isSelfProfile = true,
                                isActive = true,
                            ),
                            profile(
                                id = "child-1",
                                name = "أحمد",
                                isSelfProfile = false,
                                isShared = false,
                                isActive = false,
                            ),
                        ),
                    ),
                    onProfileSelected = {},
                    onProfileNotificationsToggled = {},
                    onAddProfileRequested = {},
                    onEditProfileRequested = {},
                    onOpenSharedProfile = {},
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = { _ -> },
                    onRequestEmailLink = {},
                    onSignOut = {},
                )
            }
        }

        composeRule.onNodeWithTag("profiles-card-child-1").performClick()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_button_manage_account),
        ).assertIsDisplayed()
    }

    @Test
    fun child_profile_activate_chip_activates_profile_directly() {
        var activatedProfileId: String? = null

        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
                        account = ProfilesAccountUiState(),
                        activeProfile = profile(
                            id = "self",
                            name = "حسابي",
                            isSelfProfile = true,
                            isActive = true,
                        ),
                        profiles = listOf(
                            profile(
                                id = "self",
                                name = "حسابي",
                                isSelfProfile = true,
                                isActive = true,
                            ),
                            profile(
                                id = "child-1",
                                name = "أحمد",
                                isSelfProfile = false,
                                isShared = false,
                                isActive = false,
                            ),
                        ),
                    ),
                    onProfileSelected = { profileId -> activatedProfileId = profileId },
                    onProfileNotificationsToggled = {},
                    onAddProfileRequested = {},
                    onEditProfileRequested = {},
                    onOpenSharedProfile = {},
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = { _ -> },
                    onRequestEmailLink = {},
                    onSignOut = {},
                )
            }
        }

        composeRule.onNodeWithTag("profiles-activate-child-1").performClick()

        assertEquals("child-1", activatedProfileId)
    }

    @Test
    fun self_profile_activate_chip_activates_profile_directly() {
        var activatedProfileId: String? = null

        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
                        account = ProfilesAccountUiState(
                            isRuntimeConfigured = true,
                            status = AuthSessionStatus.SignedIn,
                            email = "owner@example.com",
                        ),
                        activeProfile = profile(
                            id = "child-1",
                            name = "أحمد",
                            isSelfProfile = false,
                            isActive = true,
                        ),
                        profiles = listOf(
                            profile(
                                id = "self",
                                name = "حسابي",
                                isSelfProfile = true,
                                isActive = false,
                            ),
                            profile(
                                id = "child-1",
                                name = "أحمد",
                                isSelfProfile = false,
                                isActive = true,
                            ),
                        ),
                    ),
                    onProfileSelected = { profileId -> activatedProfileId = profileId },
                    onProfileNotificationsToggled = {},
                    onAddProfileRequested = {},
                    onEditProfileRequested = {},
                    onOpenSharedProfile = {},
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = { _ -> },
                    onRequestEmailLink = {},
                    onSignOut = {},
                )
            }
        }

        composeRule.onNodeWithTag("profiles-self-activate").performClick()

        assertEquals("self", activatedProfileId)
    }

    @Test
    fun child_profile_details_sheet_opens_shared_profile_for_that_profile() {
        var openedProfileId: String? = null

        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
                        account = ProfilesAccountUiState(),
                        activeProfile = profile(
                            id = "self",
                            name = "حسابي",
                            isSelfProfile = true,
                            isActive = true,
                        ),
                        profiles = listOf(
                            profile(
                                id = "self",
                                name = "حسابي",
                                isSelfProfile = true,
                                isActive = true,
                            ),
                            profile(
                                id = "child-1",
                                name = "أحمد",
                                isSelfProfile = false,
                                isShared = false,
                                isActive = false,
                            ),
                        ),
                    ),
                    onProfileSelected = {},
                    onProfileNotificationsToggled = {},
                    onAddProfileRequested = {},
                    onEditProfileRequested = {},
                    onOpenSharedProfile = { profileId -> openedProfileId = profileId },
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = { _ -> },
                    onRequestEmailLink = {},
                    onSignOut = {},
                )
            }
        }

        composeRule.onNodeWithTag("profiles-manage-account-child-1").performClick()
        composeRule.onNodeWithTag("profiles-detail-share").performClick()

        assertEquals("child-1", openedProfileId)
    }

    @Test
    fun profile_cards_keep_only_shared_badge() {
        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
                        account = ProfilesAccountUiState(),
                        activeProfile = profile(
                            id = "self",
                            name = "حسابي",
                            isSelfProfile = true,
                            isActive = true,
                            syncMode = ProfileSyncMode.SoloSynced,
                        ),
                        profiles = listOf(
                            profile(
                                id = "self",
                                name = "حسابي",
                                isSelfProfile = true,
                                isActive = true,
                                syncMode = ProfileSyncMode.SoloSynced,
                            ),
                            profile(
                                id = "local-1",
                                name = "أحمد",
                                isSelfProfile = false,
                                isShared = false,
                                isActive = false,
                                syncMode = ProfileSyncMode.LocalOnly,
                            ),
                            profile(
                                id = "shared-1",
                                name = "خالد",
                                isSelfProfile = false,
                                isShared = true,
                                isActive = false,
                                syncMode = ProfileSyncMode.SoloSynced,
                            ),
                        ),
                    ),
                    onProfileSelected = {},
                    onProfileNotificationsToggled = {},
                    onAddProfileRequested = {},
                    onEditProfileRequested = {},
                    onOpenSharedProfile = {},
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = { _ -> },
                    onRequestEmailLink = {},
                    onSignOut = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.profile_sync_shared)).assertIsDisplayed()
        composeRule.onAllNodes(hasText(composeRule.activity.getString(R.string.profile_sync_synced))).assertCountEquals(0)
        composeRule.onAllNodes(hasText(composeRule.activity.getString(R.string.profile_sync_local))).assertCountEquals(0)
    }

    @Test
    fun account_card_shows_signed_out_state_and_sync_paused_message() {
        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
                        account = ProfilesAccountUiState(
                            isRuntimeConfigured = true,
                            status = AuthSessionStatus.SignedOut,
                        ),
                        profiles = listOf(
                            profile(
                                id = "self",
                                name = "حسابي",
                                isSelfProfile = true,
                                isActive = true,
                            ),
                        ),
                    ),
                    onProfileSelected = {},
                    onProfileNotificationsToggled = {},
                    onAddProfileRequested = {},
                    onEditProfileRequested = {},
                    onOpenSharedProfile = {},
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = { _ -> },
                    onRequestEmailLink = {},
                    onSignOut = {},
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_self_label),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_account_state_signed_out),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_account_sync_paused),
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("profiles-account-request-link").assertIsDisplayed()
    }

    @Test
    fun account_card_shows_signed_in_state_and_sync_active_message() {
        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
                        account = ProfilesAccountUiState(
                            isRuntimeConfigured = true,
                            status = AuthSessionStatus.SignedIn,
                            email = "owner@example.com",
                        ),
                        profiles = listOf(
                            profile(
                                id = "self",
                                name = "حسابي",
                                isSelfProfile = true,
                                isActive = true,
                            ),
                        ),
                    ),
                    onProfileSelected = {},
                    onProfileNotificationsToggled = {},
                    onAddProfileRequested = {},
                    onEditProfileRequested = {},
                    onOpenSharedProfile = {},
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = { _ -> },
                    onRequestEmailLink = {},
                    onSignOut = {},
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_self_label),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_account_state_signed_in),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.settings_account_email_value, "owner@example.com"),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_account_sync_active),
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("profiles-self-manage-account").assertIsDisplayed()
        composeRule.onNodeWithTag("profiles-account-sign-out").assertIsDisplayed()
    }

    private fun profile(
        id: String,
        name: String,
        isSelfProfile: Boolean,
        isActive: Boolean,
        isShared: Boolean = false,
        syncMode: ProfileSyncMode = ProfileSyncMode.LocalOnly,
    ) = ProfileCardUiState(
        id = id,
        name = name,
        isSelfProfile = isSelfProfile,
        isShared = isShared,
        isActive = isActive,
        notificationsEnabled = true,
        syncMode = syncMode,
        paceLabelRes = null,
        completionRate = 0,
        todayCompletedCount = 0,
        todayTotalCount = 0,
    )
}
