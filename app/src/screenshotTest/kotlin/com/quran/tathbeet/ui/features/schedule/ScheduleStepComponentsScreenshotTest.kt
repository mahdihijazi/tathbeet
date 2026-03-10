package com.quran.tathbeet.ui.features.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.PaceMethod
import com.quran.tathbeet.ui.model.PaceOption
import com.quran.tathbeet.ui.theme.TathbeetTheme
import com.quran.tathbeet.ui.theme.TathbeetTokens

private const val ScheduleComponentPreviewWidth = 411
private const val ScheduleSectionPreviewHeight = 320
private const val SchedulePreviewCardHeight = 300
private const val ManualSheetPreviewHeight = 920

@PreviewTest
@Preview(
    name = "schedule_cycle_target_section_one_month",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = ScheduleSectionPreviewHeight,
    showBackground = true,
)
@Composable
fun ScheduleCycleTargetSectionOneMonthScreenshot() {
    ScheduleComponentScreenshotFrame {
        ScheduleCycleTargetSection(
            selectedCycleTarget = CycleTarget.OneMonth,
            onCycleTargetSelected = {},
            onOpenManualSheet = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "schedule_cycle_target_section_two_months",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = ScheduleSectionPreviewHeight,
    showBackground = true,
)
@Composable
fun ScheduleCycleTargetSectionTwoMonthsScreenshot() {
    ScheduleComponentScreenshotFrame {
        ScheduleCycleTargetSection(
            selectedCycleTarget = CycleTarget.TwoMonths,
            onCycleTargetSelected = {},
            onOpenManualSheet = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "schedule_manual_pace_section",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = ScheduleSectionPreviewHeight,
    showBackground = true,
)
@Composable
fun ScheduleManualPaceSectionScreenshot() {
    ScheduleComponentScreenshotFrame {
        ScheduleManualPaceSection(
            onChangeManualPace = {},
            onResetToCycleMode = {},
        )
    }
}

@PreviewTest
@Preview(
    name = "schedule_rotation_preview_cycle_mode",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = SchedulePreviewCardHeight,
    showBackground = true,
)
@Composable
fun ScheduleRotationPreviewCycleModeScreenshot() {
    ScheduleComponentScreenshotFrame {
        ScheduleRotationPreviewCard(
            paceMethod = PaceMethod.CycleTarget,
            selectedCycleTarget = CycleTarget.OneMonth,
            selectedPace = PaceOption.OneHizb,
            segmentCount = 8,
            cycleLength = 8,
        )
    }
}

@PreviewTest
@Preview(
    name = "schedule_rotation_preview_manual_mode",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = SchedulePreviewCardHeight,
    showBackground = true,
)
@Composable
fun ScheduleRotationPreviewManualModeScreenshot() {
    ScheduleComponentScreenshotFrame {
        ScheduleRotationPreviewCard(
            paceMethod = PaceMethod.Manual,
            selectedCycleTarget = CycleTarget.OneMonth,
            selectedPace = PaceOption.OneJuz,
            segmentCount = 8,
            cycleLength = 1,
        )
    }
}

@PreviewTest
@Preview(
    name = "manual_pace_sheet_content",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = ManualSheetPreviewHeight,
    showBackground = true,
)
@Composable
fun ManualPaceSheetContentScreenshot() {
    ScheduleComponentScreenshotFrame {
        ManualPaceSheetContent(
            onPaceSelected = {},
        )
    }
}

@Composable
private fun ScheduleComponentScreenshotFrame(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TathbeetTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(TathbeetTokens.spacing.x3),
            ) {
                content()
            }
        }
    }
}
