package com.quran.tathbeet.app

import com.quran.tathbeet.domain.model.LearnerAccount
import java.time.ZonedDateTime

internal object ReminderSchedulePlanner {
    fun eligibleProfileIds(
        globalNotificationsEnabled: Boolean,
        canPostNotifications: Boolean,
        accounts: List<LearnerAccount>,
        scheduledProfileIds: Set<String>,
    ): Set<String> {
        if (!globalNotificationsEnabled || !canPostNotifications) {
            return emptySet()
        }
        return accounts.asSequence()
            .filter { account -> account.notificationsEnabled && account.id in scheduledProfileIds }
            .map { account -> account.id }
            .toSet()
    }

    fun shouldHandleReminder(
        globalNotificationsEnabled: Boolean,
        canPostNotifications: Boolean,
        accountNotificationsEnabled: Boolean,
        hasSchedule: Boolean,
    ): Boolean =
        globalNotificationsEnabled &&
            canPostNotifications &&
            accountNotificationsEnabled &&
            hasSchedule

    fun nextTriggerAt(
        referenceTime: ZonedDateTime,
        hour: Int,
        minute: Int,
    ): ZonedDateTime {
        val targetToday = referenceTime
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
        return if (targetToday.isAfter(referenceTime)) {
            targetToday
        } else {
            targetToday.plusDays(1)
        }
    }
}
