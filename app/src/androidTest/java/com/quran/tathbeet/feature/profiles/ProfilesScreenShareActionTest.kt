package com.quran.tathbeet.feature.profiles

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.R
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
    fun child_profile_card_shows_manage_sharing_action() {
        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
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
                    onEditActiveProfileRequested = {},
                    onOpenSharedProfile = {},
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = {},
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_button_manage_sharing),
        ).assertIsDisplayed()
    }

    @Test
    fun child_profile_card_opens_shared_profile_for_that_profile() {
        var openedProfileId: String? = null

        composeRule.setContent {
            TathbeetTheme {
                ProfilesScreen(
                    uiState = ProfilesUiState(
                        isLoading = false,
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
                    onEditActiveProfileRequested = {},
                    onOpenSharedProfile = { profileId -> openedProfileId = profileId },
                    onProfileNameChanged = {},
                    onSaveProfile = {},
                    onDismissProfileDialog = {},
                    onRequestDeleteProfile = {},
                    onDismissDeleteProfile = {},
                    onConfirmDeleteProfile = {},
                    onOpenSchedule = {},
                )
            }
        }

        composeRule.onNodeWithTag("profiles-open-shared-child-1").performClick()

        assertEquals("child-1", openedProfileId)
    }

    private fun profile(
        id: String,
        name: String,
        isSelfProfile: Boolean,
        isActive: Boolean,
        isShared: Boolean = false,
    ) = ProfileCardUiState(
        id = id,
        name = name,
        isSelfProfile = isSelfProfile,
        isShared = isShared,
        isActive = isActive,
        notificationsEnabled = true,
        paceLabelRes = null,
        completionRate = 0,
        todayCompletedCount = 0,
        todayTotalCount = 0,
    )
}
