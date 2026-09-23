package com.example.notestodo.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Created once in NotesApp. When a new entity or column is added, bump the version
// and add an AutoMigration(from = N, to = N + 1). Room compares the schema JSON files
// in app/schemas to generate the SQL, so existing notes and tasks are kept.
@Database(
    entities = [Note::class, Task::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2), // v2: added the tasks table
        AutoMigration(from = 2, to = 3), // v3: added tasks.dueTime
        AutoMigration(from = 3, to = 4), // v4: added notes.isFavourite and notes.colorIndex
    ],
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
}
