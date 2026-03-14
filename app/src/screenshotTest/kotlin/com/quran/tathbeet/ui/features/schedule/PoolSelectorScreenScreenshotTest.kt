package com.quran.tathbeet.ui.features.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.SelectionCategory
import com.quran.tathbeet.ui.model.selectionKey
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

private const val SelectorPreviewWidth = 411
private const val SelectorPreviewHeight = 760

@PreviewTest
@Preview(
    name = "pool_selector_surahs_tab",
    locale = "ar",
    widthDp = SelectorPreviewWidth,
    heightDp = SelectorPreviewHeight,
    showBackground = true,
)
@Composable
fun PoolSelectorSurahsTabScreenshot() =
    PoolSelectorScreenshotFrame(
        selectedCategory = SelectionCategory.Surahs,
        darkTheme = false,
    )

@PreviewTest
@Preview(
    name = "pool_selector_surahs_tab_dark",
    locale = "ar",
    widthDp = SelectorPreviewWidth,
    heightDp = SelectorPreviewHeight,
    showBackground = true,
)
@Composable
fun PoolSelectorSurahsTabDarkScreenshot() =
    PoolSelectorScreenshotFrame(
        selectedCategory = SelectionCategory.Surahs,
        darkTheme = true,
    )

@PreviewTest
@Preview(
    name = "pool_selector_juz_tab",
    locale = "ar",
    widthDp = SelectorPreviewWidth,
    heightDp = SelectorPreviewHeight,
    showBackground = true,
)
@Composable
fun PoolSelectorJuzTabScreenshot() =
    PoolSelectorScreenshotFrame(
        selectedCategory = SelectionCategory.Juz,
        darkTheme = false,
    )

@PreviewTest
@Preview(
    name = "pool_selector_juz_tab_dark",
    locale = "ar",
    widthDp = SelectorPreviewWidth,
    heightDp = SelectorPreviewHeight,
    showBackground = true,
)
@Composable
fun PoolSelectorJuzTabDarkScreenshot() =
    PoolSelectorScreenshotFrame(
        selectedCategory = SelectionCategory.Juz,
        darkTheme = true,
    )

@PreviewTest
@Preview(
    name = "pool_selector_hizb_tab",
    locale = "ar",
    widthDp = SelectorPreviewWidth,
    heightDp = SelectorPreviewHeight,
    showBackground = true,
)
@Composable
fun PoolSelectorHizbTabScreenshot() =
    PoolSelectorScreenshotFrame(
        selectedCategory = SelectionCategory.Hizb,
        darkTheme = false,
    )

@PreviewTest
@Preview(
    name = "pool_selector_hizb_tab_dark",
    locale = "ar",
    widthDp = SelectorPreviewWidth,
    heightDp = SelectorPreviewHeight,
    showBackground = true,
)
@Composable
fun PoolSelectorHizbTabDarkScreenshot() =
    PoolSelectorScreenshotFrame(
        selectedCategory = SelectionCategory.Hizb,
        darkTheme = true,
    )

@PreviewTest
@Preview(
    name = "pool_selector_rub_tab",
    locale = "ar",
    widthDp = SelectorPreviewWidth,
    heightDp = SelectorPreviewHeight,
    showBackground = true,
)
@Composable
fun PoolSelectorRubTabScreenshot() =
    PoolSelectorScreenshotFrame(
        selectedCategory = SelectionCategory.Rub,
        darkTheme = false,
    )

@PreviewTest
@Preview(
    name = "pool_selector_rub_tab_dark",
    locale = "ar",
    widthDp = SelectorPreviewWidth,
    heightDp = SelectorPreviewHeight,
    showBackground = true,
)
@Composable
fun PoolSelectorRubTabDarkScreenshot() =
    PoolSelectorScreenshotFrame(
        selectedCategory = SelectionCategory.Rub,
        darkTheme = true,
    )

@Composable
private fun PoolSelectorScreenshotFrame(
    selectedCategory: SelectionCategory,
    darkTheme: Boolean,
) {
    ThemedScreenshotFrame(darkTheme = darkTheme, padded = false) {
        val optionsByCategory = sampleOptionsByCategory()
        PoolSelectorTabsPanel(
            selectedCategory = selectedCategory,
            optionsForCategory = { category -> optionsByCategory.getValue(category) },
            selectedPool = emptyList(),
            onCategorySelected = {},
            onToggleSelection = {},
        )
    }
}

@Composable
private fun sampleOptionsByCategory(): Map<SelectionCategory, List<QuranSelectionItem>> = mapOf(
    SelectionCategory.Surahs to listOf(
        sampleSurahItem(
            itemId = 1,
            name = stringResource(R.string.sample_surah_fatiha_title),
            ayahCount = 7,
            segmentCount = 1,
            firstRubId = 1,
            lastRubId = 1,
        ),
        sampleSurahItem(
            itemId = 2,
            name = stringResource(R.string.option_surah_baqarah),
            ayahCount = 286,
            segmentCount = 20,
            firstRubId = 2,
            lastRubId = 21,
        ),
        sampleSurahItem(
            itemId = 3,
            name = stringResource(R.string.sample_surah_al_imran_title),
            ayahCount = 200,
            segmentCount = 11,
            firstRubId = 22,
            lastRubId = 32,
        ),
        sampleSurahItem(
            itemId = 4,
            name = stringResource(R.string.sample_surah_an_nisa_title),
            ayahCount = 176,
            segmentCount = 12,
            firstRubId = 33,
            lastRubId = 44,
        ),
    ),
    SelectionCategory.Juz to listOf(
        sampleJuzItem(1, stringResource(R.string.sample_range_juz_1)),
        sampleJuzItem(2, stringResource(R.string.sample_range_juz_2)),
        sampleJuzItem(3, stringResource(R.string.sample_range_juz_3)),
        sampleJuzItem(4, stringResource(R.string.sample_range_juz_4)),
        sampleJuzItem(5, stringResource(R.string.sample_range_juz_5)),
    ),
    SelectionCategory.Hizb to listOf(
        sampleHizbItem(1, 1, stringResource(R.string.sample_range_hizb_1), 1, 4),
        sampleHizbItem(2, 1, stringResource(R.string.sample_range_hizb_2), 5, 8),
        sampleHizbItem(3, 2, stringResource(R.string.sample_range_hizb_3), 9, 12),
        sampleHizbItem(4, 2, stringResource(R.string.sample_range_hizb_4), 13, 16),
        sampleHizbItem(41, 21, stringResource(R.string.sample_range_hizb_41), 161, 164),
    ),
    SelectionCategory.Rub to listOf(
        sampleRubItem(1, 1, 1, stringResource(R.string.sample_range_rub_1)),
        sampleRubItem(2, 1, 1, stringResource(R.string.sample_range_rub_2)),
        sampleRubItem(3, 1, 1, stringResource(R.string.sample_range_rub_3)),
        sampleRubItem(4, 1, 1, stringResource(R.string.sample_range_rub_4)),
        sampleRubItem(12, 2, 3, stringResource(R.string.sample_range_rub_12)),
    ),
)

@Composable
private fun sampleSurahItem(
    itemId: Int,
    name: String,
    ayahCount: Int,
    segmentCount: Int,
    firstRubId: Int,
    lastRubId: Int,
): QuranSelectionItem = QuranSelectionItem(
    key = selectionKey(SelectionCategory.Surahs, itemId),
    category = SelectionCategory.Surahs,
    itemId = itemId,
    order = itemId,
    title = name,
    subtitle = stringResource(R.string.quran_surah_detail, ayahCount, segmentCount),
    segments = segmentCount,
    firstRubId = firstRubId,
    lastRubId = lastRubId,
)

@Composable
private fun sampleJuzItem(
    itemId: Int,
    rangeSummary: String = "من النبإ إلى الناس",
): QuranSelectionItem = QuranSelectionItem(
    key = selectionKey(SelectionCategory.Juz, itemId),
    category = SelectionCategory.Juz,
    itemId = itemId,
    order = itemId,
    title = stringResource(R.string.quran_juz_title, itemId),
    subtitle = stringResource(R.string.quran_juz_detail, rangeSummary),
    segments = 8,
    firstRubId = itemId * 8 - 7,
    lastRubId = itemId * 8,
)

@Composable
private fun sampleHizbItem(
    itemId: Int,
    juzId: Int,
    rangeSummary: String,
    firstRubId: Int,
    lastRubId: Int,
): QuranSelectionItem = QuranSelectionItem(
    key = selectionKey(SelectionCategory.Hizb, itemId),
    category = SelectionCategory.Hizb,
    itemId = itemId,
    order = itemId,
    title = stringResource(R.string.quran_hizb_title, itemId),
    subtitle = stringResource(R.string.quran_hizb_detail, juzId, rangeSummary),
    segments = lastRubId - firstRubId + 1,
    firstRubId = firstRubId,
    lastRubId = lastRubId,
)

@Composable
private fun sampleRubItem(
    itemId: Int,
    juzId: Int,
    hizbId: Int,
    rangeSummary: String,
): QuranSelectionItem = QuranSelectionItem(
    key = selectionKey(SelectionCategory.Rub, itemId),
    category = SelectionCategory.Rub,
    itemId = itemId,
    order = itemId,
    title = stringResource(R.string.quran_rub_title, itemId),
    subtitle = stringResource(R.string.quran_rub_detail, juzId, hizbId, rangeSummary),
    segments = 1,
    firstRubId = itemId,
    lastRubId = itemId,
)
