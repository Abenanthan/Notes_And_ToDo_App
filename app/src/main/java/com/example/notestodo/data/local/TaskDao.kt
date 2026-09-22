package com.example.notestodo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Unfinished tasks first. Within each group: soonest due date first, then tasks
    // with no due date ("dueDate IS NULL" is 1 for those, so they sort last), newest first.
    @Query("SELECT * FROM tasks ORDER BY isDone, dueDate IS NULL, dueDate, createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: Long): Task?

    // Inserts when id = 0 (new task), updates when the id already exists.
    @Upsert
    suspend fun upsert(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE isDone = 1")
    suspend fun deleteCompleted()
}
