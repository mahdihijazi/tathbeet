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

    @Test
    fun mixed_pool_keeps_short_surahs_friendly_and_includes_new_selections() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val catalog = loadQuranCatalog(context)

        val reviewUnits = catalog.buildReviewUnits(
            context = context,
            keys = setOf(
                selectionKey(SelectionCategory.Juz, 30),
                selectionKey(SelectionCategory.Juz, 29),
                selectionKey(SelectionCategory.Rub, 1),
                selectionKey(SelectionCategory.Surahs, 61),
                selectionKey(SelectionCategory.Surahs, 62),
                selectionKey(SelectionCategory.Surahs, 63),
            ),
        )

        assertTrue(reviewUnits.any { unit -> unit.id == "rub-1" })
        assertTrue(reviewUnits.any { unit -> unit.id == "surah-61" })
        assertTrue(reviewUnits.any { unit -> unit.id == "surah-62" })
        assertTrue(reviewUnits.any { unit -> unit.id == "surah-63" })
        assertTrue(reviewUnits.any { unit -> unit.rubId in 225..232 })
        assertTrue(
            reviewUnits.any { unit ->
                unit.title == context.getString(R.string.quran_surah_title, "العاديات")
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
