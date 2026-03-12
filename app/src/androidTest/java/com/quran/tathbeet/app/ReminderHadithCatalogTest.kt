package com.quran.tathbeet.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderHadithCatalogTest {

    @Test
    fun reminder_hadith_catalog_keeps_short_notifications_separate_from_card_content() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val notificationSources = ReminderHadithCatalog.notificationEntries.map { entry ->
            context.getString(entry.sourceResId)
        }
        val cardSources = ReminderHadithCatalog.cardEntries.map { entry ->
            context.getString(entry.sourceResId)
        }

        assertEquals(
            listOf(
                "صحيح البخاري 5031",
                "صحيح البخاري 5033",
                "صحيح مسلم 798a",
                "صحيح مسلم 817a",
            ),
            notificationSources,
        )
        assertEquals(
            listOf(
                "صحيح مسلم 790a",
                "صحيح مسلم 804a",
                "صحيح البخاري 5427",
            ),
            cardSources,
        )
        assertTrue(
            ReminderHadithCatalog.notificationEntries.all { entry ->
                context.getString(entry.textResId).isNotBlank()
            },
        )
        assertTrue(
            ReminderHadithCatalog.cardEntries.all { entry ->
                context.getString(entry.textResId).isNotBlank()
            },
        )
    }
}
