package com.quran.tathbeet.ui.model

import android.content.Context
import com.quran.tathbeet.R
import com.quran.tathbeet.core.text.formatAyahCount
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max

data class QuranSelectionItem(
    val key: String,
    val category: SelectionCategory,
    val itemId: Int,
    val order: Int,
    val title: String,
    val subtitle: String,
    val segments: Int,
    val firstRubId: Int,
    val lastRubId: Int,
)

class QuranCatalog internal constructor(
    internal val itemsByCategory: Map<SelectionCategory, List<QuranSelectionItem>>,
    internal val rubsById: Map<Int, RubItem>,
    internal val surahAyahCounts: Map<Int, Int>,
) {
    private val itemsByKey: Map<String, QuranSelectionItem> = itemsByCategory
        .values
        .flatten()
        .associateBy { it.key }

    fun itemsFor(category: SelectionCategory): List<QuranSelectionItem> =
        itemsByCategory[category].orEmpty()

    fun resolveSelections(keys: Set<String>): List<QuranSelectionItem> =
        keys.mapNotNull(itemsByKey::get).sortedWith(selectionComparator)

    fun requireSelection(category: SelectionCategory, itemId: Int): QuranSelectionItem =
        itemsByKey[selectionKey(category, itemId)]
            ?: error("Missing Quran selection for ${category.name}:$itemId")

}

internal data class ReviewUnitTemplate(
    val id: String,
    val rubId: Int,
    val title: String,
    val detail: String,
    val weight: Double,
    val isPartial: Boolean,
    val start: Boundary,
    val end: Boundary,
)

private val selectionComparator =
    compareBy<QuranSelectionItem>({ it.category.ordinal }, { it.order })

fun selectionKey(category: SelectionCategory, itemId: Int): String =
    "${category.name.lowercase()}-$itemId"

fun loadQuranCatalog(context: Context): QuranCatalog {
    val rubs = parseRubItems(context)
    return QuranCatalog(
        itemsByCategory = mapOf(
            SelectionCategory.Surahs to parseSurahItems(context, rubs),
            SelectionCategory.Juz to parseJuzItems(context),
            SelectionCategory.Hizb to parseHizbItems(context),
            SelectionCategory.Rub to rubs.map { it.toSelectionItem(context) },
        ),
        rubsById = rubs.associateBy { it.id },
        surahAyahCounts = context.readJsonArray(QURAN_SURAHS_ASSET).mapObjects { json ->
            json.getInt("id") to json.getInt("ayahCount")
        }.toMap(),
    )
}

fun summarizeSelectionTitles(
    context: Context,
    items: List<QuranSelectionItem>,
    emptyResId: Int,
): String {
    if (items.isEmpty()) {
        return context.getString(emptyResId)
    }

    val preview = items
        .sortedWith(selectionComparator)
        .take(SELECTION_PREVIEW_LIMIT)
        .joinToString(separator = context.getString(R.string.selection_summary_separator)) { item ->
            item.title
        }
    val remaining = items.size - SELECTION_PREVIEW_LIMIT

    return if (remaining > 0) {
        preview + context.getString(R.string.selection_summary_more, remaining)
    } else {
        preview
    }
}

fun fullSelectionTitles(
    context: Context,
    items: List<QuranSelectionItem>,
    emptyResId: Int,
): String {
    if (items.isEmpty()) {
        return context.getString(emptyResId)
    }

    return items
        .sortedWith(selectionComparator)
        .joinToString(separator = context.getString(R.string.selection_summary_separator)) { item ->
            item.title
        }
}

private fun parseSurahItems(
    context: Context,
    rubs: List<RubItem>,
): List<QuranSelectionItem> =
    context.readJsonArray(QURAN_SURAHS_ASSET).mapObjects { json ->
        val surahId = json.getInt("id")
        val surahName = json.getString("nameArabic")
        val ayahCount = json.getInt("ayahCount")
        val segmentCount = max(
            1,
            rubs.count { rub ->
                rub.start.surahId <= surahId && rub.end.surahId >= surahId
            },
        )

        QuranSelectionItem(
            key = selectionKey(SelectionCategory.Surahs, surahId),
            category = SelectionCategory.Surahs,
            itemId = surahId,
            order = surahId,
            title = context.getString(R.string.quran_surah_title, surahName),
            subtitle = context.getString(
                R.string.quran_surah_detail,
                formatAyahCount(context, ayahCount),
                segmentCount,
            ),
            segments = segmentCount,
            firstRubId = rubs.first { rub ->
                rub.start.surahId <= surahId && rub.end.surahId >= surahId
            }.id,
            lastRubId = rubs.last { rub ->
                rub.start.surahId <= surahId && rub.end.surahId >= surahId
            }.id,
        )
    }

private fun parseJuzItems(context: Context): List<QuranSelectionItem> =
    context.readJsonArray(QURAN_JUZ_ASSET).mapObjects { json ->
        val itemId = json.getInt("id")
        val start = json.getJSONObject("start").toBoundary()
        val end = json.getJSONObject("end").toBoundary()
        val firstQuarterId = json.getInt("firstQuarterId")
        val lastQuarterId = json.getInt("lastQuarterId")

        QuranSelectionItem(
            key = selectionKey(SelectionCategory.Juz, itemId),
            category = SelectionCategory.Juz,
            itemId = itemId,
            order = itemId,
            title = context.getString(R.string.quran_juz_title, itemId),
            subtitle = context.getString(
                R.string.quran_juz_detail,
                buildRangeSummary(context, start, end),
            ),
            segments = lastQuarterId - firstQuarterId + 1,
            firstRubId = firstQuarterId,
            lastRubId = lastQuarterId,
        )
    }

private fun parseHizbItems(context: Context): List<QuranSelectionItem> =
    context.readJsonArray(QURAN_HIZB_ASSET).mapObjects { json ->
        val itemId = json.getInt("id")
        val start = json.getJSONObject("start").toBoundary()
        val end = json.getJSONObject("end").toBoundary()
        val juzId = json.getInt("juzId")
        val firstQuarterId = json.getInt("firstQuarterId")
        val lastQuarterId = json.getInt("lastQuarterId")

        QuranSelectionItem(
            key = selectionKey(SelectionCategory.Hizb, itemId),
            category = SelectionCategory.Hizb,
            itemId = itemId,
            order = itemId,
            title = context.getString(R.string.quran_hizb_title, itemId),
            subtitle = context.getString(
                R.string.quran_hizb_detail,
                juzId,
                buildRangeSummary(context, start, end),
            ),
            segments = lastQuarterId - firstQuarterId + 1,
            firstRubId = firstQuarterId,
            lastRubId = lastQuarterId,
        )
    }

private fun parseRubItems(context: Context): List<RubItem> =
    context.readJsonArray(QURAN_RUB_ASSET).mapObjects { json ->
        val id = json.getInt("id")
        RubItem(
            id = id,
            juzId = json.getInt("juzId"),
            hizbId = json.getInt("hizbId"),
            start = json.getJSONObject("start").toBoundary(),
            end = json.getJSONObject("end").toBoundary(),
            title = context.getString(R.string.quran_rub_title, id),
        )
    }

private fun RubItem.toSelectionItem(context: Context): QuranSelectionItem =
    QuranSelectionItem(
        key = selectionKey(SelectionCategory.Rub, id),
        category = SelectionCategory.Rub,
        itemId = id,
        order = id,
        title = context.getString(R.string.quran_rub_title, id),
        subtitle = context.getString(
            R.string.quran_rub_detail,
            juzId,
            hizbId,
            buildRangeSummary(context, start, end),
        ),
        segments = 1,
        firstRubId = id,
        lastRubId = id,
    )

internal fun buildReviewUnitTitle(
    context: Context,
    segment: CoverageSegment,
): String =
    if (segment.start.surahId == segment.end.surahId) {
        buildRangeSummary(context, segment.start, segment.end)
    } else {
        context.getString(
            R.string.review_unit_title_multi_surah,
            segment.start.surahNameArabic,
            segment.end.surahNameArabic,
        )
    }

internal fun buildRangeSummary(
    context: Context,
    start: Boundary,
    end: Boundary,
): String =
    if (start.surahId == end.surahId) {
        context.getString(
            R.string.quran_range_single_surah,
            start.surahNameArabic,
            start.ayah,
            end.ayah,
        )
    } else {
        context.getString(
            R.string.quran_range_multi_surah,
            start.surahNameArabic,
            start.ayah,
            end.surahNameArabic,
            end.ayah,
        )
    }

private fun Context.readJsonArray(assetPath: String): JSONArray =
    JSONArray(
        assets.open(assetPath).bufferedReader().use { reader ->
            reader.readText()
        },
    )

private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    buildList(length()) {
        for (index in 0 until length()) {
            add(transform(getJSONObject(index)))
        }
    }

private fun JSONObject.toBoundary(): Boundary =
    Boundary(
        surahId = getInt("surahId"),
        surahNameArabic = getString("surahNameArabic"),
        ayah = getInt("ayah"),
    )

internal data class Boundary(
    val surahId: Int,
    val surahNameArabic: String,
    val ayah: Int,
)

internal data class RubItem(
    val id: Int,
    val juzId: Int,
    val hizbId: Int,
    val start: Boundary,
    val end: Boundary,
    val title: String,
)

internal data class CoverageSegment(
    val rubId: Int,
    val start: Boundary,
    val end: Boundary,
) {
    fun mergeWith(other: CoverageSegment): CoverageSegment =
        copy(
            start = if (start.isBeforeOrEqual(other.start)) start else other.start,
            end = if (end.isAfterOrEqual(other.end)) end else other.end,
        )

    fun isWholeRub(rub: RubItem): Boolean =
        start == rub.start && end == rub.end
}

private fun Boundary.isBeforeOrEqual(other: Boundary): Boolean =
    surahId < other.surahId || (surahId == other.surahId && ayah <= other.ayah)

private fun Boundary.isAfterOrEqual(other: Boundary): Boolean =
    surahId > other.surahId || (surahId == other.surahId && ayah >= other.ayah)

private const val QURAN_SURAHS_ASSET = "quran/surahs.json"
private const val QURAN_JUZ_ASSET = "quran/juzs.json"
private const val QURAN_HIZB_ASSET = "quran/hizbs.json"
private const val QURAN_RUB_ASSET = "quran/rub-al-hizb.json"
private const val SELECTION_PREVIEW_LIMIT = 3
