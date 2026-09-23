package com.example.notestodo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {

    // Unfinished tasks first. Within each group: soonest due date first, then tasks
    // with no due date ("dueDate IS NULL" is 1 for those, so they sort last), newest first.
    @Query("SELECT * FROM tasks ORDER BY isDone, dueDate IS NULL, dueDate, createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    // Tasks due within a date range: the calendar loads one month at a time.
    // Room runs the LocalDate arguments through Converters, same as the stored column.
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :start AND :end ORDER BY isDone, dueDate, createdAt DESC")
    fun getTasksBetween(start: LocalDate, end: LocalDate): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: Long): Task?

    // Returns the new row's id, which the repository needs to schedule the reminder
    // of a task that has just been created.
    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    // Unfinished tasks that still have a reminder due; used to set the alarms again
    // after the device restarts.
    @Query("SELECT * FROM tasks WHERE dueTime IS NOT NULL AND isDone = 0")
    suspend fun getTasksWithReminders(): List<Task>

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE isDone = 1")
    suspend fun deleteCompleted()
}
