package com.quran.tathbeet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.quran.tathbeet.R
import com.quran.tathbeet.ui.model.AccountMode
import com.quran.tathbeet.ui.model.AppDestination
import com.quran.tathbeet.ui.model.AppUiState

@Composable
fun AppShell(
    uiState: AppUiState,
    onNavigate: (AppDestination) -> Unit,
    onBack: () -> Unit,
    onReviewPlanAction: () -> Unit,
    onAccountAction: () -> Unit,
    snackbarHost: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    TathbeetBackdrop {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.82f),
            topBar = {
                AppTopBar(
                    uiState = uiState,
                    onBack = onBack,
                    onReviewPlanAction = onReviewPlanAction,
                    onAccountAction = onAccountAction,
                )
            },
            bottomBar = {
                if (uiState.destination in mainDestinations) {
                    AppBottomBar(
                        currentDestination = uiState.destination,
                        onNavigate = onNavigate,
                    )
                }
            },
            snackbarHost = snackbarHost,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                content()
            }
        }
    }
}

private val mainDestinations = listOf(
    AppDestination.Profiles,
    AppDestination.Review,
    AppDestination.Progress,
    AppDestination.Settings,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    uiState: AppUiState,
    onBack: () -> Unit,
    onReviewPlanAction: () -> Unit,
    onAccountAction: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(uiState.destination.titleRes),
            )
        },
        navigationIcon = {
            if (uiState.destination !in mainDestinations && uiState.destination != AppDestination.ScheduleIntro) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.content_back),
                    )
                }
            }
        },
        actions = {
            if (uiState.destination == AppDestination.Review) {
                IconButton(onClick = onReviewPlanAction) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.content_edit_plan),
                    )
                }
            }
            IconButton(onClick = onAccountAction) {
                Icon(
                    imageVector = if (uiState.accountMode == AccountMode.Guest) {
                        Icons.Outlined.PersonAddAlt1
                    } else {
                        Icons.Outlined.CloudDone
                    },
                    contentDescription = if (uiState.accountMode == AccountMode.Guest) {
                        stringResource(R.string.content_create_account)
                    } else {
                        stringResource(R.string.content_account_active)
                    },
                )
            }
        },
    )
}

@Composable
private fun AppBottomBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
) {
    NavigationBar {
        listOf(
            AppDestination.Profiles to Icons.Outlined.Groups,
            AppDestination.Review to Icons.AutoMirrored.Outlined.MenuBook,
            AppDestination.Progress to Icons.Outlined.BarChart,
            AppDestination.Settings to Icons.Outlined.Settings,
        ).forEach { (destination, icon) ->
            NavigationBarItem(
                selected = destination == currentDestination,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(destination.titleRes),
                    )
                },
                label = {
                    Text(text = stringResource(destination.subtitleRes))
                },
            )
        }
    }
}

@Composable
fun TathbeetBackdrop(
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-12).dp)
                .size(180.dp)
                .background(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-80).dp, y = 120.dp)
                .size(220.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    shape = CircleShape,
                ),
        )
        content()
    }
}
