package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.TitledCardSection
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun SelectedPoolSummaryCard(
    title: String,
    selectionSummary: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    TitledCardSection(
        title = title,
        modifier = modifier.fillMaxWidth(),
        tone = AppCardTone.Muted,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x1Half),
        ) {
            Text(
                text = selectionSummary,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (actionLabel != null && onActionClick != null) {
                AppPrimaryButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(0.35f),
                    enabled = actionEnabled,
                )
            }
        }
    }
}
