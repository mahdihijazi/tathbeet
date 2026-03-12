package com.quran.tathbeet.ui.model

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quran.tathbeet.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranReviewUnitsTest {

    @Test
    fun juz_thirty_keeps_late_short_surahs_as_separate_review_units() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val catalog = loadQuranCatalog(context)

        val reviewUnits = catalog.buildReviewUnits(
            context = context,
            keys = setOf(selectionKey(SelectionCategory.Juz, 30)),
        )

        assertTrue(
            reviewUnits.any { unit ->
                unit.title == context.getString(R.string.quran_surah_title, "العاديات")
            },
        )
        assertTrue(
            reviewUnits.any { unit ->
                unit.title == context.getString(R.string.quran_surah_title, "القارعة")
            },
        )
        assertTrue(
            reviewUnits.any { unit ->
                unit.title == context.getString(R.string.quran_surah_title, "الناس")
            },
        )
        assertFalse(
            reviewUnits.any { unit ->
                unit.title == context.getString(
                    R.string.review_unit_title_multi_surah,
                    "العاديات",
                    "الناس",
                )
            },
        )
        assertFalse(
            reviewUnits.any { unit ->
                unit.title == context.getString(
                    R.string.quran_range_single_surah,
                    "العاديات",
                    1,
                    8,
                )
            },
        )
        assertFalse(
            reviewUnits.any { unit ->
                unit.title == context.getString(
                    R.string.quran_range_single_surah,
                    "العاديات",
                    9,
                    11,
                )
            },
        )
    }
}
