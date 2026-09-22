package com.example.notestodo

import android.app.Application
import androidx.room.Room
import com.example.notestodo.data.local.AppDatabase
import com.example.notestodo.data.repository.NoteRepository
import com.example.notestodo.data.repository.TaskRepository

// Manual dependency injection: the app holds one database and one repository per
// feature, all created lazily the first time they are used.
class NotesApp : Application() {

    private val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "notes_todo.db").build()
    }

    val noteRepository by lazy { NoteRepository(database.noteDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
}
