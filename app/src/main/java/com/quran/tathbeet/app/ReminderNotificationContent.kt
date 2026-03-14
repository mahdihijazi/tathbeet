package com.quran.tathbeet.app

import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import java.time.LocalDate

data class ReminderNotificationTaskPreview(
    val title: String,
)

data class ReminderNotificationMotivation(
    val text: String,
    val source: String,
)

data class ReminderNotificationCopy(
    val title: String,
    val body: String,
)

data class ReminderNotificationTemplates(
    val generalTitle: String,
    val namedTitle: String,
    val taskTitle: String,
    val fallbackTodayBody: String,
    val fallbackRolloverBody: String,
    val nextTaskTodayBody: String,
    val nextTaskRolloverBody: String,
    val motivationBody: String,
)

internal object ReminderNotificationContent {
    fun buildCopy(
        templates: ReminderNotificationTemplates,
        includeProfileName: Boolean,
        nextTask: ReminderNotificationTaskPreview?,
        hasRollover: Boolean,
        motivation: ReminderNotificationMotivation?,
    ): ReminderNotificationCopy {
        val baseTitle = if (includeProfileName) templates.namedTitle else templates.generalTitle
        val title = if (motivation != null && nextTask != null) {
            templates.taskTitle.format(baseTitle, nextTask.title)
        } else {
            baseTitle
        }
        val baseBody = when {
            nextTask != null && hasRollover -> templates.nextTaskRolloverBody.format(nextTask.title)
            nextTask != null -> templates.nextTaskTodayBody.format(nextTask.title)
            hasRollover -> templates.fallbackRolloverBody
            else -> templates.fallbackTodayBody
        }
        val body = if (motivation == null) {
            baseBody
        } else {
            templates.motivationBody.format(motivation.text, motivation.source)
        }
        return ReminderNotificationCopy(
            title = title,
            body = body,
        )
    }

    fun nextAvailableTask(
        timeline: List<ReviewDay>,
        today: LocalDate,
    ): ReminderNotificationTaskPreview? =
        timeline
            .asSequence()
            .filter { reviewDay -> !reviewDay.assignedForDate.isAfter(today) }
            .sortedBy { reviewDay -> reviewDay.assignedForDate }
            .flatMap { reviewDay ->
                reviewDay.assignments
                    .sortedBy(ReviewAssignment::displayOrder)
                    .asSequence()
            }
            .firstOrNull { assignment -> !assignment.isDone }
            ?.let { assignment ->
                ReminderNotificationTaskPreview(
                    title = assignment.title,
                )
            }
}
