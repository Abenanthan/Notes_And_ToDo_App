package com.example.notestodo.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

const val REMINDER_CHANNEL_ID = "task_reminders"
const val EXTRA_TASK_ID = "task_id"
const val EXTRA_TASK_TITLE = "task_title"

// Called once at startup. Creating a channel that already exists does nothing,
// and without a channel Android drops the notification silently.
fun createReminderChannel(context: Context) {
    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        "Task reminders",
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = "Fires when a task with a reminder time is due"
    }
    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}

// From Android 12 an app needs permission to set alarms for an exact moment. Without it
// reminders still arrive, but the system may hold them back to save battery.
fun canScheduleExactReminders(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
}

// The settings page where the user grants that permission. Only reachable on Android 12+,
// which is the only version where canScheduleExactReminders() can return false.
fun exactAlarmSettingsIntent(context: Context): Intent =
    Intent(
        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        Uri.fromParts("package", context.packageName, null),
    )
