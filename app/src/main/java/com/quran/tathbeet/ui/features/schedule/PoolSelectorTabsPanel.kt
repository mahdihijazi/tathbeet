package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.SelectionCategory

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PoolSelectorTabsPanel(
    selectedCategory: SelectionCategory,
    optionsForCategory: (SelectionCategory) -> List<QuranSelectionItem>,
    selectedPool: List<QuranSelectionItem>,
    onCategorySelected: (SelectionCategory) -> Unit,
    onToggleSelection: (QuranSelectionItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = SelectionCategory.entries
    val selectedPage = categories.indexOf(selectedCategory).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = selectedPage,
        pageCount = { categories.size },
    )

    LaunchedEffect(selectedCategory) {
        val page = categories.indexOf(selectedCategory).coerceAtLeast(0)
        if (pagerState.currentPage != page) {
            pagerState.animateScrollToPage(page)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val category = categories[pagerState.currentPage]
        if (category != selectedCategory) {
            onCategorySelected(category)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { onCategorySelected(category) },
                    text = { Text(stringResource(category.labelRes)) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            val pageCategory = categories[page]
            val pageOptions = optionsForCategory(pageCategory)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("pool-selector-options-list-${pageCategory.name}"),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(pageOptions, key = { it.key }) { option ->
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
    }
}
