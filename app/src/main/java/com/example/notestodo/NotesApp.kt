package com.example.notestodo

import android.app.Application
import androidx.room.Room
import com.example.notestodo.data.local.AppDatabase
import com.example.notestodo.data.repository.NoteRepository
import com.example.notestodo.data.repository.SettingsRepository
import com.example.notestodo.data.repository.TaskRepository
import com.example.notestodo.reminder.TaskReminderScheduler
import com.example.notestodo.reminder.createReminderChannel
import com.example.notestodo.widget.TaskWidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

// Manual dependency injection: the app holds one database and one repository per
// feature, all created lazily the first time they are used.
class NotesApp : Application() {

    private val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "notes_todo.db").build()
    }

    private val reminderScheduler by lazy { TaskReminderScheduler(this) }
    private val widgetUpdater by lazy { TaskWidgetUpdater(this) }

    val noteRepository by lazy { NoteRepository(database.noteDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao(), reminderScheduler, widgetUpdater) }
    val settingsRepository by lazy { SettingsRepository(this) }

    // Outlives any one screen, for work that belongs to the app itself.
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Cheap to do on every start, and the reminder notifications need it to exist.
        createReminderChannel(this)

        // Anything that has sat in the trash for longer than the retention period goes
        // for good. Doing it at startup keeps it simple: no background worker needed.
        applicationScope.launch {
            val cutoff = System.currentTimeMillis() - TRASH_RETENTION.inWholeMilliseconds
            noteRepository.purgeNotesDeletedBefore(cutoff)
            taskRepository.purgeTasksDeletedBefore(cutoff)
        }
    }

    companion object {
        val TRASH_RETENTION = 30.days
    }
}
