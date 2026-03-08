package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.PrototypeScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.prototype.PaceOption
import com.quran.tathbeet.ui.prototype.QuranSelectionItem
import com.quran.tathbeet.ui.prototype.summarizeSelectionTitles

@Composable
fun ScheduleScreen(
    profileName: String,
    selectedPool: List<QuranSelectionItem>,
    selectedPace: PaceOption,
    segmentCount: Int,
    cycleLength: Int,
    onPaceSelected: (PaceOption) -> Unit,
    onSaveSchedule: () -> Unit,
) {
    val context = LocalContext.current
    val selectedPoolLabel = summarizeSelectionTitles(
        context = context,
        items = selectedPool,
        emptyResId = R.string.schedule_pool_empty,
    )

    PrototypeScreenLayout(
        title = stringResource(R.string.schedule_dose_title),
        subtitle = stringResource(R.string.schedule_dose_subtitle, profileName),
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
                title = stringResource(R.string.schedule_pace_title),
                subtitle = stringResource(R.string.schedule_pace_subtitle),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PaceOption.entries.forEach { pace ->
                    FilterChip(
                        selected = pace == selectedPace,
                        onClick = { onPaceSelected(pace) },
                        label = { Text(stringResource(pace.labelRes)) },
                    )
                }
            }
        }

        item {
            RotationPreviewCard(
                segmentCount = segmentCount,
                cycleLength = cycleLength,
            )
        }

        item {
            Button(
                onClick = onSaveSchedule,
                enabled = selectedPool.isNotEmpty(),
            ) {
                Text(stringResource(R.string.schedule_save))
            }
        }
    }
}

@Composable
private fun RotationPreviewCard(
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
            Text(
                text = stringResource(R.string.schedule_preview_cycle_length, cycleLength),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
