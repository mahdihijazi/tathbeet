package com.quran.tathbeet.ui.features.profiles

import com.quran.tathbeet.app.LocalReminderScheduler
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.domain.model.AppSettings
import com.quran.tathbeet.domain.model.AppThemeMode
import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ScheduleSelection
import com.quran.tathbeet.domain.model.SelectionCategory
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.domain.repository.SettingsRepository
import com.quran.tathbeet.testutil.MainDispatcherRule
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cached_profile_state_renders_before_background_bootstrap_finishes() = runTest {
        val today = LocalDate.of(2026, 3, 19)
        val ensureGate = CompletableDeferred<Unit>()
        val profile = LearnerAccount(
            id = "self",
            name = "حسابي",
            isSelfProfile = true,
            isShared = false,
            notificationsEnabled = true,
            syncMode = ProfileSyncMode.SoloSynced,
            cloudProfileId = "personal-self",
        )
        val profiles = MutableStateFlow(listOf(profile))
        val activeAccount = MutableStateFlow<LearnerAccount?>(profile)
        val reviewRepository = FakeReviewRepository(
            timeline = listOf(
                reviewDay(
                    learnerId = profile.id,
                    date = today,
                    assignments = listOf(
                        reviewAssignment(id = "today-pending", date = today, rubId = 5),
                    ),
                ),
            ),
            ensureGate = ensureGate,
        )
        val viewModel = ProfilesViewModel(
            profileRepository = FakeProfileRepository(profiles, activeAccount),
            scheduleRepository = FakeScheduleRepository(),
            reviewRepository = reviewRepository,
            settingsRepository = FakeSettingsRepository(),
            timeProvider = FixedTimeProvider(today),
            localReminderScheduler = NoOpLocalReminderScheduler,
        )

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.profiles.isNotEmpty())
        assertEquals("self", viewModel.uiState.value.activeProfile?.id)
        assertEquals(listOf("self"), viewModel.uiState.value.profiles.map { it.id })
        assertEquals(1, viewModel.uiState.value.profiles.first().todayTotalCount)
        assertEquals(listOf(today), reviewRepository.ensureCalls)

        ensureGate.complete(Unit)
        advanceUntilIdle()
    }
}

private class FakeProfileRepository(
    private val accounts: MutableStateFlow<List<LearnerAccount>>,
    private val activeAccount: MutableStateFlow<LearnerAccount?>,
) : ProfileRepository {
    override fun observeAccounts(): Flow<List<LearnerAccount>> = accounts

    override fun observeActiveAccount(): Flow<LearnerAccount?> = activeAccount

    override suspend fun ensureDefaultAccount(name: String) = Unit

    override suspend fun createProfile(name: String): LearnerAccount =
        error("not used")

    override suspend fun updateAccountName(accountId: String, name: String) = Unit

    override suspend fun updateNotificationsEnabled(accountId: String, enabled: Boolean) = Unit

    override suspend fun deleteProfile(accountId: String) = Unit

    override suspend fun setActiveAccount(accountId: String) = Unit
}

private class FakeScheduleRepository(
    private val schedule: RevisionSchedule = RevisionSchedule(
        id = "active-self",
        learnerId = "self",
        paceMethod = PaceMethod.Manual,
        cycleTarget = CycleTarget.OneWeek,
        manualPace = PaceOption.OneRub,
        selections = listOf(
            ScheduleSelection(
                category = SelectionCategory.Surahs,
                itemId = 1,
                displayOrder = 0,
            ),
        ),
    ),
) : ScheduleRepository {
    override fun observeActiveSchedule(learnerId: String): Flow<RevisionSchedule?> = flowOf(schedule)

    override suspend fun saveSchedule(schedule: RevisionSchedule) = Unit
}

private class FakeReviewRepository(
    timeline: List<ReviewDay>,
    private val ensureGate: CompletableDeferred<Unit>? = null,
) : ReviewRepository {
    private val timelineFlow = MutableStateFlow(timeline)

    val ensureCalls = mutableListOf<LocalDate>()

    override fun observeReviewTimeline(learnerId: String): Flow<List<ReviewDay>> = timelineFlow

    override fun observeReviewDay(
        learnerId: String,
        assignedForDate: LocalDate,
    ): Flow<ReviewDay?> = flowOf(timelineFlow.value.firstOrNull { it.assignedForDate == assignedForDate })

    override suspend fun ensureAssignmentsForDate(
        learnerId: String,
        assignedForDate: LocalDate,
    ): Boolean {
        ensureCalls += assignedForDate
        ensureGate?.await()
        return timelineFlow.value.any { it.assignedForDate == assignedForDate }
    }

    override suspend fun completeAssignment(assignmentId: String, rating: Int) = Unit

    override suspend fun updateAssignmentRating(assignmentId: String, rating: Int) = Unit

    override suspend fun refreshForScheduleChange(learnerId: String, restartDate: LocalDate) = Unit

    override suspend fun restartCycle(learnerId: String, restartDate: LocalDate) = Unit
}

private class FakeSettingsRepository : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = flowOf(AppSettings(hasSeenScheduleIntro = true))

    override suspend fun markScheduleIntroSeen() = Unit

    override suspend fun setGlobalNotificationsEnabled(enabled: Boolean) = Unit

    override suspend fun setMotivationalMessagesEnabled(enabled: Boolean) = Unit

    override suspend fun setReminderTime(
        hour: Int,
        minute: Int,
    ) = Unit

    override suspend fun setThemeMode(themeMode: AppThemeMode) = Unit
}

private object NoOpLocalReminderScheduler : LocalReminderScheduler {
    override suspend fun syncSchedules() = Unit

    override suspend fun cancelProfile(profileId: String) = Unit

    override suspend fun handleReminder(profileId: String) = Unit
}

private class FixedTimeProvider(
    private val today: LocalDate,
) : TimeProvider {
    override fun today(): LocalDate = today

    override fun now(): ZonedDateTime = ZonedDateTime.of(today.year, today.monthValue, today.dayOfMonth, 12, 0, 0, 0, ZoneId.of("UTC"))

    override fun zoneId(): ZoneId = ZoneId.of("UTC")
}

private fun reviewDay(
    learnerId: String,
    date: LocalDate,
    assignments: List<ReviewAssignment>,
): ReviewDay =
    ReviewDay(
        learnerId = learnerId,
        assignedForDate = date,
        completionRate = if (assignments.isEmpty()) 0 else (assignments.count { it.isDone } * 100) / assignments.size,
        assignments = assignments,
    )

private fun reviewAssignment(
    id: String,
    date: LocalDate,
    rubId: Int,
    isDone: Boolean = false,
): ReviewAssignment =
    ReviewAssignment(
        id = id,
        learnerId = "self",
        assignedForDate = date,
        taskKey = "rub-$rubId",
        title = id,
        detail = "detail-$id",
        rubId = rubId,
        readingTarget = null,
        weight = 1.0,
        displayOrder = rubId,
        isRollover = false,
        isDone = isDone,
        rating = if (isDone) 4 else null,
        completedAt = if (isDone) {
            ZonedDateTime.of(2026, 3, 19, 12, 0, 0, 0, ZoneId.of("UTC"))
        } else {
            null
        },
    )
