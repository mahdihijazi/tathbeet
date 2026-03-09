package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.WizardHeader
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.SelectionCategory
import com.quran.tathbeet.ui.model.summarizeSelectionTitles

@Composable
fun PoolSelectorScreen(
    selectedCategory: SelectionCategory,
    optionsForCategory: (SelectionCategory) -> List<QuranSelectionItem>,
    selectedPool: List<QuranSelectionItem>,
    showWizardHeader: Boolean,
    onCategorySelected: (SelectionCategory) -> Unit,
    onToggleSelection: (QuranSelectionItem) -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val selectedPoolLabel = summarizeSelectionTitles(
        context = context,
        items = selectedPool,
        emptyResId = R.string.pool_selector_empty,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showWizardHeader) {
            WizardHeader(
                currentStep = 2,
                totalSteps = 3,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.pool_selector_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SelectedPoolSummaryCard(
            title = stringResource(R.string.pool_selector_current_selection),
            selectionSummary = selectedPoolLabel,
            actionLabel = stringResource(R.string.action_next),
            onActionClick = onDone,
        )

        PoolSelectorTabsPanel(
            selectedCategory = selectedCategory,
            optionsForCategory = optionsForCategory,
            selectedPool = selectedPool,
            onCategorySelected = onCategorySelected,
            onToggleSelection = onToggleSelection,
            modifier = Modifier.weight(1f),
        )
    }
}
