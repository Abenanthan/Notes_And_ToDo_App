package com.example.notestodo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // An empty query becomes LIKE '%%', which matches every note,
    // so this one query serves as both "get all" and "search".
    @Query(
        """
        SELECT * FROM notes
        WHERE deletedAt IS NULL
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY isFavourite DESC, updatedAt DESC
        """
    )
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Long): Note?

    // Inserts when id = 0 (new note), updates when the id already exists.
    @Upsert
    suspend fun upsert(note: Note)

    @Query("SELECT * FROM notes WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getDeletedNotes(): Flow<List<Note>>

    // Permanent: @Delete removes the row, unlike moving a note to the trash.
    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL")
    suspend fun purgeAllDeleted()

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun purgeDeletedBefore(cutoff: Long)
}
