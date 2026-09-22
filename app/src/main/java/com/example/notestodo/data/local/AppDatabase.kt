package com.example.notestodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Created once in NotesApp. When a new entity or column is added, bump the version
// and add an AutoMigration(from = N, to = N + 1).
@Database(entities = [Note::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
