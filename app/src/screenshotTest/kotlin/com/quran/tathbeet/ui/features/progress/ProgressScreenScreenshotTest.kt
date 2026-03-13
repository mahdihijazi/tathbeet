package com.quran.tathbeet.ui.features.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.components.TathbeetBackdrop
import com.quran.tathbeet.ui.model.AppProfile
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.Guardian
import com.quran.tathbeet.ui.model.PaceMethod
import com.quran.tathbeet.ui.model.PaceOption
import com.quran.tathbeet.ui.model.ReviewTask
import com.quran.tathbeet.ui.model.TextSpec
import com.quran.tathbeet.ui.model.completionRate
import com.quran.tathbeet.ui.theme.TathbeetTheme

private const val ProgressPreviewWidth = 411
private const val ProgressPreviewHeight = 1500

@PreviewTest
@Preview(
    name = "progress_screen_empty",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenEmptyScreenshot() {
    ProgressScreenshotBox {
        val profile = ProgressPreviewFactory.emptyState()
        ProgressScreen(
            profile = profile,
            completionRate = profile.completionRate,
            onOpenReview = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "progress_screen_partial",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenPartialScreenshot() {
    ProgressScreenshotBox {
        val profile = ProgressPreviewFactory.partialState()
        ProgressScreen(
            profile = profile,
            completionRate = profile.completionRate,
            onOpenReview = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "progress_screen_complete",
    locale = "ar",
    widthDp = ProgressPreviewWidth,
    heightDp = ProgressPreviewHeight,
    showBackground = true,
)
@Composable
fun ProgressScreenCompleteScreenshot() {
    ProgressScreenshotBox {
        val profile = ProgressPreviewFactory.completeState()
        ProgressScreen(
            profile = profile,
            completionRate = profile.completionRate,
            onOpenReview = {},
        )
    }
}

@Composable
private fun ProgressScreenshotBox(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme {
            TathbeetBackdrop {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }
}

private object ProgressPreviewFactory {
    fun emptyState(): AppProfile = profile(
        id = "progress-empty",
        weekCompletion = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
        reviewTasks = listOf(
            reviewTask(id = "1", isDone = false),
            reviewTask(id = "2", isDone = false),
            reviewTask(id = "3", isDone = false),
            reviewTask(id = "4", isDone = false),
        ),
    )

    fun partialState(): AppProfile = profile(
        id = "progress-partial",
        weekCompletion = listOf(0.4f, 0.65f, 0.8f, 1f, 1f, 0.5f, 0.6f),
        reviewTasks = listOf(
            reviewTask(id = "1", isDone = true, isRollover = true),
            reviewTask(id = "2", isDone = true),
            reviewTask(id = "3", isDone = false),
            reviewTask(id = "4", isDone = false),
            reviewTask(id = "5", isDone = false),
        ),
    )

    fun completeState(): AppProfile = profile(
        id = "progress-complete",
        weekCompletion = listOf(1f, 0.8f, 1f, 1f, 0.9f, 1f, 1f),
        reviewTasks = listOf(
            reviewTask(id = "1", isDone = true),
            reviewTask(id = "2", isDone = true),
            reviewTask(id = "3", isDone = true),
        ),
    )

    private fun profile(
        id: String,
        weekCompletion: List<Float>,
        reviewTasks: List<ReviewTask>,
    ): AppProfile =
        AppProfile(
            id = id,
            name = TextSpec(rawText = "مريم"),
            isSelfProfile = false,
            isShared = false,
            guardians = setOf(Guardian.Mother),
            notificationsEnabled = true,
            paceMethod = PaceMethod.CycleTarget,
            cycleTarget = CycleTarget.OneMonth,
            pace = PaceOption.OneRub,
            selectedPoolKeys = emptySet(),
            reviewTasks = reviewTasks,
            weekCompletion = weekCompletion,
            activityFeed = emptyList(),
        )

    private fun reviewTask(
        id: String,
        isDone: Boolean,
        isRollover: Boolean = false,
    ): ReviewTask =
        ReviewTask(
            id = id,
            title = TextSpec(rawText = "مقطع $id"),
            detail = TextSpec(rawText = "تفصيل قصير للمراجعة"),
            isDone = isDone,
            isRollover = isRollover,
        )
}
