package com.quran.tathbeet.test

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.quran.tathbeet.MainActivity
import com.quran.tathbeet.R
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class BaseMainActivityUiTest {

    @get:Rule
    val composeRule: ComposeTestRule = createEmptyComposeRule()

    protected val targetContext: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun clearAppStateBeforeTest() {
        clearDatabase()
    }

    @After
    fun clearAppStateAfterTest() {
        clearDatabase()
    }

    protected fun launchMainActivity(): ActivityScenario<MainActivity> =
        ActivityScenario.launch(Intent(targetContext, MainActivity::class.java))

    protected fun tapNext() {
        composeRule.onNodeWithText(
            targetContext.getString(R.string.action_next),
        ).performClick()
    }

    protected fun openJuzTab() {
        composeRule.onNodeWithText(
            targetContext.getString(R.string.selection_category_juz),
        ).performClick()
    }

    protected fun selectVisibleJuz(juzNumber: Int) {
        val title = targetContext.getString(R.string.quran_juz_title, juzNumber)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(title).performClick()
    }

    protected fun saveSchedule() {
        composeRule.onNodeWithText(
            targetContext.getString(R.string.schedule_save),
        ).performClick()
    }

    protected fun assertReviewVisible() {
        composeRule.onNodeWithContentDescription(
            targetContext.getString(R.string.content_edit_plan),
        ).assertIsDisplayed()
    }

    protected fun assertIntroVisible() {
        composeRule.onNodeWithText(
            targetContext.getString(R.string.schedule_intro_body),
        ).assertIsDisplayed()
    }

    private fun clearDatabase() {
        targetContext.deleteDatabase("tathbeet.db")
        targetContext.deleteDatabase("tathbeet.db-shm")
        targetContext.deleteDatabase("tathbeet.db-wal")
    }
}
