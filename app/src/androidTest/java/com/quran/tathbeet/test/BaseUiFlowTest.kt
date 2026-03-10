package com.quran.tathbeet.test

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.quran.tathbeet.R
import com.quran.tathbeet.app.AppContainer
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.ui.TathbeetApp
import com.quran.tathbeet.ui.theme.TathbeetTheme
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
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

    protected fun saveSchedule() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.schedule_save),
        ).performClick()
    }

    protected fun openJuzTab() {
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.selection_category_juz),
        ).performClick()
    }

    protected fun selectVisibleJuz(juzNumber: Int) {
        val title = composeRule.activity.getString(R.string.quran_juz_title, juzNumber)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(title).performClick()
    }

    protected fun assertReviewVisible() {
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.content_edit_plan),
        ).assertIsDisplayed()
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
)
