package com.quran.tathbeet.ui.model

import android.content.Context
import androidx.annotation.StringRes
import com.quran.tathbeet.R
import kotlin.math.ceil
import kotlin.math.roundToInt

enum class AppDestination(
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
) {
    Profiles(R.string.destination_profiles, R.string.destination_profiles),
    ScheduleIntro(R.string.destination_schedule_intro, R.string.destination_schedule_intro),
    PoolSelector(R.string.destination_pool_selector, R.string.destination_pool_selector_short),
    ScheduleDose(R.string.destination_schedule_dose, R.string.destination_schedule_dose),
    Review(R.string.destination_review, R.string.destination_review),
    Progress(R.string.destination_progress, R.string.destination_progress),
    Shared(R.string.destination_shared, R.string.destination_shared),
    Settings(R.string.destination_settings, R.string.destination_settings),
}

enum class AccountMode {
    Guest,
    Google,
}

enum class PaceOption(
    @param:StringRes val labelRes: Int,
    val dailySegments: Int,
) {
    OneRub(R.string.pace_one_rub, 1),
    OneHizb(R.string.pace_one_hizb, 4),
    OneJuz(R.string.pace_one_juz, 8),
    OneAndHalfJuz(R.string.pace_one_and_half_juz, 12),
    TwoJuz(R.string.pace_two_juz, 16),
    TwoAndHalfJuz(R.string.pace_two_and_half_juz, 20),
    ThreeJuz(R.string.pace_three_juz, 24),
    FourJuz(R.string.pace_four_juz, 32),
    FiveJuz(R.string.pace_five_juz, 40),
}

enum class PaceMethod {
    CycleTarget,
    Manual,
}

enum class CycleTarget(
    @param:StringRes val labelRes: Int,
    val days: Int,
) {
    OneWeek(R.string.cycle_target_one_week, 7),
    TwoWeeks(R.string.cycle_target_two_weeks, 14),
    OneMonth(R.string.cycle_target_one_month, 30),
    FortyFiveDays(R.string.cycle_target_forty_five_days, 45),
    TwoMonths(R.string.cycle_target_two_months, 60),
}

enum class SelectionCategory(
    @param:StringRes val labelRes: Int,
) {
    Surahs(R.string.selection_category_surahs),
    Juz(R.string.selection_category_juz),
    Hizb(R.string.selection_category_hizb),
    Rub(R.string.selection_category_rub),
}

enum class Guardian {
    Mother,
    Father,
}

enum class SyncState {
    OfflineReady,
    SyncPending,
    Synced,
}

data class ReviewTask(
    val id: String,
    val title: TextSpec,
    val detail: TextSpec,
    val isDone: Boolean,
    val isRollover: Boolean,
)

data class AppProfile(
    val id: String,
    val name: TextSpec,
    val isSelfProfile: Boolean,
    val isShared: Boolean,
    val guardians: Set<Guardian>,
    val notificationsEnabled: Boolean,
    val paceMethod: PaceMethod,
    val cycleTarget: CycleTarget,
    val pace: PaceOption,
    val selectedPoolKeys: Set<String>,
    val reviewTasks: List<ReviewTask>,
    val weekCompletion: List<Float>,
    val activityFeed: List<TextSpec>,
)

data class AppUiState(
    val destination: AppDestination,
    val scheduleReturnDestination: AppDestination,
    val accountMode: AccountMode,
    val profiles: List<AppProfile>,
    val activeProfileId: String,
    val activeSelectionCategory: SelectionCategory,
    val hasSeenScheduleIntro: Boolean,
    val hasCompletedScheduleOnboarding: Boolean,
    val globalNotificationsEnabled: Boolean,
    val motivationalMessagesEnabled: Boolean,
    val reminderTime: String,
    val arabicFirst: Boolean,
    val syncState: SyncState,
    val extraProfileCount: Int,
)

val AppUiState.activeProfile: AppProfile
    get() = profiles.first { it.id == activeProfileId }

val AppProfile.dailyProgress: Float
    get() = if (reviewTasks.isEmpty()) 0f else reviewTasks.count { it.isDone }.toFloat() / reviewTasks.size

val AppProfile.completionRate: Int
    get() = (weekCompletion.average() * 100).roundToInt()

fun AppUiState.poolSegmentCount(
    catalog: QuranCatalog,
    profile: AppProfile = activeProfile,
): Int =
    catalog.resolveSelections(profile.selectedPoolKeys).sumOf { it.segments }

fun AppUiState.cycleLength(
    catalog: QuranCatalog,
    profile: AppProfile = activeProfile,
): Int =
    ceil(poolSegmentCount(catalog, profile).toFloat() / profile.pace.dailySegments).toInt().coerceAtLeast(1)

fun recommendedPace(
    segmentCount: Int,
    cycleTarget: CycleTarget,
): PaceOption {
    val requiredSegmentsPerDay = ceil(segmentCount.toFloat() / cycleTarget.days).toInt().coerceAtLeast(1)
    return PaceOption.entries.firstOrNull { it.dailySegments >= requiredSegmentsPerDay }
        ?: PaceOption.entries.last()
}

fun AppUiState.updateActiveProfile(transform: (AppProfile) -> AppProfile): AppUiState =
    copy(
        profiles = profiles.map { profile ->
            if (profile.id == activeProfileId) transform(profile) else profile
        },
    )

fun AppUiState.scheduleWizardStartDestination(): AppDestination =
    if (hasSeenScheduleIntro) AppDestination.PoolSelector else AppDestination.ScheduleIntro

fun generateTasksForProfile(
    profile: AppProfile,
    catalog: QuranCatalog,
    context: Context,
): List<ReviewTask> {
    val base = catalog.buildReviewUnits(
        context = context,
        keys = profile.selectedPoolKeys,
    )
        .mapIndexed { index, task ->
            ReviewTask(
                id = task.id,
                title = TextSpec(rawText = task.title),
                detail = TextSpec(rawText = task.detail),
                isDone = index == 1 || index == 2,
                isRollover = index == 0 || index == 1,
            )
        }

    return if (base.isEmpty()) {
        listOf(
            ReviewTask(
                id = "starter",
                title = TextSpec(R.string.review_starter_title),
                detail = TextSpec(R.string.review_starter_detail),
                isDone = false,
                isRollover = false,
            ),
        )
    } else {
        base.take(profile.pace.dailySegments.coerceAtMost(base.size))
    }
}

fun seedAppState(
    context: Context,
    catalog: QuranCatalog,
): AppUiState {
    val father = AppProfile(
        id = "mahdi",
        name = TextSpec(R.string.profile_name_father),
        isSelfProfile = true,
        isShared = false,
        guardians = emptySet(),
        notificationsEnabled = true,
        paceMethod = PaceMethod.CycleTarget,
        cycleTarget = CycleTarget.OneMonth,
        pace = PaceOption.OneJuz,
        selectedPoolKeys = setOf(
            catalog.requireSelection(SelectionCategory.Surahs, 2).key,
            catalog.requireSelection(SelectionCategory.Juz, 30).key,
            catalog.requireSelection(SelectionCategory.Hizb, 41).key,
        ),
        reviewTasks = emptyList(),
        weekCompletion = listOf(0.8f, 0.7f, 1f, 0.6f, 0.75f, 0.9f, 0.5f),
        activityFeed = listOf(
            TextSpec(R.string.feed_father_today),
            TextSpec(R.string.feed_father_yesterday),
        ),
    )
    val maryam = AppProfile(
        id = "maryam",
        name = TextSpec(R.string.profile_name_maryam),
        isSelfProfile = false,
        isShared = true,
        guardians = setOf(Guardian.Mother, Guardian.Father),
        notificationsEnabled = true,
        paceMethod = PaceMethod.CycleTarget,
        cycleTarget = CycleTarget.OneMonth,
        pace = PaceOption.OneJuz,
        selectedPoolKeys = setOf(
            catalog.requireSelection(SelectionCategory.Juz, 30).key,
        ),
        reviewTasks = emptyList(),
        weekCompletion = listOf(1f, 0.5f, 1f, 1f, 0.8f, 0.65f, 0.4f),
        activityFeed = listOf(
            TextSpec(R.string.feed_shared_father_done),
            TextSpec(R.string.feed_shared_mother_time),
            TextSpec(R.string.feed_shared_synced),
        ),
    )
    val yusuf = AppProfile(
        id = "yusuf",
        name = TextSpec(R.string.profile_name_yusuf),
        isSelfProfile = false,
        isShared = false,
        guardians = setOf(Guardian.Mother),
        notificationsEnabled = false,
        paceMethod = PaceMethod.CycleTarget,
        cycleTarget = CycleTarget.OneMonth,
        pace = PaceOption.OneJuz,
        selectedPoolKeys = setOf(
            catalog.requireSelection(SelectionCategory.Surahs, 2).key,
            catalog.requireSelection(SelectionCategory.Rub, 12).key,
        ),
        reviewTasks = emptyList(),
        weekCompletion = listOf(0.2f, 0.3f, 0.5f, 0.2f, 0.6f, 0.4f, 0.25f),
        activityFeed = listOf(
            TextSpec(R.string.feed_yusuf_today),
            TextSpec(R.string.feed_yusuf_yesterday),
        ),
    )

    val seededProfiles = listOf(father, maryam, yusuf).map { profile ->
        val recommendedPace = recommendedPace(
            segmentCount = catalog.resolveSelections(profile.selectedPoolKeys).sumOf { it.segments },
            cycleTarget = profile.cycleTarget,
        )
        profile.copy(
            pace = if (profile.paceMethod == PaceMethod.CycleTarget) recommendedPace else profile.pace,
            reviewTasks = generateTasksForProfile(
                profile = if (profile.paceMethod == PaceMethod.CycleTarget) {
                    profile.copy(pace = recommendedPace)
                } else {
                    profile
                },
                catalog = catalog,
                context = context,
            ),
        )
    }

    return AppUiState(
        destination = AppDestination.ScheduleIntro,
        scheduleReturnDestination = AppDestination.Review,
        accountMode = AccountMode.Guest,
        profiles = seededProfiles,
        activeProfileId = "maryam",
        activeSelectionCategory = SelectionCategory.Surahs,
        hasSeenScheduleIntro = false,
        hasCompletedScheduleOnboarding = false,
        globalNotificationsEnabled = true,
        motivationalMessagesEnabled = true,
        reminderTime = reminderOptions[2],
        arabicFirst = true,
        syncState = SyncState.OfflineReady,
        extraProfileCount = 0,
    )
}

val reminderOptions = listOf("18:30", "19:00", "19:30", "20:00")

@StringRes
fun SyncState.displayLabelRes(): Int = when (this) {
    SyncState.OfflineReady -> R.string.sync_offline_ready
    SyncState.SyncPending -> R.string.sync_pending
    SyncState.Synced -> R.string.sync_synced
}
