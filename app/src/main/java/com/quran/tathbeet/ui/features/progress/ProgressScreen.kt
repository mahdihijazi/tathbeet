package com.quran.tathbeet.ui.features.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.app.ReminderHadithCatalog
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.CardSection
import com.quran.tathbeet.ui.components.MetricCard
import com.quran.tathbeet.ui.components.ScreenLayout
import com.quran.tathbeet.ui.components.SectionHeader
import com.quran.tathbeet.ui.components.TitledCardSection
import com.quran.tathbeet.ui.model.AppProfile

@Composable
fun ProgressScreen(
    profile: AppProfile,
    completionRate: Int,
    onOpenReview: () -> Unit,
) {
    val summary = profile.toProgressSummary(completionRate)
    val hadith = ReminderHadithCatalog.cardEntryFor(profile.id.hashCode())

    ScreenLayout(
        title = stringResource(R.string.progress_title),
        subtitle = stringResource(R.string.progress_subtitle),
    ) {
        item {
            SectionHeader(title = stringResource(R.string.progress_today_title))
        }

        item {
            TodaySummaryCard(
                summary = summary,
                onOpenReview = onOpenReview,
            )
        }

        item {
            SectionHeader(
                title = stringResource(R.string.progress_week_title),
                subtitle = stringResource(R.string.progress_week_subtitle),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = stringResource(R.string.progress_week_rate_title),
                        value = stringResource(R.string.percentage_value, summary.completionRate),
                        supporting = stringResource(R.string.progress_week_rate_supporting),
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        title = stringResource(R.string.progress_week_days_title),
                        value = stringResource(
                            R.string.progress_week_days_value,
                            summary.completedDays,
                            summary.weekValues.size,
                        ),
                        supporting = stringResource(R.string.progress_week_days_supporting),
                    )
                }
            }
        }

        item {
            WeeklyRhythmCard(values = summary.weekValues)
        }

        item {
            SectionHeader(
                title = stringResource(R.string.progress_motivation_title),
                subtitle = stringResource(R.string.progress_motivation_subtitle),
            )
        }

        item {
            MotivationCard(
                text = stringResource(hadith.textResId),
                source = stringResource(R.string.progress_motivation_source, stringResource(hadith.sourceResId)),
            )
        }
    }
}

@Composable
private fun TodaySummaryCard(
    summary: ProgressSummary,
    onOpenReview: () -> Unit,
) {
    CardSection(
        tone = AppCardTone.Highlight,
    ) {
        if (summary.todayTotal == 0) {
            Text(
                text = stringResource(R.string.progress_today_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.progress_today_empty_supporting),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = stringResource(
                    R.string.progress_today_ratio,
                    summary.todayCompleted,
                    summary.todayTotal,
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (summary.remainingCount == 0) {
                    stringResource(R.string.progress_today_done)
                } else {
                    stringResource(R.string.progress_today_remaining, summary.remainingCount)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (summary.hasRollover) {
                Text(
                    text = stringResource(R.string.progress_today_supporting_rollover),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AppPrimaryButton(
            text = stringResource(R.string.progress_back_to_review),
            onClick = onOpenReview,
        )
    }
}

@Composable
private fun WeeklyRhythmCard(values: List<Float>) {
    TitledCardSection(
        title = stringResource(R.string.progress_weekly_rhythm_title),
    ) {
        Text(
            text = stringResource(R.string.progress_weekly_rhythm_supporting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            values.forEachIndexed { index, value ->
                WeeklyDayBar(
                    label = stringResource(weeklyDayLabels[index]),
                    value = value,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeeklyDayBar(
    label: String,
    value: Float,
    modifier: Modifier = Modifier,
) {
    val clampedValue = value.coerceIn(0f, 1f)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.height(92.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(92.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height((92 * clampedValue).dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MotivationCard(
    text: String,
    source: String,
) {
    CardSection(
        tone = AppCardTone.Accent,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = source,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

private data class ProgressSummary(
    val todayCompleted: Int,
    val todayTotal: Int,
    val remainingCount: Int,
    val completionRate: Int,
    val completedDays: Int,
    val weekValues: List<Float>,
    val hasRollover: Boolean,
)

private fun AppProfile.toProgressSummary(completionRate: Int): ProgressSummary {
    val weekValues = weekCompletion.normalizedWeekValues()
    val todayCompleted = reviewTasks.count { it.isDone }
    val todayTotal = reviewTasks.size
    return ProgressSummary(
        todayCompleted = todayCompleted,
        todayTotal = todayTotal,
        remainingCount = (todayTotal - todayCompleted).coerceAtLeast(0),
        completionRate = completionRate,
        completedDays = weekValues.count { it >= 1f },
        weekValues = weekValues,
        hasRollover = reviewTasks.any { it.isRollover },
    )
}

private fun List<Float>.normalizedWeekValues(): List<Float> {
    val clippedValues = takeLast(7).map { it.coerceIn(0f, 1f) }
    return if (clippedValues.size == 7) {
        clippedValues
    } else {
        List(7 - clippedValues.size) { 0f } + clippedValues
    }
}

private val weeklyDayLabels = listOf(
    R.string.progress_day_sat,
    R.string.progress_day_sun,
    R.string.progress_day_mon,
    R.string.progress_day_tue,
    R.string.progress_day_wed,
    R.string.progress_day_thu,
    R.string.progress_day_fri,
)
