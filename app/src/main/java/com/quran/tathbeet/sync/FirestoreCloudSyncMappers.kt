package com.quran.tathbeet.sync

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.QuranReadingTarget
import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ScheduleSelection
import com.quran.tathbeet.domain.model.SelectionCategory
import java.time.LocalDate
import java.time.ZonedDateTime

internal fun QuerySnapshot?.toCloudProfileSummaries(): List<CloudProfileSummary> =
    this?.documents.orEmpty()
        .map { document ->
            CloudProfileSummary(
                cloudProfileId = document.id,
                displayName = document.getString("displayName").orEmpty(),
                syncMode = document.getString("syncMode")
                    ?.let { value -> enumValueOf<ProfileSyncMode>(value) }
                    ?: ProfileSyncMode.LocalOnly,
                ownerEmail = document.getString("ownerEmail"),
                memberRole = document.getString("role").toMemberRole(),
            )
        }
        .sortedBy { summary -> summary.displayName }

internal fun com.google.firebase.firestore.DocumentSnapshot.toRevisionSchedule(
    profileId: String,
): RevisionSchedule? {
    val paceMethod = getString("paceMethod") ?: return null
    val cycleTargetDays = getLong("cycleTargetDays")?.toInt() ?: CycleTarget.OneMonth.days
    val manualPaceSegments = getLong("manualPaceSegments")?.toInt() ?: PaceOption.OneRub.dailySegments
    @Suppress("UNCHECKED_CAST")
    val selections = (get("selections") as? List<Map<String, Any?>>).orEmpty()
        .mapIndexedNotNull { index, selection ->
            val category = selection["category"] as? String ?: return@mapIndexedNotNull null
            val itemId = (selection["itemId"] as? Number)?.toInt() ?: return@mapIndexedNotNull null
            ScheduleSelection(
                category = SelectionCategory.valueOf(category),
                itemId = itemId,
                displayOrder = (selection["displayOrder"] as? Number)?.toInt() ?: index,
            )
        }
    return RevisionSchedule(
        id = "active-$profileId",
        learnerId = profileId,
        paceMethod = PaceMethod.valueOf(paceMethod),
        cycleTarget = CycleTarget.entries.firstOrNull { it.days == cycleTargetDays } ?: CycleTarget.OneMonth,
        manualPace = PaceOption.entries.firstOrNull { it.dailySegments == manualPaceSegments } ?: PaceOption.OneRub,
        selections = selections,
    )
}

internal fun com.google.firebase.firestore.DocumentSnapshot.toReviewAssignment(
    profileId: String,
): ReviewAssignment? {
    val assignedForDate = getString("assignedForDate")?.let(LocalDate::parse) ?: return null
    val taskKey = getString("taskKey") ?: return null
    val title = getString("title") ?: return null
    val detail = getString("detail") ?: return null
    val rubId = getLong("rubId")?.toInt() ?: return null
    val completedAt = getString("completedAt")?.let(ZonedDateTime::parse)
    @Suppress("UNCHECKED_CAST")
    val readingTargetMap = get("readingTarget") as? Map<String, Any?>
    return ReviewAssignment(
        id = id,
        learnerId = profileId,
        assignedForDate = assignedForDate,
        taskKey = taskKey,
        title = title,
        detail = detail,
        rubId = rubId,
        readingTarget = readingTargetMap?.toReadingTarget(),
        weight = (get("weight") as? Number)?.toDouble() ?: 1.0,
        displayOrder = getLong("displayOrder")?.toInt().orZero(),
        isRollover = getBoolean("isRollover") ?: false,
        isDone = getBoolean("isDone") ?: false,
        rating = getLong("rating")?.toInt(),
        completedAt = completedAt,
    )
}

internal fun RevisionSchedule?.toPlanMap(): Map<String, Any> =
    this?.let { schedule ->
        mapOf(
            "paceMethod" to schedule.paceMethod.name,
            "cycleTargetDays" to schedule.cycleTarget.days,
            "manualPaceSegments" to schedule.manualPace.dailySegments,
            "selections" to schedule.selections.map { selection ->
                mapOf(
                    "category" to selection.category.name,
                    "itemId" to selection.itemId,
                    "displayOrder" to selection.displayOrder,
                )
            },
            "updatedAt" to FieldValue.serverTimestamp(),
        )
    } ?: mapOf(
        "selections" to emptyList<Map<String, Any>>(),
        "updatedAt" to FieldValue.serverTimestamp(),
    )

internal fun ReviewAssignment.toTaskMap(): Map<String, Any?> =
    buildMap {
        put("assignedForDate", assignedForDate.toString())
        put("taskKey", taskKey)
        put("title", title)
        put("detail", detail)
        put("rubId", rubId)
        put("weight", weight)
        put("displayOrder", displayOrder)
        put("isRollover", isRollover)
        put("isDone", isDone)
        put("rating", rating)
        put("completedAt", completedAt?.toString())
        put("updatedAt", FieldValue.serverTimestamp())
        readingTarget?.let { target ->
            put(
                "readingTarget",
                mapOf(
                    "startSurahId" to target.startSurahId,
                    "startAyah" to target.startAyah,
                    "endSurahId" to target.endSurahId,
                    "endAyah" to target.endAyah,
                ),
            )
        }
    }

internal fun Map<String, Any?>.toReadingTarget(): QuranReadingTarget? {
    val startSurahId = (get("startSurahId") as? Number)?.toInt() ?: return null
    val startAyah = (get("startAyah") as? Number)?.toInt() ?: return null
    val endSurahId = (get("endSurahId") as? Number)?.toInt() ?: return null
    val endAyah = (get("endAyah") as? Number)?.toInt() ?: return null
    return QuranReadingTarget(
        startSurahId = startSurahId,
        startAyah = startAyah,
        endSurahId = endSurahId,
        endAyah = endAyah,
    )
}

internal fun String?.toMemberRole(): CloudProfileMemberRole =
    when (this) {
        CloudProfileMemberRole.Owner.name -> CloudProfileMemberRole.Owner
        else -> CloudProfileMemberRole.Editor
    }

internal fun CloudProfileMemberRole.toProfileSyncMode(isShared: Boolean): ProfileSyncMode =
    when (this) {
        CloudProfileMemberRole.Owner -> if (isShared) {
            ProfileSyncMode.SharedOwner
        } else {
            ProfileSyncMode.SoloSynced
        }
        CloudProfileMemberRole.Editor -> ProfileSyncMode.SharedEditor
    }

internal fun completionRateFor(assignments: List<ReviewAssignment>): Int {
    if (assignments.isEmpty()) return 0
    val completed = assignments.count { assignment -> assignment.isDone }
    return (completed * 100) / assignments.size
}

internal fun String?.normalizeEmail(): String = this?.trim()?.lowercase().orEmpty()

internal fun String?.documentId(): String = normalizeEmail()

internal fun Int?.orZero(): Int = this ?: 0
