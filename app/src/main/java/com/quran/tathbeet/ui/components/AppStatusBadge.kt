package com.quran.tathbeet.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.quran.tathbeet.ui.theme.TathbeetTokens

enum class AppStatusBadgeTone {
    Neutral,
    Highlight,
    Accent,
}

@Composable
fun AppStatusBadge(
    text: String,
    tone: AppStatusBadgeTone,
    modifier: Modifier = Modifier,
) {
    val colors = when (tone) {
        AppStatusBadgeTone.Neutral -> BadgeColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppStatusBadgeTone.Highlight -> BadgeColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        AppStatusBadgeTone.Accent -> BadgeColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TathbeetTokens.radii.pill),
        color = colors.containerColor,
        contentColor = colors.contentColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = TathbeetTokens.spacing.x1Half,
                vertical = TathbeetTokens.spacing.half,
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private data class BadgeColors(
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
)
