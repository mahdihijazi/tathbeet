package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.PaceMethod
import com.quran.tathbeet.ui.model.PaceOption
import com.quran.tathbeet.ui.model.QuranSelectionItem
import com.quran.tathbeet.ui.model.summarizeSelectionTitles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    selectedPool: List<QuranSelectionItem>,
    paceMethod: PaceMethod,
    selectedCycleTarget: CycleTarget,
    selectedPace: PaceOption,
    segmentCount: Int,
    cycleLength: Int,
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
        subtitle = stringResource(R.string.schedule_dose_subtitle),
    ) {
        item {
            SectionHeader(
                title = stringResource(R.string.schedule_selected_pool_title),
                subtitle = stringResource(R.string.schedule_selected_pool_subtitle),
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = selectedPoolLabel,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CycleTarget.entries.forEach { target ->
                    FilterChip(
                        selected = paceMethod == PaceMethod.CycleTarget && target == selectedCycleTarget,
                        onClick = { onCycleTargetSelected(target) },
                        label = { Text(stringResource(target.labelRes)) },
                    )
                }
            }
        }

        item {
            if (paceMethod == PaceMethod.CycleTarget) {
                Button(
                    onClick = { showManualPaceSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.schedule_manual_sheet_open))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.schedule_manual_mode_title),
                        subtitle = stringResource(R.string.schedule_manual_mode_subtitle),
                    )
                    OutlinedButton(
                        onClick = { showManualPaceSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.schedule_manual_sheet_change))
                    }
                    OutlinedButton(
                        onClick = onResetToCycleMode,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.schedule_back_to_cycle_mode))
                    }
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
            Button(
                onClick = onSaveSchedule,
                enabled = selectedPool.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.schedule_save))
            }
        }
    }

    if (showManualPaceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManualPaceSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    OutlinedButton(
                        onClick = {
                            onPaceSelected(pace)
                            showManualPaceSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(pace.labelRes))
                    }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.schedule_preview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.schedule_preview_pool_size, segmentCount),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (paceMethod == PaceMethod.CycleTarget) {
                Text(
                    text = stringResource(R.string.schedule_preview_target_cycle, stringResource(selectedCycleTarget.labelRes)),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    text = stringResource(R.string.schedule_preview_manual_mode),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Text(
                text = stringResource(R.string.schedule_preview_daily_equivalent, stringResource(selectedPace.labelRes)),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.schedule_preview_cycle_length, cycleLength),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
