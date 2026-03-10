package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ScheduleManualPaceSection(
    onChangeManualPace: () -> Unit,
    onResetToCycleMode: () -> Unit,
) {
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
            onClick = onChangeManualPace,
        )
        AppSecondaryButton(
            text = stringResource(R.string.schedule_back_to_cycle_mode),
            onClick = onResetToCycleMode,
        )
    }
}
