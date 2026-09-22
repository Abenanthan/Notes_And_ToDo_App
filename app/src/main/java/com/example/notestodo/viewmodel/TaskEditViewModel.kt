package com.example.notestodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notestodo.NotesApp
import com.example.notestodo.data.local.Task
import com.example.notestodo.data.repository.TaskRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

// Same pattern as NoteEditViewModel: load by id, edit in Compose state, save on the way out.
class TaskEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
) : ViewModel() {

    // Navigation puts the "taskId" route argument into the SavedStateHandle.
    private val taskId: Long = savedStateHandle[TASK_ID_ARG] ?: NEW_TASK_ID
    val isNewTask = taskId == NEW_TASK_ID

    // The task as it was loaded from the database; stays null for a new task.
    private var existingTask: Task? = null

    var title by mutableStateOf("")
        private set
    var dueDate by mutableStateOf<LocalDate?>(null)
        private set

    // Becomes true after the save/delete has finished; the screen then navigates back.
    var isFinished by mutableStateOf(false)
        private set
    private var isClosing = false

    init {
        if (!isNewTask) {
            viewModelScope.launch {
                existingTask = repository.getTask(taskId)?.also {
                    title = it.title
                    dueDate = it.dueDate
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        title = value
    }

    fun onDueDateChange(date: LocalDate?) {
        dueDate = date
    }

    fun saveAndClose() = finish {
        val task = existingTask
        val trimmedTitle = title.trim()
        when {
            // A task needs a title: clearing it removes the task, and a blank new task is never saved.
            trimmedTitle.isEmpty() -> task?.let { repository.deleteTask(it) }
            task == null -> repository.saveTask(Task(title = trimmedTitle, dueDate = dueDate))
            task.title != trimmedTitle || task.dueDate != dueDate ->
                repository.saveTask(task.copy(title = trimmedTitle, dueDate = dueDate))
            else -> Unit // unchanged, nothing to save
        }
    }

    fun delete() = finish {
        existingTask?.let { repository.deleteTask(it) }
    }

    // Runs the database work before signalling the screen to close. The guard stops
    // a double tap on back from saving a new task twice.
    private fun finish(action: suspend () -> Unit) {
        if (isClosing) return
        isClosing = true
        viewModelScope.launch {
            action()
            isFinished = true
        }
    }

    companion object {
        const val TASK_ID_ARG = "taskId"
        const val NEW_TASK_ID = -1L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotesApp
                TaskEditViewModel(createSavedStateHandle(), app.taskRepository)
            }
        }
    }
}
