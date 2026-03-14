package com.quran.tathbeet.ui.features.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.quran.tathbeet.ui.model.CycleTarget
import com.quran.tathbeet.ui.model.PaceMethod
import com.quran.tathbeet.ui.model.PaceOption
import com.quran.tathbeet.ui.screenshot.ThemedScreenshotFrame

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
fun ScheduleCycleTargetSectionOneMonthScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = false) {
        ScheduleCycleTargetSectionPreview(selectedCycleTarget = CycleTarget.OneMonth)
    }

@PreviewTest
@Preview(
    name = "schedule_cycle_target_section_one_month_dark",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = ScheduleSectionPreviewHeight,
    showBackground = true,
)
@Composable
fun ScheduleCycleTargetSectionOneMonthDarkScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = true) {
        ScheduleCycleTargetSectionPreview(selectedCycleTarget = CycleTarget.OneMonth)
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
fun ScheduleCycleTargetSectionTwoMonthsScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = false) {
        ScheduleCycleTargetSectionPreview(selectedCycleTarget = CycleTarget.TwoMonths)
    }

@PreviewTest
@Preview(
    name = "schedule_cycle_target_section_two_months_dark",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = ScheduleSectionPreviewHeight,
    showBackground = true,
)
@Composable
fun ScheduleCycleTargetSectionTwoMonthsDarkScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = true) {
        ScheduleCycleTargetSectionPreview(selectedCycleTarget = CycleTarget.TwoMonths)
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
fun ScheduleManualPaceSectionScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = false) {
        ScheduleManualPaceSectionPreview()
    }

@PreviewTest
@Preview(
    name = "schedule_manual_pace_section_dark",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = ScheduleSectionPreviewHeight,
    showBackground = true,
)
@Composable
fun ScheduleManualPaceSectionDarkScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = true) {
        ScheduleManualPaceSectionPreview()
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
fun ScheduleRotationPreviewCycleModeScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = false) {
        ScheduleRotationPreviewCycleModePreview()
    }

@PreviewTest
@Preview(
    name = "schedule_rotation_preview_cycle_mode_dark",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = SchedulePreviewCardHeight,
    showBackground = true,
)
@Composable
fun ScheduleRotationPreviewCycleModeDarkScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = true) {
        ScheduleRotationPreviewCycleModePreview()
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
fun ScheduleRotationPreviewManualModeScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = false) {
        ScheduleRotationPreviewManualModePreview()
    }

@PreviewTest
@Preview(
    name = "schedule_rotation_preview_manual_mode_dark",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = SchedulePreviewCardHeight,
    showBackground = true,
)
@Composable
fun ScheduleRotationPreviewManualModeDarkScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = true) {
        ScheduleRotationPreviewManualModePreview()
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
fun ManualPaceSheetContentScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = false) {
        ManualPaceSheetContentPreview()
    }

@PreviewTest
@Preview(
    name = "manual_pace_sheet_content_dark",
    locale = "ar",
    widthDp = ScheduleComponentPreviewWidth,
    heightDp = ManualSheetPreviewHeight,
    showBackground = true,
)
@Composable
fun ManualPaceSheetContentDarkScreenshot() =
    ScheduleComponentScreenshotFrame(darkTheme = true) {
        ManualPaceSheetContentPreview()
    }

@Composable
private fun ScheduleComponentScreenshotFrame(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    ThemedScreenshotFrame(darkTheme = darkTheme) {
        content()
    }
}

@Composable
private fun ScheduleCycleTargetSectionPreview(selectedCycleTarget: CycleTarget) {
    ScheduleCycleTargetSection(
        selectedCycleTarget = selectedCycleTarget,
        onCycleTargetSelected = {},
        onOpenManualSheet = {},
    )
}

@Composable
private fun ScheduleManualPaceSectionPreview() {
    ScheduleManualPaceSection(
        onChangeManualPace = {},
        onResetToCycleMode = {},
    )
}

@Composable
private fun ScheduleRotationPreviewCycleModePreview() {
    ScheduleRotationPreviewCard(
        paceMethod = PaceMethod.CycleTarget,
        selectedCycleTarget = CycleTarget.OneMonth,
        selectedPace = PaceOption.OneHizb,
        segmentCount = 8,
        cycleLength = 8,
    )
}

@Composable
private fun ScheduleRotationPreviewManualModePreview() {
    ScheduleRotationPreviewCard(
        paceMethod = PaceMethod.Manual,
        selectedCycleTarget = CycleTarget.OneMonth,
        selectedPace = PaceOption.OneJuz,
        segmentCount = 8,
        cycleLength = 1,
    )
}

@Composable
private fun ManualPaceSheetContentPreview() {
    ManualPaceSheetContent(
        onPaceSelected = {},
    )
}
