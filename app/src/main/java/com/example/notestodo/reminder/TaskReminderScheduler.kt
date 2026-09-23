package com.example.notestodo.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.notestodo.data.local.Task
import java.time.LocalDateTime
import java.time.ZoneId

// Sets and clears the alarm behind a task's reminder. There is one alarm per task,
// identified by the task's id, so saving a task again just replaces its alarm.
class TaskReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(task: Task) {
        // Start from a clean slate: the date, the time or the task itself may have changed.
        cancel(task.id)

        val date = task.dueDate ?: return
        val time = task.dueTime ?: return // no time means the user wants no reminder
        if (task.isDone) return

        val triggerAtMillis = LocalDateTime.of(date, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        if (triggerAtMillis <= System.currentTimeMillis()) return // the moment has passed

        val pendingIntent = reminderIntent(task.id, task.title)
        if (canScheduleExactReminders(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            // Permission refused: still deliver it, just without the promise of being punctual.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(taskId: Long) {
        // FLAG_NO_CREATE returns null when this task has no alarm waiting.
        val existing = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intentFor(taskId, title = null),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (existing != null) {
            alarmManager.cancel(existing)
            existing.cancel()
        }
    }

    private fun reminderIntent(taskId: Long, title: String): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intentFor(taskId, title),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    // Two PendingIntents count as the same alarm when the request code and the intent
    // match; the extras are ignored. That is why the task id alone can cancel one.
    private fun intentFor(taskId: Long, title: String?) =
        Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
        }
}
