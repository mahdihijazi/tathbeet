package com.quran.tathbeet.ui.features.debug

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppCard
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.AppKeyValueRow
import com.quran.tathbeet.ui.components.AppPill
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.AppSelectionChip
import com.quran.tathbeet.ui.components.AppTabbedPager
import com.quran.tathbeet.ui.components.AppTabbedPagerStyle
import com.quran.tathbeet.ui.components.AppTabbedPagerTab
import com.quran.tathbeet.ui.components.BodyTextCard
import com.quran.tathbeet.ui.components.CardSection
import com.quran.tathbeet.ui.components.HeroCard
import com.quran.tathbeet.ui.components.InfoActionCard
import com.quran.tathbeet.ui.components.MetricCard
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.components.TitledCardSection
import com.quran.tathbeet.ui.components.WizardHeader
import com.quran.tathbeet.ui.model.SelectionCategory
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun UiCatalogDebugScreen() {
    var chipSelected by remember { mutableStateOf(true) }
    var fixedPagerTab by remember { mutableStateOf(CatalogFixedTab.Today) }
    var scrollablePagerTab by remember { mutableStateOf(SelectionCategory.Juz) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScreenLayout(
                title = stringResource(R.string.debug_ui_catalog_title),
                subtitle = stringResource(R.string.debug_ui_catalog_subtitle),
            ) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.debug_ui_catalog_title),
                        subtitle = stringResource(R.string.debug_ui_catalog_subtitle),
                    )
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_screen_layout_title),
                    ) {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.debug_ui_catalog_screen_layout_body),
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = stringResource(R.string.debug_ui_catalog_section_header_title),
                        subtitle = stringResource(R.string.debug_ui_catalog_section_header_subtitle),
                    )
                }

                item {
                    HeroCard(
                        eyebrow = stringResource(R.string.debug_ui_catalog_hero_eyebrow),
                        title = stringResource(R.string.debug_ui_catalog_hero_title),
                        body = stringResource(R.string.debug_ui_catalog_hero_body),
                    ) {
                        AppPill(text = stringResource(R.string.debug_ui_catalog_pill_text))
                    }
                }

                item {
                    MetricCard(
                        title = stringResource(R.string.debug_ui_catalog_metric_title),
                        value = stringResource(R.string.debug_ui_catalog_metric_value),
                        supporting = stringResource(R.string.debug_ui_catalog_metric_supporting),
                    )
                }

                item {
                    AppCard(
                        tone = AppCardTone.Accent,
                    ) {
                        Column(
                            modifier = Modifier.padding(TathbeetTokens.spacing.x2Half),
                            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
                        ) {
                            androidx.compose.material3.Text(
                                text = stringResource(R.string.debug_ui_catalog_app_card_title),
                            )
                            androidx.compose.material3.Text(
                                text = stringResource(R.string.debug_ui_catalog_app_card_body),
                            )
                        }
                    }
                }

                item {
                    CardSection {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.debug_ui_catalog_card_section_title),
                        )
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.debug_ui_catalog_card_section_body),
                        )
                    }
                }

                item {
                    InfoActionCard(
                        title = stringResource(R.string.debug_ui_catalog_info_action_title),
                    ) {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.debug_ui_catalog_info_action_body),
                        )
                        AppKeyValueRow(
                            label = stringResource(R.string.debug_ui_catalog_key_value_label),
                            value = stringResource(R.string.debug_ui_catalog_key_value_value),
                        )
                    }
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_titled_section_title),
                        tone = AppCardTone.Muted,
                    ) {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.debug_ui_catalog_titled_section_body),
                        )
                    }
                }

                item {
                    BodyTextCard(
                        text = stringResource(R.string.debug_ui_catalog_body_text),
                    )
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_pill_title),
                    ) {
                        AppPill(text = stringResource(R.string.debug_ui_catalog_pill_text))
                    }
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_wizard_title),
                    ) {
                        WizardHeader(
                            currentStep = 2,
                            totalSteps = 4,
                        )
                    }
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_tabbed_pager_title),
                    ) {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.debug_ui_catalog_tabbed_pager_body),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_tabbed_pager_fixed_title),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                        ) {
                            AppTabbedPager(
                                tabs = CatalogFixedTab.entries.map { tab ->
                                    AppTabbedPagerTab(
                                        value = tab,
                                        label = stringResource(tab.labelRes),
                                    )
                                },
                                selectedTab = fixedPagerTab,
                                onTabSelected = { fixedPagerTab = it },
                                rowStyle = AppTabbedPagerStyle.Fixed,
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                tabsModifier = Modifier.padding(horizontal = TathbeetTokens.spacing.x1),
                            ) { tab ->
                                BodyTextCard(
                                    text = stringResource(tab.bodyRes),
                                )
                            }
                        }
                    }
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_tabbed_pager_scrollable_title),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                        ) {
                            AppTabbedPager(
                                tabs = SelectionCategory.entries.map { category ->
                                    AppTabbedPagerTab(
                                        value = category,
                                        label = stringResource(category.labelRes),
                                    )
                                },
                                selectedTab = scrollablePagerTab,
                                onTabSelected = { scrollablePagerTab = it },
                                rowStyle = AppTabbedPagerStyle.Scrollable(edgePadding = 0.dp),
                            ) { category ->
                                BodyTextCard(
                                    text = stringResource(
                                        R.string.debug_ui_catalog_tabbed_pager_page_selection,
                                        stringResource(category.labelRes),
                                    ),
                                )
                            }
                        }
                    }
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_buttons_title),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
                        ) {
                            AppPrimaryButton(
                                text = stringResource(R.string.debug_ui_catalog_primary_button),
                                onClick = {},
                            )
                            AppSecondaryButton(
                                text = stringResource(R.string.debug_ui_catalog_secondary_button),
                                onClick = {},
                            )
                        }
                    }
                }

                item {
                    TitledCardSection(
                        title = stringResource(R.string.debug_ui_catalog_selection_title),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
                        ) {
                            AppSelectionChip(
                                text = stringResource(R.string.debug_ui_catalog_selection_primary),
                                selected = chipSelected,
                                onClick = { chipSelected = true },
                            )
                            AppSelectionChip(
                                text = stringResource(R.string.debug_ui_catalog_selection_secondary),
                                selected = !chipSelected,
                                onClick = { chipSelected = false },
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class CatalogFixedTab(
    val labelRes: Int,
    val bodyRes: Int,
) {
    Today(
        labelRes = R.string.review_tab_today,
        bodyRes = R.string.debug_ui_catalog_tabbed_pager_page_today,
    ),
    FullPlan(
        labelRes = R.string.review_tab_full_plan,
        bodyRes = R.string.debug_ui_catalog_tabbed_pager_page_full_plan,
    ),
}
