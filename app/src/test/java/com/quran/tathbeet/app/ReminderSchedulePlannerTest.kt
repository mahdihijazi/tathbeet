package com.quran.tathbeet.app

import com.quran.tathbeet.domain.model.LearnerAccount
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulePlannerTest {

    @Test
    fun eligible_profile_ids_include_all_enabled_profiles_with_saved_schedules() {
        val eligibleProfileIds = ReminderSchedulePlanner.eligibleProfileIds(
            globalNotificationsEnabled = true,
            canPostNotifications = true,
            accounts = listOf(
                learnerAccount(id = "self", notificationsEnabled = true, isSelfProfile = true),
                learnerAccount(id = "ahmad", notificationsEnabled = true),
                learnerAccount(id = "maryam", notificationsEnabled = false),
                learnerAccount(id = "guest", notificationsEnabled = true),
            ),
            scheduledProfileIds = setOf("self", "ahmad", "maryam"),
        )

        assertEquals(setOf("self", "ahmad"), eligibleProfileIds)
    }

    @Test
    fun eligible_profile_ids_are_empty_when_notifications_cannot_be_posted() {
        val eligibleProfileIds = ReminderSchedulePlanner.eligibleProfileIds(
            globalNotificationsEnabled = true,
            canPostNotifications = false,
            accounts = listOf(
                learnerAccount(id = "self", notificationsEnabled = true, isSelfProfile = true),
            ),
            scheduledProfileIds = setOf("self"),
        )

        assertTrue(eligibleProfileIds.isEmpty())
    }

    @Test
    fun should_handle_reminder_requires_notification_permission() {
        val shouldHandleReminder = ReminderSchedulePlanner.shouldHandleReminder(
            globalNotificationsEnabled = true,
            canPostNotifications = false,
            accountNotificationsEnabled = true,
            hasSchedule = true,
        )

        assertFalse(shouldHandleReminder)
    }

    @Test
    fun next_trigger_at_keeps_same_day_when_reminder_time_is_still_ahead() {
        val referenceTime = ZonedDateTime.of(2026, 3, 13, 7, 45, 0, 0, ZoneId.of("UTC"))

        val triggerAt = ReminderSchedulePlanner.nextTriggerAt(
            referenceTime = referenceTime,
            hour = 8,
            minute = 15,
        )

        assertEquals(ZonedDateTime.of(2026, 3, 13, 8, 15, 0, 0, ZoneId.of("UTC")), triggerAt)
    }

    @Test
    fun next_trigger_at_moves_to_next_day_after_the_scheduled_time_passes() {
        val referenceTime = ZonedDateTime.of(2026, 3, 13, 20, 30, 0, 0, ZoneId.of("UTC"))

        val triggerAt = ReminderSchedulePlanner.nextTriggerAt(
            referenceTime = referenceTime,
            hour = 19,
            minute = 0,
        )

        assertEquals(ZonedDateTime.of(2026, 3, 14, 19, 0, 0, 0, ZoneId.of("UTC")), triggerAt)
    }

    private fun learnerAccount(
        id: String,
        notificationsEnabled: Boolean,
        isSelfProfile: Boolean = false,
    ): LearnerAccount =
        LearnerAccount(
            id = id,
            name = id,
            isSelfProfile = isSelfProfile,
            isShared = false,
            notificationsEnabled = notificationsEnabled,
        )
}
