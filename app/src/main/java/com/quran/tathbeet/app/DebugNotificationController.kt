package com.quran.tathbeet.app

import androidx.annotation.StringRes
import com.quran.tathbeet.R

interface DebugNotificationController {
    val reminderScenarios: List<ReminderNotificationDebugScenario>

    suspend fun triggerReminderNotification(scenario: ReminderNotificationDebugScenario)
}

enum class ReminderNotificationDebugScenario(
    val id: String,
    @param:StringRes val labelResId: Int,
    val includeProfileName: Boolean,
    val hasRollover: Boolean,
    val includesMotivation: Boolean,
    val motivationalEntryIndex: Int = 0,
) {
    TodayGeneral(
        id = "today-general",
        labelResId = R.string.debug_notification_today_general,
        includeProfileName = false,
        hasRollover = false,
        includesMotivation = false,
    ),
    TodayGeneralMotivation(
        id = "today-general-motivation",
        labelResId = R.string.debug_notification_today_general_motivation,
        includeProfileName = false,
        hasRollover = false,
        includesMotivation = true,
        motivationalEntryIndex = 0,
    ),
    RolloverGeneral(
        id = "rollover-general",
        labelResId = R.string.debug_notification_rollover_general,
        includeProfileName = false,
        hasRollover = true,
        includesMotivation = false,
    ),
    RolloverGeneralMotivation(
        id = "rollover-general-motivation",
        labelResId = R.string.debug_notification_rollover_general_motivation,
        includeProfileName = false,
        hasRollover = true,
        includesMotivation = true,
        motivationalEntryIndex = 1,
    ),
    TodayNamedProfile(
        id = "today-named-profile",
        labelResId = R.string.debug_notification_today_named_profile,
        includeProfileName = true,
        hasRollover = false,
        includesMotivation = false,
    ),
    TodayNamedProfileMotivation(
        id = "today-named-profile-motivation",
        labelResId = R.string.debug_notification_today_named_profile_motivation,
        includeProfileName = true,
        hasRollover = false,
        includesMotivation = true,
        motivationalEntryIndex = 2,
    ),
    RolloverNamedProfile(
        id = "rollover-named-profile",
        labelResId = R.string.debug_notification_rollover_named_profile,
        includeProfileName = true,
        hasRollover = true,
        includesMotivation = false,
    ),
    RolloverNamedProfileMotivation(
        id = "rollover-named-profile-motivation",
        labelResId = R.string.debug_notification_rollover_named_profile_motivation,
        includeProfileName = true,
        hasRollover = true,
        includesMotivation = true,
        motivationalEntryIndex = 3,
    ),
}

object NoOpDebugNotificationController : DebugNotificationController {
    override val reminderScenarios: List<ReminderNotificationDebugScenario> = emptyList()

    override suspend fun triggerReminderNotification(scenario: ReminderNotificationDebugScenario) = Unit
}
