package com.example.notestodo.data.repository

import com.example.notestodo.data.local.Task
import com.example.notestodo.data.local.TaskDao
import com.example.notestodo.reminder.TaskReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Every write goes through here, so a task's alarm always matches what is stored.
class TaskRepository(
    private val taskDao: TaskDao,
    private val reminderScheduler: TaskReminderScheduler,
) {

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksBetween(start: LocalDate, end: LocalDate): Flow<List<Task>> =
        taskDao.getTasksBetween(start, end)

    suspend fun getTask(id: Long): Task? = taskDao.getTask(id)

    suspend fun saveTask(task: Task) {
        val saved = if (task.id == 0L) {
            task.copy(id = taskDao.insert(task))
        } else {
            taskDao.update(task)
            task
        }
        // schedule() also clears the alarm when the task is done or has no time.
        reminderScheduler.schedule(saved)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
        reminderScheduler.cancel(task.id)
    }

    // Completed tasks have no alarm left: it is cancelled the moment one is ticked off.
    suspend fun deleteCompletedTasks() = taskDao.deleteCompleted()

    suspend fun rescheduleAllReminders() {
        taskDao.getTasksWithReminders().forEach { reminderScheduler.schedule(it) }
    }
}
