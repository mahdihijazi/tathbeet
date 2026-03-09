package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.CardSection
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun SelectedPoolSummaryCard(
    title: String,
    selectionSummary: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CardSection(
        modifier = modifier.fillMaxWidth(),
        tone = AppCardTone.Muted,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = selectionSummary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(0.35f),
                shape = RoundedCornerShape(TathbeetTokens.radii.pill),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
