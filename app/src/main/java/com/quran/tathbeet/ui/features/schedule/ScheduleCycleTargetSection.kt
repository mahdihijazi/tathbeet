package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.AppSelectionChip
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ScheduleCycleTargetSection(
    selectedCycleTarget: CycleTarget,
    onCycleTargetSelected: (CycleTarget) -> Unit,
    onOpenManualSheet: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
    ) {
        SectionHeader(
            title = stringResource(R.string.schedule_cycle_target_title),
            subtitle = stringResource(R.string.schedule_cycle_target_subtitle),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1),
        ) {
            CycleTarget.entries.forEach { target ->
                AppSelectionChip(
                    selected = target == selectedCycleTarget,
                    onClick = { onCycleTargetSelected(target) },
                    text = stringResource(target.labelRes),
                )
            }
        }

        AppSecondaryButton(
            text = stringResource(R.string.schedule_manual_sheet_open),
            onClick = onOpenManualSheet,
        )
    }
}
