package com.quran.tathbeet.app

import androidx.annotation.StringRes
import com.quran.tathbeet.R

data class ReminderHadithEntry(
    @param:StringRes val textResId: Int,
    @param:StringRes val sourceResId: Int,
)

object ReminderHadithCatalog {
    val notificationEntries: List<ReminderHadithEntry> = listOf(
        ReminderHadithEntry(
            textResId = R.string.reminder_hadith_notification_1_text,
            sourceResId = R.string.reminder_hadith_notification_1_source,
        ),
        ReminderHadithEntry(
            textResId = R.string.reminder_hadith_notification_2_text,
            sourceResId = R.string.reminder_hadith_notification_2_source,
        ),
        ReminderHadithEntry(
            textResId = R.string.reminder_hadith_notification_3_text,
            sourceResId = R.string.reminder_hadith_notification_3_source,
        ),
        ReminderHadithEntry(
            textResId = R.string.reminder_hadith_notification_4_text,
            sourceResId = R.string.reminder_hadith_notification_4_source,
        ),
    )

    val cardEntries: List<ReminderHadithEntry> = listOf(
        ReminderHadithEntry(
            textResId = R.string.reminder_hadith_card_1_text,
            sourceResId = R.string.reminder_hadith_card_1_source,
        ),
        ReminderHadithEntry(
            textResId = R.string.reminder_hadith_card_2_text,
            sourceResId = R.string.reminder_hadith_card_2_source,
        ),
        ReminderHadithEntry(
            textResId = R.string.reminder_hadith_card_3_text,
            sourceResId = R.string.reminder_hadith_card_3_source,
        ),
    )

    fun notificationEntryFor(dayOfYear: Int): ReminderHadithEntry {
        val index = Math.floorMod(dayOfYear - 1, notificationEntries.size)
        return notificationEntries[index]
    }
}
