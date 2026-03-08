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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.components.WizardHeader
import com.quran.tathbeet.ui.theme.TathbeetTokens

@Composable
fun ScheduleIntroScreen(
    onNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(TathbeetTokens.spacing.x3),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TathbeetTokens.spacing.x4),
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x3),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WizardHeader(
                    currentStep = 1,
                    totalSteps = 3,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TathbeetTokens.spacing.x4),
                verticalArrangement = Arrangement.spacedBy(TathbeetTokens.spacing.x3),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(TathbeetTokens.spacing.x4 * 5),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f),
                    ) {}
                    Surface(
                        modifier = Modifier.size(TathbeetTokens.spacing.x4 * 4),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                        tonalElevation = TathbeetTokens.spacing.x1,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(R.drawable.tathbeet_logo),
                                contentDescription = null,
                                modifier = Modifier.size(TathbeetTokens.spacing.x4 * 3),
                            )
                        }
                    }
                }
                OnboardingMessageCard(
                    title = stringResource(R.string.schedule_intro_body),
                    body = stringResource(R.string.schedule_intro_supporting),
                    actionLabel = stringResource(R.string.action_next),
                    onActionClick = onNext,
                )
            }
        }
    }
}
