package com.example.notestodo.data.repository

import com.example.notestodo.data.local.Note
import com.example.notestodo.data.local.NoteDao
import kotlinx.coroutines.flow.Flow

// The single place ViewModels go for note data. If notes are ever synced to a
// server, only this class changes.
class NoteRepository(private val noteDao: NoteDao) {

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun getNote(id: Long): Note? = noteDao.getNote(id)

    suspend fun saveNote(note: Note) = noteDao.upsert(note)

    suspend fun deleteNote(note: Note) = noteDao.delete(note)
}
