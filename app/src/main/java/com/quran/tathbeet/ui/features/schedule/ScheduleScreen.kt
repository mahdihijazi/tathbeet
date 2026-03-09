package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.AppKeyValueRow
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.AppSelectionChip
import com.quran.tathbeet.ui.components.CardSection
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.components.WizardHeader
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.PaceMethod
import com.quran.tathbeet.ui.model.PaceOption
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.summarizeSelectionTitles
import com.quran.tathbeet.ui.theme.TathbeetTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    selectedPool: List<QuranSelectionItem>,
    paceMethod: PaceMethod,
    selectedCycleTarget: CycleTarget,
    selectedPace: PaceOption,
    segmentCount: Int,
    cycleLength: Int,
    showWizardHeader: Boolean,
    onCycleTargetSelected: (CycleTarget) -> Unit,
    onPaceSelected: (PaceOption) -> Unit,
    onResetToCycleMode: () -> Unit,
    onSaveSchedule: () -> Unit,
) {
    val context = LocalContext.current
    val selectedPoolLabel = summarizeSelectionTitles(
        context = context,
        items = selectedPool,
        emptyResId = R.string.schedule_pool_empty,
    )
    var showManualPaceSheet by remember { mutableStateOf(false) }

    ScreenLayout(
        title = stringResource(R.string.schedule_dose_title),
        subtitle = if (showWizardHeader) "" else stringResource(R.string.schedule_dose_subtitle),
    ) {
        if (showWizardHeader) {
            item {
                WizardHeader(
                    currentStep = 3,
                    totalSteps = 3,
                )
            }

            item {
                Text(
                    text = stringResource(R.string.schedule_dose_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.schedule_selected_pool_title),
                subtitle = stringResource(R.string.schedule_selected_pool_subtitle),
            )
        }

        item {
            CardSection(tone = AppCardTone.Highlight) {
                Text(
                    text = selectedPoolLabel,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.schedule_cycle_target_title),
                subtitle = stringResource(R.string.schedule_cycle_target_subtitle),
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
            ) {
                CycleTarget.entries.forEach { target ->
                    AppSelectionChip(
                        selected = paceMethod == PaceMethod.CycleTarget && target == selectedCycleTarget,
                        onClick = { onCycleTargetSelected(target) },
                        text = stringResource(target.labelRes),
                    )
                }
            }
        }

        item {
            if (paceMethod == PaceMethod.CycleTarget) {
                AppSecondaryButton(
                    text = stringResource(R.string.schedule_manual_sheet_open),
                    onClick = { showManualPaceSheet = true },
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.schedule_manual_mode_title),
                        subtitle = stringResource(R.string.schedule_manual_mode_subtitle),
                    )
                    AppSecondaryButton(
                        text = stringResource(R.string.schedule_manual_sheet_change),
                        onClick = { showManualPaceSheet = true },
                    )
                    AppSecondaryButton(
                        text = stringResource(R.string.schedule_back_to_cycle_mode),
                        onClick = onResetToCycleMode,
                    )
                }
            }
        }

        item {
            RotationPreviewCard(
                paceMethod = paceMethod,
                selectedCycleTarget = selectedCycleTarget,
                selectedPace = selectedPace,
                segmentCount = segmentCount,
                cycleLength = cycleLength,
            )
        }

        item {
            AppPrimaryButton(
                text = stringResource(R.string.schedule_save),
                onClick = onSaveSchedule,
                enabled = selectedPool.isNotEmpty(),
            )
        }
    }

    if (showManualPaceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManualPaceSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TathbeetTokens.spacing.x2Half,
                        vertical = TathbeetTokens.spacing.x1,
                    ),
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
            ) {
                Text(
                    text = stringResource(R.string.schedule_manual_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.schedule_manual_sheet_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PaceOption.entries.forEach { pace ->
                    AppSecondaryButton(
                        text = stringResource(pace.labelRes),
                        onClick = {
                            onPaceSelected(pace)
                            showManualPaceSheet = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RotationPreviewCard(
    paceMethod: PaceMethod,
    selectedCycleTarget: CycleTarget,
    selectedPace: PaceOption,
    segmentCount: Int,
    cycleLength: Int,
) {
    CardSection(tone = AppCardTone.Muted) {
        Text(
            text = stringResource(R.string.schedule_preview_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        AppKeyValueRow(
            label = stringResource(R.string.schedule_preview_pool_size, segmentCount),
            value = segmentCount.toString(),
        )
        AppKeyValueRow(
            label = if (paceMethod == PaceMethod.CycleTarget) {
                stringResource(R.string.schedule_preview_target_cycle, stringResource(selectedCycleTarget.labelRes))
            } else {
                stringResource(R.string.schedule_preview_manual_mode)
            },
            value = stringResource(selectedPace.labelRes),
        )
        AppKeyValueRow(
            label = stringResource(R.string.schedule_preview_cycle_length, cycleLength),
            value = cycleLength.toString(),
        )
    }
}
