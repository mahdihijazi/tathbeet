package com.quran.tathbeet.ui.features.schedule

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
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.WizardHeader
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
            SelectedPoolSummaryCard(
                title = stringResource(R.string.schedule_selected_pool_title),
                selectionSummary = selectedPoolLabel,
            )
        }

        if (paceMethod == PaceMethod.CycleTarget) {
            item {
                ScheduleCycleTargetSection(
                    selectedCycleTarget = selectedCycleTarget,
                    onCycleTargetSelected = onCycleTargetSelected,
                    onOpenManualSheet = { showManualPaceSheet = true },
                )
            }
        } else {
            item {
                ScheduleManualPaceSection(
                    onChangeManualPace = { showManualPaceSheet = true },
                    onResetToCycleMode = onResetToCycleMode,
                )
            }
        }

        item {
            ScheduleRotationPreviewCard(
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
            ManualPaceSheetContent(
                onPaceSelected = { pace ->
                    onPaceSelected(pace)
                    showManualPaceSheet = false
                },
            )
        }
    }
}
