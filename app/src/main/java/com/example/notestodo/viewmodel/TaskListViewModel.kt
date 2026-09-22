package com.example.notestodo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notestodo.NotesApp
import com.example.notestodo.data.local.Task
import com.example.notestodo.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    // Room re-emits whenever the tasks table changes, so ticking a checkbox or saving
    // a task updates the list on its own. null means the first result hasn't loaded yet.
    val tasks: StateFlow<List<Task>?> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onTaskCheckedChange(task: Task, isDone: Boolean) {
        viewModelScope.launch { repository.saveTask(task.copy(isDone = isDone)) }
    }

    fun clearCompleted() {
        viewModelScope.launch { repository.deleteCompletedTasks() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotesApp
                TaskListViewModel(app.taskRepository)
            }
        }
    }
}
