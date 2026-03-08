package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.PrototypeScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.prototype.QuranSelectionItem
import com.quran.tathbeet.ui.prototype.SelectionCategory
import com.quran.tathbeet.ui.prototype.summarizeSelectionTitles

@Composable
fun PoolSelectorScreen(
    selectedCategory: SelectionCategory,
    visibleOptions: List<QuranSelectionItem>,
    selectedPool: List<QuranSelectionItem>,
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

    PrototypeScreenLayout(
        title = stringResource(R.string.pool_selector_title),
        subtitle = stringResource(R.string.pool_selector_subtitle),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.pool_selector_current_selection),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = selectedPoolLabel,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(onClick = onDone) {
                        Text(stringResource(R.string.action_next))
                    }
                }
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SelectionCategory.entries.forEach { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategorySelected(category) },
                        label = { Text(stringResource(category.labelRes)) },
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(selectedCategory.labelRes),
                subtitle = stringResource(R.string.pool_selector_section_subtitle),
            )
        }

        items(visibleOptions, key = { it.key }) { option ->
            val isSelected = selectedPool.any { item -> item.key == option.key }
            Card(onClick = { onToggleSelection(option) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = option.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection(option) },
                    )
                }
            }
        }

    }
}
