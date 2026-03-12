package com.quran.tathbeet.ui.model

import android.content.Context
import com.quran.tathbeet.R
import com.quran.tathbeet.core.text.formatAyahCount

internal fun QuranCatalog.buildReviewUnits(
    context: Context,
    keys: Set<String>,
): List<ReviewUnitTemplate> {
    val selectedItems = resolveSelections(keys)
    val explicitRubIds = selectedItems
        .filter { item -> item.category == SelectionCategory.Rub }
        .map { item -> item.itemId }
        .toSet()
    val coverageByRubId = linkedMapOf<Int, CoverageSegment>()

    selectedItems.forEach { item ->
        buildCoverageSegments(item).forEach { segment ->
            coverageByRubId[segment.rubId] = coverageByRubId[segment.rubId]
                ?.mergeWith(segment)
                ?: segment
        }
    }

    val rawUnits = coverageByRubId.values
        .sortedBy { segment -> segment.rubId }
        .flatMap { segment ->
            if (explicitRubIds.contains(segment.rubId)) {
                listOf(buildRubReviewUnit(context, segment))
            } else {
                buildFriendlyReviewUnits(context, segment)
            }
        }

    return mergeAdjacentShortSurahUnits(
        context = context,
        units = rawUnits,
    )
}

private fun QuranCatalog.buildFriendlyReviewUnits(
    context: Context,
    segment: CoverageSegment,
): List<ReviewUnitTemplate> {
    val surahSlices = segment.toSurahSlices(this)
    if (surahSlices.isEmpty()) {
        return listOf(buildRubReviewUnit(context, segment))
    }

    val mixedUnits = mutableListOf<ReviewUnitTemplate>()
    var pendingPartialStart: Boundary? = null
    var pendingPartialEnd: Boundary? = null

    fun flushPendingPartial() {
        val start = pendingPartialStart ?: return
        val end = pendingPartialEnd ?: return
        mixedUnits += buildRubReviewUnit(
            context = context,
            segment = CoverageSegment(
                rubId = segment.rubId,
                start = start,
                end = end,
            ),
        )
        pendingPartialStart = null
        pendingPartialEnd = null
    }

    surahSlices.forEach { slice ->
        val shouldShowAsSurah = slice.isWholeSurah(this) && isShortSurah(slice.surahId)
        if (shouldShowAsSurah) {
            flushPendingPartial()
            mixedUnits += slice.toSurahReviewUnit(
                context = context,
                catalog = this,
                rubId = segment.rubId,
            )
        } else {
            val sliceStart = slice.toStartBoundary(catalog = this)
            val sliceEnd = slice.toEndBoundary(catalog = this)
            if (pendingPartialStart == null) {
                pendingPartialStart = sliceStart
            }
            pendingPartialEnd = sliceEnd
        }
    }

    flushPendingPartial()

    return if (mixedUnits.isEmpty()) {
        listOf(buildRubReviewUnit(context, segment))
    } else {
        mixedUnits
    }
}

private fun QuranCatalog.buildRubReviewUnit(
    context: Context,
    segment: CoverageSegment,
): ReviewUnitTemplate {
    val rub = rubsById.getValue(segment.rubId)
    val isWholeRub = segment.isWholeRub(rub)
    return ReviewUnitTemplate(
        id = if (isWholeRub) {
            "rub-${segment.rubId}"
        } else {
            "segment-${segment.rubId}-${segment.start.surahId}-${segment.start.ayah}-${segment.end.surahId}-${segment.end.ayah}"
        },
        rubId = segment.rubId,
        title = buildReviewUnitTitle(context, segment),
        detail = context.getString(
            R.string.review_unit_detail,
            rub.title,
            buildRangeSummary(context, segment.start, segment.end),
        ),
        weight = segment.weight(this),
        isPartial = !isWholeRub,
        start = segment.start,
        end = segment.end,
    )
}

private fun SurahSlice.toSurahReviewUnit(
    context: Context,
    catalog: QuranCatalog,
    rubId: Int,
): ReviewUnitTemplate =
    ReviewUnitTemplate(
        id = "surah-$surahId",
        rubId = rubId,
        title = catalog.requireSelection(SelectionCategory.Surahs, surahId).title,
        detail = formatAyahCount(context, ayahCount()),
        weight = ayahCount().toDouble() / catalog.rubsById.getValue(rubId).ayahCount(catalog).toDouble(),
        isPartial = true,
        start = toStartBoundary(catalog),
        end = toEndBoundary(catalog),
    )

private fun QuranCatalog.mergeAdjacentShortSurahUnits(
    context: Context,
    units: List<ReviewUnitTemplate>,
): List<ReviewUnitTemplate> {
    if (units.isEmpty()) return units

    val mergedUnits = mutableListOf<ReviewUnitTemplate>()
    var index = 0
    while (index < units.size) {
        val current = units[index]
        val next = units.getOrNull(index + 1)
        if (next != null && shouldMergeIntoShortSurah(current, next)) {
            mergedUnits += buildMergedShortSurahUnit(
                context = context,
                first = current,
                second = next,
            )
            index += 2
        } else {
            mergedUnits += current
            index += 1
        }
    }
    return mergedUnits
}

private fun QuranCatalog.shouldMergeIntoShortSurah(
    current: ReviewUnitTemplate,
    next: ReviewUnitTemplate,
): Boolean {
    if (!current.id.startsWith("segment-") || !next.id.startsWith("segment-")) {
        return false
    }
    val surahId = current.start.surahId
    if (
        current.start.surahId != current.end.surahId ||
        next.start.surahId != next.end.surahId ||
        surahId != next.start.surahId
    ) {
        return false
    }
    if (!isShortSurah(surahId)) {
        return false
    }
    val lastAyah = surahAyahCounts.getValue(surahId)
    return current.start.ayah == 1 &&
        current.end.ayah + 1 == next.start.ayah &&
        next.end.ayah == lastAyah
}

private fun QuranCatalog.buildMergedShortSurahUnit(
    context: Context,
    first: ReviewUnitTemplate,
    second: ReviewUnitTemplate,
): ReviewUnitTemplate {
    val surahId = first.start.surahId
    val title = requireSelection(SelectionCategory.Surahs, surahId).title
    return ReviewUnitTemplate(
        id = "surah-$surahId",
        rubId = first.rubId,
        title = title,
        detail = formatAyahCount(context, surahAyahCounts.getValue(surahId)),
        weight = first.weight + second.weight,
        isPartial = true,
        start = first.start,
        end = second.end,
    )
}

private fun QuranCatalog.buildCoverageSegments(item: QuranSelectionItem): List<CoverageSegment> =
    (item.firstRubId..item.lastRubId).map { rubId ->
        val rub = rubsById.getValue(rubId)
        val clippedStart = when (item.category) {
            SelectionCategory.Surahs -> {
                if (rub.start.surahId < item.itemId) {
                    Boundary(
                        surahId = item.itemId,
                        surahNameArabic = rub.end.surahNameArabic.takeIf { boundary -> rub.end.surahId == item.itemId }
                            ?: rub.start.surahNameArabic,
                        ayah = 1,
                    )
                } else {
                    rub.start
                }
            }
            else -> rub.start
        }
        val clippedEnd = when (item.category) {
            SelectionCategory.Surahs -> {
                if (rub.end.surahId > item.itemId) {
                    Boundary(
                        surahId = item.itemId,
                        surahNameArabic = rub.start.surahNameArabic.takeIf { boundary -> rub.start.surahId == item.itemId }
                            ?: rub.end.surahNameArabic,
                        ayah = surahAyahCounts.getValue(item.itemId),
                    )
                } else {
                    rub.end
                }
            }
            else -> rub.end
        }
        CoverageSegment(
            rubId = rubId,
            start = clippedStart,
            end = clippedEnd,
        )
    }

private fun QuranCatalog.isShortSurah(surahId: Int): Boolean =
    rubsById.values
        .asSequence()
        .filter { rub -> surahId in rub.start.surahId..rub.end.surahId }
        .map { rub -> rub.ayahCount(this) }
        .any { rubAyahCount -> surahAyahCounts.getValue(surahId) <= rubAyahCount }

private fun CoverageSegment.weight(catalog: QuranCatalog): Double =
    ayahCount(catalog).toDouble() / catalog.rubsById.getValue(rubId).ayahCount(catalog).toDouble()

private fun CoverageSegment.ayahCount(catalog: QuranCatalog): Int =
    countAyahs(start = start, end = end, surahAyahCounts = catalog.surahAyahCounts)

private fun RubItem.ayahCount(catalog: QuranCatalog): Int =
    countAyahs(start = start, end = end, surahAyahCounts = catalog.surahAyahCounts)

private fun countAyahs(
    start: Boundary,
    end: Boundary,
    surahAyahCounts: Map<Int, Int>,
): Int {
    if (start.surahId == end.surahId) {
        return end.ayah - start.ayah + 1
    }

    var total = surahAyahCounts.getValue(start.surahId) - start.ayah + 1
    for (surahId in (start.surahId + 1) until end.surahId) {
        total += surahAyahCounts.getValue(surahId)
    }
    total += end.ayah
    return total
}

private fun CoverageSegment.toSurahSlices(catalog: QuranCatalog): List<SurahSlice> =
    (start.surahId..end.surahId).map { surahId ->
        SurahSlice(
            surahId = surahId,
            startAyah = if (surahId == start.surahId) start.ayah else 1,
            endAyah = if (surahId == end.surahId) end.ayah else catalog.surahAyahCounts.getValue(surahId),
        )
    }

private data class SurahSlice(
    val surahId: Int,
    val startAyah: Int,
    val endAyah: Int,
) {
    fun ayahCount(): Int = endAyah - startAyah + 1

    fun isWholeSurah(catalog: QuranCatalog): Boolean =
        startAyah == 1 && endAyah == catalog.surahAyahCounts.getValue(surahId)

    fun toStartBoundary(catalog: QuranCatalog): Boundary =
        Boundary(
            surahId = surahId,
            surahNameArabic = catalog.requireSelection(SelectionCategory.Surahs, surahId).title
                .removePrefix("سورة "),
            ayah = startAyah,
        )

    fun toEndBoundary(catalog: QuranCatalog): Boundary =
        Boundary(
            surahId = surahId,
            surahNameArabic = catalog.requireSelection(SelectionCategory.Surahs, surahId).title
                .removePrefix("سورة "),
            ayah = endAyah,
        )
}
