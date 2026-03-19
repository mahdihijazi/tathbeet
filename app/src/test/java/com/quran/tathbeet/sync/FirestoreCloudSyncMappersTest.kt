package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewAssignment
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreCloudSyncMappersTest {

    @Test
    fun normalize_email_trims_and_lowercases_for_firestore_ids() {
        assertEquals("owner@example.com", "  Owner@Example.com  ".normalizeEmail())
        assertEquals("owner@example.com", "  Owner@Example.com  ".documentId())
    }

    @Test
    fun member_roles_map_to_expected_sync_modes() {
        assertEquals(ProfileSyncMode.SharedOwner, CloudProfileMemberRole.Owner.toProfileSyncMode(isShared = true))
        assertEquals(ProfileSyncMode.SoloSynced, CloudProfileMemberRole.Owner.toProfileSyncMode(isShared = false))
        assertEquals(ProfileSyncMode.SharedEditor, CloudProfileMemberRole.Editor.toProfileSyncMode(isShared = true))
        assertEquals(ProfileSyncMode.SharedEditor, CloudProfileMemberRole.Editor.toProfileSyncMode(isShared = false))
    }

    @Test
    fun completion_rate_handles_empty_and_partial_timelines() {
        assertEquals(0, completionRateFor(emptyList()))
        assertEquals(
            50,
            completionRateFor(
                listOf(
                    reviewAssignment(done = true),
                    reviewAssignment(done = false),
                ),
            ),
        )
    }

    private fun reviewAssignment(done: Boolean): ReviewAssignment =
        ReviewAssignment(
            id = if (done) "done" else "todo",
            learnerId = "self",
            assignedForDate = LocalDate.of(2026, 3, 19),
            taskKey = "task-${if (done) "done" else "todo"}",
            title = "Task",
            detail = "Detail",
            rubId = 1,
            readingTarget = null,
            weight = 1.0,
            displayOrder = 0,
            isRollover = false,
            isDone = done,
            rating = null,
            completedAt = null,
        )
}
