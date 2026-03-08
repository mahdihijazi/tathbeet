package com.quran.tathbeet.ui.features.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.HeroCard
import com.quran.tathbeet.ui.components.MetricCard
import com.quran.tathbeet.ui.components.PrototypeScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.prototype.PrototypeProfile
import com.quran.tathbeet.ui.prototype.asString

@Composable
fun ProgressScreen(
    profile: PrototypeProfile,
    completionRate: Int,
    onOpenReview: () -> Unit,
) {
    val insights = listOf(
        stringResource(R.string.progress_insight_1),
        stringResource(R.string.progress_insight_2),
        stringResource(R.string.progress_insight_3),
    )

    PrototypeScreenLayout(
        title = stringResource(R.string.progress_title),
        subtitle = stringResource(R.string.progress_subtitle),
    ) {
        item {
            HeroCard(
                eyebrow = stringResource(R.string.progress_eyebrow),
                title = stringResource(R.string.progress_rate_title, completionRate),
                body = stringResource(R.string.progress_rate_body, profile.name.asString()),
            ) {
                Button(onClick = onOpenReview) {
                    Text(stringResource(R.string.progress_back_to_review))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = stringResource(R.string.progress_week_title),
                        value = stringResource(
                            R.string.progress_week_value,
                            profile.reviewTasks.count { it.isDone },
                            profile.reviewTasks.size,
                        ),
                        supporting = stringResource(R.string.progress_week_supporting),
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = stringResource(R.string.progress_shared_title),
                        value = if (profile.isShared) {
                            stringResource(R.string.progress_shared_enabled)
                        } else {
                            stringResource(R.string.progress_shared_disabled)
                        },
                        supporting = stringResource(R.string.progress_shared_supporting),
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.progress_weekly_rhythm_title),
                subtitle = stringResource(R.string.progress_weekly_rhythm_subtitle),
            )
        }

        item {
            WeeklyBars(profile.weekCompletion)
        }

        item {
            SectionHeader(
                title = stringResource(R.string.progress_quick_read_title),
                subtitle = stringResource(R.string.progress_quick_read_subtitle),
            )
        }

        items(insights) { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = insight,
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun WeeklyBars(values: List<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEach { value ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height((28 + (110 * value)).dp)
                            .padding(top = 2.dp),
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                        ) {}
                    }
                    Text(
                        text = stringResource(R.string.percentage_value, (value * 100).toInt()),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
