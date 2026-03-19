package com.quran.tathbeet.sync

import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.QuranReadingTarget
import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ScheduleSelection
import com.quran.tathbeet.domain.model.SelectionCategory
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProfileSyncCoordinatorTest {

    @Test
    fun sign_in_immediately_syncs_self_profile_and_marks_it_as_solo_synced() = runTest {
        val profileRepository = FakeProfileRepository()
        val scheduleRepository = FakeScheduleRepository()
        val reviewRepository = FakeReviewRepository()
        val cloudStore = FakeCloudSyncStore()
        val coordinator = ProfileSyncCoordinator(
            profileRepository = profileRepository,
            scheduleRepository = scheduleRepository,
            reviewRepository = reviewRepository,
            cloudSyncStore = cloudStore,
        )

        coordinator.ensureSelfProfileSynced(
            user = AuthUser(
                uid = "uid-owner",
                email = "owner@example.com",
            ),
        )

        val syncedAccount = profileRepository.activeAccount.value!!
        val snapshot = cloudStore.snapshots[syncedAccount.cloudProfileId]

        assertEquals(ProfileSyncMode.SoloSynced, syncedAccount.syncMode)
        assertEquals("personal-uid-owner", syncedAccount.cloudProfileId)
        assertNotNull(snapshot)
        assertEquals("self", snapshot?.localProfileId)
        assertEquals("owner@example.com", snapshot?.ownerEmail)
        assertEquals("صاحب الحساب", snapshot?.displayName)
        assertEquals(1, snapshot?.reviewDays?.size)
    }

    @Test
    fun repeated_sync_with_the_same_snapshot_does_not_write_twice() = runTest {
        val profileRepository = FakeProfileRepository()
        val scheduleRepository = FakeScheduleRepository()
        val reviewRepository = FakeReviewRepository()
        val cloudStore = FakeCloudSyncStore()
        val coordinator = ProfileSyncCoordinator(
            profileRepository = profileRepository,
            scheduleRepository = scheduleRepository,
            reviewRepository = reviewRepository,
            cloudSyncStore = cloudStore,
        )

        val user = AuthUser(
            uid = "uid-owner",
            email = "owner@example.com",
        )

        coordinator.syncOwnedProfile(accountId = "self", user = user)
        coordinator.syncOwnedProfile(accountId = "self", user = user)

        assertEquals(1, cloudStore.upsertCalls)
        assertEquals("personal-uid-owner", profileRepository.activeAccount.value?.cloudProfileId)
    }
}

private class FakeProfileRepository : SyncProfileRepository {
    val activeAccount = MutableStateFlow(
        LearnerAccount(
            id = "self",
            name = "صاحب الحساب",
            isSelfProfile = true,
            isShared = false,
            notificationsEnabled = true,
        ),
    )

    override suspend fun getAccount(accountId: String): LearnerAccount? =
        if (activeAccount.value?.id == accountId) activeAccount.value else null

    override suspend fun getSelfProfile(): LearnerAccount? = activeAccount.value

    override suspend fun updateSyncState(
        accountId: String,
        syncMode: ProfileSyncMode,
        cloudProfileId: String?,
        isShared: Boolean,
    ) {
        val current = activeAccount.value ?: return
        activeAccount.value = current.copy(
            syncMode = syncMode,
            cloudProfileId = cloudProfileId,
            isShared = isShared,
        )
    }
}

private class FakeScheduleRepository : SyncScheduleRepository {
    override suspend fun getActiveSchedule(learnerId: String): RevisionSchedule =
        RevisionSchedule(
            id = "active-self",
            learnerId = learnerId,
            paceMethod = PaceMethod.CycleTarget,
            cycleTarget = CycleTarget.OneMonth,
            manualPace = PaceOption.OneJuz,
            selections = listOf(
                ScheduleSelection(
                    category = SelectionCategory.Juz,
                    itemId = 30,
                    displayOrder = 0,
                ),
            ),
        )
}

private class FakeReviewRepository : SyncReviewRepository {
    override suspend fun getReviewTimeline(learnerId: String): List<ReviewDay> =
        listOf(
            ReviewDay(
                learnerId = learnerId,
                assignedForDate = LocalDate.of(2026, 3, 14),
                completionRate = 50,
                assignments = listOf(
                    ReviewAssignment(
                        id = "assignment-1",
                        learnerId = learnerId,
                        assignedForDate = LocalDate.of(2026, 3, 14),
                        taskKey = "surah-1",
                        title = "الفاتحة",
                        detail = "7 آيات",
                        rubId = 1,
                        readingTarget = QuranReadingTarget(1, 1, 1, 7),
                        weight = 1.0,
                        displayOrder = 0,
                        isRollover = false,
                        isDone = true,
                        rating = 4,
                        completedAt = null,
                    ),
                ),
            ),
        )
}

private class FakeCloudSyncStore : CloudSyncStore {
    val snapshots = linkedMapOf<String, CloudProfileSnapshot>()
    var upsertCalls = 0

    override fun observeAccessibleProfiles(userId: String): Flow<List<CloudProfileSummary>> = flowOf(emptyList())

    override fun observeProfileSnapshot(cloudProfileId: String): Flow<CloudProfileSnapshot?> =
        flowOf(snapshots[cloudProfileId])

    override suspend fun claimAccessibleProfiles(user: AuthUser): List<CloudProfileSummary> = emptyList()

    override suspend fun fetchProfileSnapshot(cloudProfileId: String): CloudProfileSnapshot? =
        snapshots[cloudProfileId]

    override suspend fun upsertOwnedProfile(snapshot: CloudProfileSnapshot) {
        upsertCalls++
        snapshots[snapshot.cloudProfileId] = snapshot
    }

    override suspend fun listMembers(cloudProfileId: String): List<CloudProfileMember> = emptyList()

    override suspend fun inviteEditor(
        cloudProfileId: String,
        ownerUserId: String,
        email: String,
    ) = Unit

    override suspend fun removeMember(
        cloudProfileId: String,
        email: String,
    ) = Unit

    override suspend fun deleteProfile(cloudProfileId: String) = Unit
}
