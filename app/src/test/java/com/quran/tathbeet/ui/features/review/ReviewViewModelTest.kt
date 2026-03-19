package com.quran.tathbeet.ui.features.review

import com.quran.tathbeet.app.QuranExternalLauncher
import com.quran.tathbeet.app.QuranLaunchResult
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.model.ProfileSyncMode
import com.quran.tathbeet.domain.model.QuranReadingTarget
import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.testutil.MainDispatcherRule
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
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
class ReviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun daily_tab_hides_completed_past_tasks_but_keeps_todays_completed_tasks_visible() = runTest {
        val today = LocalDate.of(2026, 3, 14)
        val reviewRepository = FakeReviewRepository(
            timeline = listOf(
                reviewDay(
                    learnerId = "learner-1",
                    date = today.minusDays(1),
                    assignments = listOf(
                        reviewAssignment(id = "yesterday-done", date = today.minusDays(1), rubId = 2, isDone = true),
                        reviewAssignment(id = "yesterday-pending", date = today.minusDays(1), rubId = 3),
                    ),
                ),
                reviewDay(
                    learnerId = "learner-1",
                    date = today,
                    assignments = listOf(
                        reviewAssignment(id = "today-done", date = today, rubId = 4, isDone = true),
                        reviewAssignment(id = "today-pending", date = today, rubId = 5),
                    ),
                ),
                reviewDay(
                    learnerId = "learner-1",
                    date = today.plusDays(1),
                    assignments = listOf(
                        reviewAssignment(id = "tomorrow-pending", date = today.plusDays(1), rubId = 6),
                    ),
                ),
            ),
        )

        val viewModel = ReviewViewModel(
            profileRepository = FakeProfileRepository(),
            reviewRepository = reviewRepository,
            timeProvider = FixedTimeProvider(today),
            quranExternalLauncher = NoOpQuranExternalLauncher,
        )

        advanceUntilIdle()

        assertEquals(
            listOf("yesterday-pending"),
            viewModel.uiState.value.sections.first { it.id == "overdue" }.tasks.map { it.id },
        )
        assertEquals(
            listOf("today-done", "today-pending"),
            viewModel.uiState.value.sections.first { it.id == today.toString() }.tasks.map { it.id },
        )
        assertEquals(
            listOf("tomorrow-pending"),
            viewModel.uiState.value.sections.first { it.id == today.plusDays(1).toString() }.tasks.map { it.id },
        )
        assertFalse(viewModel.uiState.value.sections.flatMap { it.tasks }.any { it.id == "yesterday-done" })
        assertTrue(viewModel.uiState.value.sections.flatMap { it.tasks }.any { it.id == "today-done" })
        assertEquals(
            setOf("yesterday-done", "yesterday-pending", "today-done", "today-pending", "tomorrow-pending"),
            viewModel.uiState.value.fullPlanTasks.map { it.id }.toSet(),
        )
        assertEquals(listOf(today), reviewRepository.ensureCalls)
    }

    @Test
    fun completing_task_from_daily_tab_keeps_it_visible_for_rating() = runTest {
        val today = LocalDate.of(2026, 3, 14)
        val reviewRepository = FakeReviewRepository(
            timeline = listOf(
                reviewDay(
                    learnerId = "learner-1",
                    date = today,
                    assignments = listOf(
                        reviewAssignment(id = "today-pending", date = today, rubId = 5),
                    ),
                ),
            ),
        )
        val viewModel = ReviewViewModel(
            profileRepository = FakeProfileRepository(),
            reviewRepository = reviewRepository,
            timeProvider = FixedTimeProvider(today),
            quranExternalLauncher = NoOpQuranExternalLauncher,
        )

        advanceUntilIdle()
        viewModel.requestCompleteTask("today-pending")
        advanceUntilIdle()
        viewModel.updateTaskRating("today-pending", 1)
        advanceUntilIdle()

        assertEquals(
            listOf("today-pending"),
            viewModel.uiState.value.sections.first { it.id == today.toString() }.tasks.map { it.id },
        )
        assertTrue(reviewRepository.completedAssignments.contains("today-pending"))
        assertTrue(viewModel.uiState.value.sections.flatMap { it.tasks }.first { it.id == "today-pending" }.isDone)
        assertEquals(1, viewModel.uiState.value.sections.flatMap { it.tasks }.first { it.id == "today-pending" }.rating)
        assertTrue(viewModel.uiState.value.fullPlanTasks.first { it.id == "today-pending" }.isDone)
    }

    @Test
    fun daily_tab_keeps_today_section_when_it_only_has_completed_tasks() = runTest {
        val today = LocalDate.of(2026, 3, 14)
        val viewModel = ReviewViewModel(
            profileRepository = FakeProfileRepository(),
            reviewRepository = FakeReviewRepository(
                timeline = listOf(
                    reviewDay(
                        learnerId = "learner-1",
                        date = today,
                        assignments = listOf(
                            reviewAssignment(id = "today-done", date = today, rubId = 4, isDone = true),
                        ),
                    ),
                    reviewDay(
                        learnerId = "learner-1",
                        date = today.plusDays(1),
                        assignments = listOf(
                            reviewAssignment(id = "tomorrow-pending", date = today.plusDays(1), rubId = 5),
                        ),
                    ),
                ),
            ),
            timeProvider = FixedTimeProvider(today),
            quranExternalLauncher = NoOpQuranExternalLauncher,
        )

        advanceUntilIdle()

        assertEquals(
            listOf(today.toString(), today.plusDays(1).toString()),
            viewModel.uiState.value.sections.map { it.id },
        )
        assertEquals(
            listOf("today-done"),
            viewModel.uiState.value.sections.first { it.id == today.toString() }.tasks.map { it.id },
        )
    }

    @Test
    fun full_plan_quran_order_sort_keeps_completed_tasks_between_new_before_and_after_tasks() = runTest {
        val today = LocalDate.of(2026, 3, 14)
        val viewModel = ReviewViewModel(
            profileRepository = FakeProfileRepository(),
            reviewRepository = FakeReviewRepository(
                timeline = listOf(
                    reviewDay(
                        learnerId = "learner-1",
                        date = today,
                        assignments = listOf(
                            reviewAssignment(id = "completed-middle", date = today, rubId = 2, isDone = true),
                            reviewAssignment(id = "pending-early", date = today, rubId = 1),
                        ),
                    ),
                    reviewDay(
                        learnerId = "learner-1",
                        date = today.plusDays(1),
                        assignments = listOf(
                            reviewAssignment(id = "pending-late", date = today.plusDays(1), rubId = 3),
                        ),
                    ),
                ),
            ),
            timeProvider = FixedTimeProvider(today),
            quranExternalLauncher = NoOpQuranExternalLauncher,
        )

        advanceUntilIdle()
        viewModel.selectFullPlanSortMode(ReviewFullPlanSortMode.QuranOrder)

        assertEquals(
            listOf("pending-early", "completed-middle", "pending-late"),
            viewModel.uiState.value.fullPlanTasks.map { it.id },
        )
    }

    @Test
    fun active_account_row_changes_with_the_same_id_do_not_restart_daily_bootstrap() = runTest {
        val today = LocalDate.of(2026, 3, 14)
        val activeAccount = MutableStateFlow<LearnerAccount?>(
            LearnerAccount(
                id = "learner-1",
                name = "أحمد",
                isSelfProfile = true,
                isShared = false,
                notificationsEnabled = true,
                syncMode = ProfileSyncMode.SoloSynced,
            ),
        )
        val reviewRepository = FakeReviewRepository(
            timeline = listOf(
                reviewDay(
                    learnerId = "learner-1",
                    date = today,
                    assignments = listOf(
                        reviewAssignment(id = "today-pending", date = today, rubId = 5),
                    ),
                ),
            ),
        )
        val viewModel = ReviewViewModel(
            profileRepository = MutableProfileRepository(activeAccount),
            reviewRepository = reviewRepository,
            timeProvider = FixedTimeProvider(today),
            quranExternalLauncher = NoOpQuranExternalLauncher,
        )

        advanceUntilIdle()
        activeAccount.value = activeAccount.value?.copy(
            name = "أحمد المعدل",
        )
        advanceUntilIdle()

        assertEquals(listOf(today), reviewRepository.ensureCalls)
        assertTrue(viewModel.uiState.value.sections.isNotEmpty())
    }

    @Test
    fun cached_review_state_renders_before_background_bootstrap_finishes() = runTest {
        val today = LocalDate.of(2026, 3, 14)
        val ensureGate = CompletableDeferred<Unit>()
        val reviewRepository = FakeReviewRepository(
            timeline = listOf(
                reviewDay(
                    learnerId = "learner-1",
                    date = today,
                    assignments = listOf(
                        reviewAssignment(id = "today-pending", date = today, rubId = 5),
                    ),
                ),
            ),
            ensureGate = ensureGate,
        )
        val viewModel = ReviewViewModel(
            profileRepository = FakeProfileRepository(),
            reviewRepository = reviewRepository,
            timeProvider = FixedTimeProvider(today),
            quranExternalLauncher = NoOpQuranExternalLauncher,
        )

        runCurrent()

        assertEquals(listOf(today), reviewRepository.ensureCalls)
        assertEquals(
            listOf("today-pending"),
            viewModel.uiState.value.sections.first { it.id == today.toString() }.tasks.map { it.id },
        )
        ensureGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun cycle_complete_dialog_stays_hidden_while_initial_refresh_is_pending() = runTest {
        val today = LocalDate.of(2026, 3, 14)
        val ensureGate = CompletableDeferred<Unit>()
        val reviewRepository = FakeReviewRepository(
            timeline = listOf(
                reviewDay(
                    learnerId = "learner-1",
                    date = today,
                    assignments = listOf(
                        reviewAssignment(id = "today-done", date = today, rubId = 5, isDone = true),
                    ),
                ),
            ),
            ensureGate = ensureGate,
        )
        val viewModel = ReviewViewModel(
            profileRepository = FakeProfileRepository(),
            reviewRepository = reviewRepository,
            timeProvider = FixedTimeProvider(today),
            quranExternalLauncher = NoOpQuranExternalLauncher,
        )

        runCurrent()

        assertFalse(viewModel.uiState.value.showCycleResetDialog)

        reviewRepository.timelineFlow.value = listOf(
            reviewDay(
                learnerId = "learner-1",
                date = today,
                assignments = listOf(
                    reviewAssignment(id = "today-pending", date = today, rubId = 5),
                ),
            ),
        )
        ensureGate.complete(Unit)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCycleResetDialog)
        assertEquals(
            listOf("today-pending"),
            viewModel.uiState.value.sections.first { it.id == today.toString() }.tasks.map { it.id },
        )
    }
}

private class FakeProfileRepository : ProfileRepository {
    override fun observeAccounts(): Flow<List<LearnerAccount>> = flowOf(listOf(activeAccount))

    override fun observeActiveAccount(): Flow<LearnerAccount?> = flowOf(activeAccount)

    override suspend fun ensureDefaultAccount(name: String) = Unit

    override suspend fun createProfile(name: String): LearnerAccount = activeAccount

    override suspend fun updateAccountName(accountId: String, name: String) = Unit

    override suspend fun updateNotificationsEnabled(accountId: String, enabled: Boolean) = Unit

    override suspend fun deleteProfile(accountId: String) = Unit

    override suspend fun setActiveAccount(accountId: String) = Unit

    private companion object {
        val activeAccount = LearnerAccount(
            id = "learner-1",
            name = "أحمد",
            isSelfProfile = true,
            isShared = false,
            notificationsEnabled = true,
            syncMode = ProfileSyncMode.LocalOnly,
        )
    }
}

private class MutableProfileRepository(
    private val activeAccount: MutableStateFlow<LearnerAccount?>,
) : ProfileRepository {
    override fun observeAccounts(): Flow<List<LearnerAccount>> = flowOf(listOfNotNull(activeAccount.value))

    override fun observeActiveAccount(): Flow<LearnerAccount?> = activeAccount

    override suspend fun ensureDefaultAccount(name: String) = Unit

    override suspend fun createProfile(name: String): LearnerAccount =
        error("not used")

    override suspend fun updateAccountName(accountId: String, name: String) = Unit

    override suspend fun updateNotificationsEnabled(accountId: String, enabled: Boolean) = Unit

    override suspend fun deleteProfile(accountId: String) = Unit

    override suspend fun setActiveAccount(accountId: String) = Unit
}

private class FakeReviewRepository(
    timeline: List<ReviewDay>,
    private val ensureGate: CompletableDeferred<Unit>? = null,
) : ReviewRepository {
    val timelineFlow = MutableStateFlow(timeline)

    val ensureCalls = mutableListOf<LocalDate>()
    val completedAssignments = mutableListOf<String>()

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

    override suspend fun completeAssignment(
        assignmentId: String,
        rating: Int,
    ) {
        completedAssignments += assignmentId
        timelineFlow.value = timelineFlow.value.map { day ->
            day.copy(
                assignments = day.assignments.map { assignment ->
                    if (assignment.id == assignmentId) {
                        assignment.copy(
                            isDone = true,
                            rating = rating,
                            completedAt = ZonedDateTime.of(2026, 3, 14, 12, 0, 0, 0, ZoneId.of("UTC")),
                        )
                    } else {
                        assignment
                    }
                },
            )
        }
    }

    override suspend fun updateAssignmentRating(
        assignmentId: String,
        rating: Int,
    ) {
        timelineFlow.value = timelineFlow.value.map { day ->
            day.copy(
                assignments = day.assignments.map { assignment ->
                    if (assignment.id == assignmentId) assignment.copy(rating = rating) else assignment
                },
            )
        }
    }

    override suspend fun refreshForScheduleChange(
        learnerId: String,
        restartDate: LocalDate,
    ) = Unit

    override suspend fun restartCycle(
        learnerId: String,
        restartDate: LocalDate,
    ) = Unit
}

private class FixedTimeProvider(
    private val today: LocalDate,
) : TimeProvider {
    override fun today(): LocalDate = today

    override fun now(): ZonedDateTime = ZonedDateTime.of(today.year, today.monthValue, today.dayOfMonth, 12, 0, 0, 0, ZoneId.of("UTC"))

    override fun zoneId(): ZoneId = ZoneId.of("UTC")
}

private object NoOpQuranExternalLauncher : QuranExternalLauncher {
    override fun openReadingTarget(target: QuranReadingTarget): QuranLaunchResult =
        QuranLaunchResult.LaunchedInApp

    override fun openQuranAndroidInstallPage() = Unit

    override fun openReadingTargetOnWeb(target: QuranReadingTarget) = Unit
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
        learnerId = "learner-1",
        assignedForDate = date,
        taskKey = "rub-$rubId",
        title = id,
        detail = "detail-$id",
        rubId = rubId,
        readingTarget = QuranReadingTarget(rubId, 1, rubId, 7),
        weight = 1.0,
        displayOrder = rubId,
        isRollover = date.isBefore(LocalDate.of(2026, 3, 14)),
        isDone = isDone,
        rating = if (isDone) 4 else null,
        completedAt = if (isDone) {
            ZonedDateTime.of(2026, 3, 13, 12, 0, 0, 0, ZoneId.of("UTC"))
        } else {
            null
        },
    )
