package com.quran.tathbeet.feature.schedule

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.quran.tathbeet.ui.features.schedule.PoolSelectorTabsPanel
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.SelectionCategory
import com.quran.tathbeet.ui.model.selectionKey
import com.quran.tathbeet.ui.theme.TathbeetTheme
import org.junit.Rule
import org.junit.Test

class PoolSelectorTabsPanelTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tapping_tab_updates_selected_category_only_after_pager_settles() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            var selectedCategory by remember { mutableStateOf(SelectionCategory.Surahs) }
            val itemsByCategory = SelectionCategory.entries.associateWith { category ->
                listOf(sampleItem(category))
            }

            TathbeetTheme {
                Column {
                    Text(
                        text = selectedCategory.name,
                        modifier = Modifier.testTag("selected-category"),
                    )
                    PoolSelectorTabsPanel(
                        selectedCategory = selectedCategory,
                        optionsForCategory = { category -> itemsByCategory.getValue(category) },
                        selectedPool = emptyList(),
                        onCategorySelected = { selectedCategory = it },
                        onToggleSelection = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag("selected-category").assertTextEquals(SelectionCategory.Surahs.name)

        composeRule.onNodeWithTag("pool-selector-tab-Juz").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("selected-category").assertTextEquals(SelectionCategory.Surahs.name)

        composeRule.mainClock.advanceTimeBy(5_000)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("selected-category").assertTextEquals(SelectionCategory.Juz.name)
    }

    private fun sampleItem(category: SelectionCategory): QuranSelectionItem =
        QuranSelectionItem(
            key = selectionKey(category, 1),
            category = category,
            itemId = 1,
            order = 1,
            title = category.name,
            subtitle = category.name,
            segments = 1,
            firstRubId = 1,
            lastRubId = 1,
        )
}
