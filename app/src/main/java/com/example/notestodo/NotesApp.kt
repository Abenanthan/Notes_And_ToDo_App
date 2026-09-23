package com.example.notestodo

import android.app.Application
import androidx.room.Room
import com.example.notestodo.data.local.AppDatabase
import com.example.notestodo.data.repository.NoteRepository
import com.example.notestodo.data.repository.SettingsRepository
import com.example.notestodo.data.repository.TaskRepository
import com.example.notestodo.reminder.TaskReminderScheduler
import com.example.notestodo.reminder.createReminderChannel

// Manual dependency injection: the app holds one database and one repository per
// feature, all created lazily the first time they are used.
class NotesApp : Application() {

    private val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "notes_todo.db").build()
    }

    private val reminderScheduler by lazy { TaskReminderScheduler(this) }

    val noteRepository by lazy { NoteRepository(database.noteDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao(), reminderScheduler) }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        // Cheap to do on every start, and the reminder notifications need it to exist.
        createReminderChannel(this)
    }
}
