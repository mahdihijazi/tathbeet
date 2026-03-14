package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.SelectionCategory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = selectedPage,
        pageCount = { categories.size },
    )

    LaunchedEffect(selectedCategory) {
        val page = categories.indexOf(selectedCategory).coerceAtLeast(0)
        if (pagerState.settledPage != page && pagerState.targetPage != page) {
            pagerState.scrollToPage(page)
        }
    }

    LaunchedEffect(pagerState, selectedCategory) {
        snapshotFlow { pagerState.settledPage }
            .filter { page -> page in categories.indices }
            .distinctUntilChanged()
            .collectLatest { page ->
                val category = categories[page]
                if (category != selectedCategory) {
                    onCategorySelected(category)
                }
            }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth(),
            indicator = { PoolSelectorTabIndicator(pagerState = pagerState) },
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        if (pagerState.targetPage != index || pagerState.settledPage != index) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    },
                    modifier = Modifier.testTag("pool-selector-tab-${category.name}"),
                    text = { Text(stringResource(category.labelRes)) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .testTag("pool-selector-pager"),
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
                    Card(
                        onClick = { onToggleSelection(option) },
                        modifier = Modifier.testTag("pool-selector-option-${option.key}"),
                    ) {
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
                                modifier = Modifier.testTag("pool-selector-checkbox-${option.key}"),
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

@Composable
private fun TabIndicatorScope.PoolSelectorTabIndicator(
    pagerState: PagerState,
) {
    TabRowDefaults.PrimaryIndicator(
        modifier = Modifier
            .tabIndicatorOffset(
                selectedTabIndex = pagerState.currentPage,
                matchContentSize = true,
            )
            .height(3.dp),
        width = Dp.Unspecified,
        color = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
    )
}
