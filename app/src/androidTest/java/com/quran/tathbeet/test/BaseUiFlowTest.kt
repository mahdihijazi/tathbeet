package com.quran.tathbeet.test

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.quran.tathbeet.R
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import com.quran.tathbeet.ui.TathbeetApp
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.SelectionCategory
import com.quran.tathbeet.ui.theme.TathbeetTheme
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class BaseUiFlowTest {

    @get:Rule
    val composeRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> =
        createAndroidComposeRule<ComponentActivity>()

    protected lateinit var appContainer: TestAppContainer

    @Before
    fun baseSetUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        appContainer = TestAppContainer(context)
        setAppContent()
    }

    protected fun setAppContent() {
        composeRule.setContent {
            TathbeetTheme {
                TathbeetApp(appContainer = appContainer)
            }
        }
    }

    @After
    fun baseTearDown() {
        appContainer.database.close()
    }

    protected fun completeOnboardingWithJuzOne() {
        tapNext()
        openJuzTab()
        selectVisibleJuz(1)
        tapNext()
        saveSchedule()
    }

    protected fun tapNext() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.action_next),
        ).performClick()
    }

    protected fun enterProfileName(name: String) {
        composeRule.onNodeWithTag("schedule-profile-name-input").performTextClearance()
        composeRule.onNodeWithTag("schedule-profile-name-input").performTextInput(name)
    }

    protected fun saveSchedule() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.schedule_save),
        ).performClick()
    }

    protected fun openProfilesTab() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.destination_profiles),
        ).performClick()
    }

    protected fun openAddProfileDialog() {
        composeRule.onNodeWithTag("screen-layout-list").performScrollToNode(
            hasTestTag("profiles-add-button"),
        )
        composeRule.onNodeWithTag("profiles-add-button").performClick()
    }

    protected fun openEditActiveProfileDialog() {
        composeRule.onNodeWithTag("profiles-edit-active").performClick()
    }

    protected fun enterProfileEditorName(name: String) {
        composeRule.onNodeWithTag("profiles-editor-name-input").performTextClearance()
        composeRule.onNodeWithTag("profiles-editor-name-input").performTextInput(name)
    }

    protected fun saveProfileDialog() {
        composeRule.onNodeWithTag("profiles-editor-save").performClick()
    }

    protected fun deleteProfileFromDialog() {
        composeRule.onNodeWithTag("profiles-editor-delete").performClick()
    }

    protected fun confirmProfileDeletion() {
        composeRule.onNodeWithTag("profiles-delete-confirm").performClick()
    }

    protected fun toggleProfileNotifications(profileId: String) {
        composeRule.onNodeWithTag("profiles-notifications-$profileId").performClick()
    }

    protected fun navigateBack() {
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_back),
        ).performClick()
    }

    protected fun openJuzTab() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.selection_category_juz),
        ).performClick()
    }

    protected fun openSurahTab() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.selection_category_surahs),
        ).performClick()
    }

    protected fun openRubTab() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.selection_category_rub),
        ).performClick()
    }

    protected fun selectCycleTarget(cycleTarget: CycleTarget) {
        composeRule.onNodeWithText(
            composeRule.activity.getString(cycleTarget.labelRes),
        ).performClick()
    }

    protected fun selectVisibleJuz(juzNumber: Int) {
        val optionTag = "pool-selector-option-juz-$juzNumber"
        composeRule.onNodeWithTag("pool-selector-options-list-Juz").performScrollToNode(hasTestTag(optionTag))
        composeRule.onNodeWithTag(optionTag).performClick()
    }

    protected fun selectVisibleSurah(surahNameArabic: String) {
        val optionTag = "pool-selector-option-surahs-${surahIdForName(surahNameArabic)}"
        composeRule.onNodeWithTag("pool-selector-options-list-Surahs").performScrollToNode(hasTestTag(optionTag))
        composeRule.onNodeWithTag(optionTag).performClick()
    }

    protected fun selectVisibleRub(rubNumber: Int) {
        val optionTag = "pool-selector-option-rub-$rubNumber"
        composeRule.onNodeWithTag("pool-selector-options-list-Rub").performScrollToNode(hasTestTag(optionTag))
        composeRule.onNodeWithTag(optionTag).performClick()
    }

    protected fun assertReviewVisible() {
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                composeRule.activity.getString(R.string.review_progress_title),
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    protected fun assertPoolSelectorVisible() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.pool_selector_subtitle),
        ).assertIsDisplayed()
    }

    protected fun assertScheduleDoseVisible() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.schedule_dose_subtitle),
        ).assertIsDisplayed()
    }

    protected fun assertIntroVisible() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.schedule_intro_body),
        ).assertIsDisplayed()
    }

    protected fun assertProfilesVisible() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.profile_screen_subtitle),
        ).assertIsDisplayed()
    }

    protected fun completeReviewTask(
        taskId: String,
        rating: Int? = null,
    ) {
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasTestTag("review-task-$taskId"),
        )
        composeRule.onNodeWithTag("review-complete-$taskId").performClick()
        if (rating != null) {
            composeRule.onNodeWithTag("review-inline-rating-$taskId-$rating").performClick()
        }
    }

    protected fun scrollReviewListToTag(tag: String) {
        composeRule.onNodeWithTag("review-sections-list").performScrollToNode(
            hasTestTag(tag),
        )
    }

    protected fun todayDate(): LocalDate = appContainer.timeProvider.today()

    protected fun awaitReviewDay(date: LocalDate): ReviewDay = runBlocking {
        val account = appContainer.profileRepository.observeActiveAccount()
            .filterNotNull()
            .first()
        appContainer.reviewRepository.observeReviewDay(account.id, date)
            .filterNotNull()
            .first()
    }

    protected fun awaitTodayReviewDay(): ReviewDay = awaitReviewDay(todayDate())

    protected fun awaitReviewTimeline(): List<ReviewDay> = runBlocking {
        appContainer.reviewRepository.observeReviewTimeline(activeAccountId()).first()
    }

    protected fun firstTodayAssignment(): ReviewAssignment =
        awaitTodayReviewDay().assignments.first()

    protected fun activeAccountId(): String = runBlocking {
        appContainer.profileRepository.observeActiveAccount()
            .filterNotNull()
            .first()
            .id
    }

    private fun surahIdForName(surahNameArabic: String): Int =
        appContainer.quranCatalogRepository.getCatalog()
            .itemsFor(SelectionCategory.Surahs)
            .first { surah ->
                surah.title == composeRule.activity.getString(R.string.quran_surah_title, surahNameArabic)
            }
            .itemId
}

class TestAppContainer(
    context: Context,
) : AppContainer(
    context = context,
    timeProvider = object : TimeProvider {
        override fun today(): LocalDate = LocalDate.of(2026, 3, 10)

        override fun now(): ZonedDateTime =
            ZonedDateTime.of(2026, 3, 10, 8, 0, 0, 0, ZoneId.of("UTC"))

        override fun zoneId(): ZoneId = ZoneId.of("UTC")
    },
    database = Room.inMemoryDatabaseBuilder(context, TathbeetDatabase::class.java)
        .allowMainThreadQueries()
        .build(),
) {
    val recordingQuranExternalLauncher = RecordingQuranExternalLauncher()

    override val quranExternalLauncher = recordingQuranExternalLauncher
}
