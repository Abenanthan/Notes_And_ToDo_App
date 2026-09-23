package com.example.notestodo.data.repository

import com.example.notestodo.data.local.Task
import com.example.notestodo.data.local.TaskDao
import com.example.notestodo.reminder.TaskReminderScheduler
import com.example.notestodo.widget.TaskWidgetUpdater
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Every write goes through here, so a task's alarm and the home-screen widget always
// match what is stored.
class TaskRepository(
    private val taskDao: TaskDao,
    private val reminderScheduler: TaskReminderScheduler,
    private val widgetUpdater: TaskWidgetUpdater,
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
        widgetUpdater.updateAll()
    }

    fun getDeletedTasks(): Flow<List<Task>> = taskDao.getDeletedTasks()

    // Soft delete: the row stays put with a timestamp, so the task can come back.
    // Its reminder goes, though: a task in the trash should not ring.
    suspend fun moveTaskToTrash(task: Task) {
        taskDao.update(task.copy(deletedAt = System.currentTimeMillis()))
        reminderScheduler.cancel(task.id)
        widgetUpdater.updateAll()
    }

    suspend fun restoreTask(task: Task) {
        val restored = task.copy(deletedAt = null)
        taskDao.update(restored)
        // schedule() ignores reminders that have since passed.
        reminderScheduler.schedule(restored)
        widgetUpdater.updateAll()
    }

    suspend fun deleteTaskForever(task: Task) {
        taskDao.delete(task)
        reminderScheduler.cancel(task.id)
        widgetUpdater.updateAll()
    }

    suspend fun emptyTaskTrash() {
        taskDao.purgeAllDeleted()
        widgetUpdater.updateAll()
    }

    suspend fun purgeTasksDeletedBefore(cutoff: Long) = taskDao.purgeDeletedBefore(cutoff)

    // Completed tasks have no alarm left: it is cancelled the moment one is ticked off.
    suspend fun trashCompletedTasks() {
        taskDao.trashCompleted(System.currentTimeMillis())
        widgetUpdater.updateAll()
    }

    suspend fun rescheduleAllReminders() {
        taskDao.getTasksWithReminders().forEach { reminderScheduler.schedule(it) }
    }
}
