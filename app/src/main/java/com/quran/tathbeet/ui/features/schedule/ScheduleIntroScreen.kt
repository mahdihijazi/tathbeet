package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.AppCardTone
import com.quran.tathbeet.ui.components.AppPrimaryButton
import com.quran.tathbeet.ui.components.CardSection
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ScheduleIntroScreen(
    onNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(TathbeetTokens.spacing.x3),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x3),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(TathbeetTokens.spacing.x4 * 4),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f),
                ) {}
                Surface(
                    modifier = Modifier.size(TathbeetTokens.spacing.x4 * 3),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                    tonalElevation = TathbeetTokens.spacing.x1,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.tathbeet_logo),
                            contentDescription = null,
                            modifier = Modifier.size(TathbeetTokens.spacing.x4 * 2),
                        )
                    }
                }
            }
            CardSection(
                modifier = Modifier.fillMaxWidth(),
                tone = AppCardTone.Muted,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x2),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.schedule_intro_body),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.schedule_intro_supporting),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    AppPrimaryButton(
                        text = stringResource(R.string.action_next),
                        onClick = onNext,
                    )
                }
            }
        }
    }
}
