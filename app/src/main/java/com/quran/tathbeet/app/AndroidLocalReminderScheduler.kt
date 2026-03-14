package com.quran.tathbeet.app

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quran.tathbeet.MainActivity
import com.quran.tathbeet.R
import com.quran.tathbeet.core.time.TimeProvider
import com.quran.tathbeet.domain.model.LearnerAccount
import com.quran.tathbeet.domain.repository.ProfileRepository
import com.quran.tathbeet.domain.repository.ReviewRepository
import com.quran.tathbeet.domain.repository.ScheduleRepository
import com.quran.tathbeet.domain.repository.SettingsRepository
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first

class AndroidLocalReminderScheduler(
    context: Context,
    private val timeProvider: TimeProvider,
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val settingsRepository: SettingsRepository,
    private val reviewRepository: ReviewRepository,
) : LocalReminderScheduler, DebugNotificationController {

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = NotificationManagerCompat.from(appContext)

    override val reminderScenarios: List<ReminderNotificationDebugScenario> =
        ReminderNotificationDebugScenario.entries

    override suspend fun syncSchedules() {
        createNotificationChannel()

        val settings = settingsRepository.observeSettings().first()
        val accounts = profileRepository.observeAccounts().first()
        val scheduledProfileIds = accounts
            .filter { account -> hasSchedule(account.id) }
            .map { account -> account.id }
            .toSet()
        val eligibleProfileIds = ReminderSchedulePlanner.eligibleProfileIds(
            globalNotificationsEnabled = settings.globalNotificationsEnabled,
            canPostNotifications = canPostNotifications(),
            accounts = accounts,
            scheduledProfileIds = scheduledProfileIds,
        )

        accounts.forEach { account ->
            if (account.id in eligibleProfileIds) {
                scheduleReminder(
                    profileId = account.id,
                    hour = settings.reminderHour,
                    minute = settings.reminderMinute,
                    referenceTime = timeProvider.now(),
                )
            } else {
                cancelProfile(account.id)
            }
        }
    }

    override suspend fun cancelProfile(profileId: String) {
        alarmManager.cancel(reminderPendingIntent(profileId))
    }

    override suspend fun handleReminder(profileId: String) {
        val settings = settingsRepository.observeSettings().first()
        val account = profileRepository.observeAccounts().first()
            .firstOrNull { profile -> profile.id == profileId }
            ?: return
        val canPostNotifications = canPostNotifications()
        val hasSchedule = hasSchedule(profileId)

        if (
            !ReminderSchedulePlanner.shouldHandleReminder(
                globalNotificationsEnabled = settings.globalNotificationsEnabled,
                canPostNotifications = canPostNotifications,
                accountNotificationsEnabled = account.notificationsEnabled,
                hasSchedule = hasSchedule,
            )
        ) {
            cancelProfile(profileId)
            return
        }

        reviewRepository.ensureAssignmentsForDate(
            learnerId = profileId,
            assignedForDate = timeProvider.today(),
        )

        showReminderNotification(
            account = account,
            motivationalMessagesEnabled = settings.motivationalMessagesEnabled,
        )

        scheduleReminder(
            profileId = profileId,
            hour = settings.reminderHour,
            minute = settings.reminderMinute,
            referenceTime = timeProvider.now().plusMinutes(1),
        )
    }

    override suspend fun triggerReminderNotification(scenario: ReminderNotificationDebugScenario) {
        createNotificationChannel()
        if (!canPostNotifications()) {
            return
        }

        val account = profileRepository.observeAccounts().first().firstOrNull() ?: return
        val copy = reminderDebugCopy(
            scenario = scenario,
            account = account,
        )
        postReminderNotification(
            notificationId = debugNotificationId(scenario),
            requestCode = debugNotificationId(scenario),
            profileId = account.id,
            title = copy.title,
            body = copy.body,
        )
    }

    private suspend fun showReminderNotification(
        account: LearnerAccount,
        motivationalMessagesEnabled: Boolean,
    ) {
        val copy = buildReminderCopy(
            account = account,
            learnerId = account.id,
            motivationalMessagesEnabled = motivationalMessagesEnabled,
        )
        postReminderNotification(
            notificationId = notificationRequestCode(account.id),
            requestCode = notificationRequestCode(account.id),
            profileId = account.id,
            title = copy.title,
            body = copy.body,
        )
    }

    private suspend fun buildReminderCopy(
        account: LearnerAccount,
        learnerId: String,
        motivationalMessagesEnabled: Boolean,
    ): ReminderNotificationCopy {
        val timeline = reviewRepository.observeReviewTimeline(learnerId).first()
        val today = timeProvider.today()
        val hasRollover = timeline.any { day ->
            day.assignedForDate.isBefore(today) && day.assignments.any { assignment -> !assignment.isDone }
        }
        return ReminderNotificationContent.buildCopy(
            templates = reminderNotificationTemplates(account.name),
            includeProfileName = shouldIncludeProfileName(account),
            nextTask = ReminderNotificationContent.nextAvailableTask(
                timeline = timeline,
                today = today,
            ),
            hasRollover = hasRollover,
            motivation = if (motivationalMessagesEnabled) {
                ReminderHadithCatalog.notificationEntryFor(timeProvider.today().dayOfYear)
                    .toMotivation(appContext::getString)
            } else {
                null
            },
        )
    }

    private suspend fun reminderDebugCopy(
        scenario: ReminderNotificationDebugScenario,
        account: LearnerAccount,
    ): ReminderNotificationCopy =
        ReminderNotificationContent.buildCopy(
            templates = reminderNotificationTemplates(account.name),
            includeProfileName = scenario.includeProfileName,
            nextTask = ReminderNotificationContent.nextAvailableTask(
                timeline = reviewRepository.observeReviewTimeline(account.id).first(),
                today = timeProvider.today(),
            ),
            hasRollover = scenario.hasRollover,
            motivation = scenario.hadithEntry?.toMotivation(appContext::getString),
        )

    private suspend fun shouldIncludeProfileName(account: LearnerAccount): Boolean {
        val accounts = profileRepository.observeAccounts().first()
        return accounts.count { profile ->
            profile.notificationsEnabled && hasSchedule(profile.id)
        } > 1 || !account.isSelfProfile
    }

    private suspend fun hasSchedule(profileId: String): Boolean =
        scheduleRepository.observeActiveSchedule(profileId).first() != null

    private fun scheduleReminder(
        profileId: String,
        hour: Int,
        minute: Int,
        referenceTime: ZonedDateTime,
    ) {
        val triggerAt = ReminderSchedulePlanner.nextTriggerAt(
            referenceTime = referenceTime,
            hour = hour,
            minute = minute,
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.toInstant().toEpochMilli(),
            reminderPendingIntent(profileId),
        )
    }

    private fun reminderPendingIntent(profileId: String): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            reminderRequestCode(profileId),
            Intent(appContext, ReminderBroadcastReceiver::class.java).apply {
                action = ReminderAlarmAction
                putExtra(ReminderProfileIdExtra, profileId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun reminderRequestCode(profileId: String): Int =
        profileId.hashCode()

    private fun notificationRequestCode(profileId: String): Int =
        profileId.hashCode() + 31_000

    private fun debugNotificationId(scenario: ReminderNotificationDebugScenario): Int =
        61_000 + scenario.ordinal

    private fun canPostNotifications(): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationManager.areNotificationsEnabled()
        } else {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                ReminderChannelId,
                appContext.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = appContext.getString(R.string.reminder_channel_description)
            },
        )
    }

    private fun reminderNotificationTemplates(
        accountName: String,
    ) = ReminderNotificationTemplates(
        generalTitle = appContext.getString(R.string.reminder_notification_title),
        namedTitle = appContext.getString(R.string.reminder_notification_title_for_profile, accountName),
        taskTitle = appContext.getString(R.string.reminder_notification_title_with_task),
        fallbackTodayBody = appContext.getString(R.string.reminder_notification_body_today),
        fallbackRolloverBody = appContext.getString(R.string.reminder_notification_body_rollover),
        nextTaskTodayBody = appContext.getString(R.string.reminder_notification_body_next_task),
        nextTaskRolloverBody = appContext.getString(R.string.reminder_notification_body_next_task_rollover),
        motivationBody = appContext.getString(R.string.reminder_notification_body_with_motivation),
    )

    private fun postReminderNotification(
        notificationId: Int,
        requestCode: Int,
        profileId: String,
        title: String,
        body: String,
    ) {
        val openIntent = Intent(appContext, MainActivity::class.java).apply {
            action = ReminderNotificationAction
            putExtra(ReminderProfileIdExtra, profileId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            appContext,
            requestCode,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(appContext, ReminderChannelId)
            .setSmallIcon(R.drawable.tathbeet_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val ReminderChannelId = "daily_reminders"
        const val ReminderAlarmAction = "com.quran.tathbeet.REMINDER_ALARM"
        const val ReminderNotificationAction = "com.quran.tathbeet.REMINDER_NOTIFICATION"
        const val ReminderProfileIdExtra = "reminder_profile_id"
    }
}

private val ReminderNotificationDebugScenario.hadithEntry: ReminderHadithEntry?
    get() = if (includesMotivation) {
        ReminderHadithCatalog.notificationEntries[motivationalEntryIndex]
    } else {
        null
    }

private fun ReminderHadithEntry.toMotivation(
    resolve: (Int) -> String,
): ReminderNotificationMotivation =
    ReminderNotificationMotivation(
        text = resolve(textResId),
        source = resolve(sourceResId),
    )
