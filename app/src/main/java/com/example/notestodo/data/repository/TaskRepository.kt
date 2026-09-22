package com.example.notestodo.data.repository

import com.example.notestodo.data.local.Task
import com.example.notestodo.data.local.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTask(id: Long): Task? = taskDao.getTask(id)

    suspend fun saveTask(task: Task) = taskDao.upsert(task)

    suspend fun deleteTask(task: Task) = taskDao.delete(task)

    suspend fun deleteCompletedTasks() = taskDao.deleteCompleted()
}
