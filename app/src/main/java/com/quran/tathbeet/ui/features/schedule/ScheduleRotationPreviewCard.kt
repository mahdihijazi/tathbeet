package com.quran.tathbeet.ui.features.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.AppKeyValueRow
import com.quran.tathbeet.ui.components.TitledCardSection
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.PaceMethod
import com.quran.tathbeet.ui.model.PaceOption

@Composable
fun ScheduleRotationPreviewCard(
    paceMethod: PaceMethod,
    selectedCycleTarget: CycleTarget,
    selectedPace: PaceOption,
    segmentCount: Int,
    cycleLength: Int,
) {
    TitledCardSection(
        title = stringResource(R.string.schedule_preview_title),
        tone = AppCardTone.Muted,
    ) {
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
