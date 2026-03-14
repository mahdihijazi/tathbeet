package com.quran.tathbeet.app

import com.quran.tathbeet.domain.model.ReviewAssignment
import com.quran.tathbeet.domain.model.ReviewDay
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderNotificationContentTest {

    @Test
    fun buildCopy_uses_next_task_context_for_today_notification() {
        val copy = ReminderNotificationContent.buildCopy(
            templates = templates(),
            includeProfileName = false,
            nextTask = ReminderNotificationTaskPreview(
                title = "سورة الإخلاص",
            ),
            hasRollover = false,
            motivation = null,
        )

        assertEquals("حان موعد وردك", copy.title)
        assertEquals("ابدأ الآن بـ سورة الإخلاص", copy.body)
    }

    @Test
    fun buildCopy_moves_task_name_to_title_when_motivation_is_present() {
        val copy = ReminderNotificationContent.buildCopy(
            templates = templates(),
            includeProfileName = true,
            nextTask = ReminderNotificationTaskPreview(
                title = "سورة الإخلاص",
            ),
            hasRollover = false,
            motivation = ReminderNotificationMotivation(
                text = "حديث قصير",
                source = "صحيح مسلم",
            ),
        )

        assertEquals("حان موعد الورد لـ أحمد - سورة الإخلاص", copy.title)
        assertEquals("حديث قصير\nصحيح مسلم", copy.body)
    }

    @Test
    fun buildCopy_uses_single_line_rollover_sentence_for_regular_notification() {
        val copy = ReminderNotificationContent.buildCopy(
            templates = templates(),
            includeProfileName = false,
            nextTask = ReminderNotificationTaskPreview(
                title = "سورة الملك",
            ),
            hasRollover = true,
            motivation = null,
        )

        assertEquals("حان موعد وردك", copy.title)
        assertEquals(
            "ابدأ الآن بـ سورة الملك ولديك أيضاً مراجعات مؤجلة من الأيام السابقة.",
            copy.body,
        )
    }

    @Test
    fun nextAvailableTask_picks_first_pending_assignment_before_future_days() {
        val today = LocalDate.of(2026, 3, 14)
        val nextTask = ReminderNotificationContent.nextAvailableTask(
            timeline = listOf(
                reviewDay(
                    date = today.minusDays(1),
                    assignments = listOf(
                        assignment(
                            id = "done-rollover",
                            date = today.minusDays(1),
                            displayOrder = 0,
                            isDone = true,
                            title = "سورة الناس",
                        ),
                        assignment(
                            id = "next-rollover",
                            date = today.minusDays(1),
                            displayOrder = 1,
                            isDone = false,
                            title = "سورة الفلق",
                        ),
                    ),
                ),
                reviewDay(
                    date = today,
                    assignments = listOf(
                        assignment(
                            id = "today-task",
                            date = today,
                            displayOrder = 0,
                            isDone = false,
                            title = "سورة الإخلاص",
                        ),
                    ),
                ),
                reviewDay(
                    date = today.plusDays(1),
                    assignments = listOf(
                        assignment(
                            id = "future-task",
                            date = today.plusDays(1),
                            displayOrder = 0,
                            isDone = false,
                            title = "سورة الكافرون",
                        ),
                    ),
                ),
            ),
            today = today,
        )

        assertEquals(
            ReminderNotificationTaskPreview(
                title = "سورة الفلق",
            ),
            nextTask,
        )
    }

    @Test
    fun nextAvailableTask_returns_null_when_everything_available_is_done() {
        val today = LocalDate.of(2026, 3, 14)

        val nextTask = ReminderNotificationContent.nextAvailableTask(
            timeline = listOf(
                reviewDay(
                    date = today,
                    assignments = listOf(
                        assignment(
                            id = "done",
                            date = today,
                            displayOrder = 0,
                            isDone = true,
                            title = "سورة الإخلاص",
                        ),
                    ),
                ),
            ),
            today = today,
        )

        assertNull(nextTask)
    }

    private fun templates() = ReminderNotificationTemplates(
        generalTitle = "حان موعد وردك",
        namedTitle = "حان موعد الورد لـ أحمد",
        taskTitle = "%1\$s - %2\$s",
        fallbackTodayBody = "افتح التطبيق وابدأ أول مقطع من ورد اليوم.",
        fallbackRolloverBody = "افتح التطبيق وأكمل المقاطع المؤجلة مع ورد اليوم.",
        nextTaskTodayBody = "ابدأ الآن بـ %1\$s",
        nextTaskRolloverBody = "ابدأ الآن بـ %1\$s ولديك أيضاً مراجعات مؤجلة من الأيام السابقة.",
        motivationBody = "%1\$s\n%2\$s",
    )

    private fun reviewDay(
        date: LocalDate,
        assignments: List<ReviewAssignment>,
    ) = ReviewDay(
        learnerId = "self",
        assignedForDate = date,
        completionRate = 0,
        assignments = assignments,
    )

    private fun assignment(
        id: String,
        date: LocalDate,
        displayOrder: Int,
        isDone: Boolean,
        title: String,
    ) = ReviewAssignment(
        id = id,
        learnerId = "self",
        assignedForDate = date,
        taskKey = id,
        title = title,
        detail = "5 آيات",
        rubId = displayOrder,
        readingTarget = null,
        weight = 1.0,
        displayOrder = displayOrder,
        isRollover = date.isBefore(LocalDate.of(2026, 3, 14)),
        isDone = isDone,
        rating = null,
        completedAt = null,
    )
}
