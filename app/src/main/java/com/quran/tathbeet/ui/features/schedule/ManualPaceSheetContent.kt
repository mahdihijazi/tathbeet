package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppSecondaryButton
import com.quran.tathbeet.ui.model.PaceOption
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ManualPaceSheetContent(
    onPaceSelected: (PaceOption) -> Unit,
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
                onClick = { onPaceSelected(pace) },
            )
        }
    }
}
