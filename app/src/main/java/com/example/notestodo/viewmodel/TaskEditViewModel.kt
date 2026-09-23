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
import java.time.LocalTime

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

    // A time turns the due date into a reminder; without a date it means nothing.
    var dueTime by mutableStateOf<LocalTime?>(null)
        private set

    // Becomes true after the save/delete has finished; the screen then navigates back.
    var isFinished by mutableStateOf(false)
        private set
    private var isClosing = false

    init {
        if (isNewTask) {
            // The calendar opens this screen with the day the user tapped already filled in.
            val presetDueDate: Long = savedStateHandle[DUE_DATE_ARG] ?: NO_DUE_DATE
            if (presetDueDate != NO_DUE_DATE) dueDate = LocalDate.ofEpochDay(presetDueDate)
        } else {
            viewModelScope.launch {
                existingTask = repository.getTask(taskId)?.also {
                    title = it.title
                    dueDate = it.dueDate
                    dueTime = it.dueTime
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        title = value
    }

    fun onDueDateChange(date: LocalDate?) {
        dueDate = date
        // Clearing the day leaves nothing for a reminder to fire on.
        if (date == null) dueTime = null
    }

    fun onDueTimeChange(time: LocalTime?) {
        dueTime = time
    }

    fun saveAndClose() = finish {
        val task = existingTask
        val trimmedTitle = title.trim()
        when {
            // A task needs a title: clearing it removes the task, and a blank new task is never saved.
            // A task with its title erased has nothing worth restoring.
            trimmedTitle.isEmpty() -> task?.let { repository.deleteTaskForever(it) }
            task == null -> repository.saveTask(Task(title = trimmedTitle, dueDate = dueDate, dueTime = dueTime))
            task.title != trimmedTitle || task.dueDate != dueDate || task.dueTime != dueTime ->
                repository.saveTask(task.copy(title = trimmedTitle, dueDate = dueDate, dueTime = dueTime))
            else -> Unit // unchanged, nothing to save
        }
    }

    fun delete() = finish {
        existingTask?.let { repository.moveTaskToTrash(it) }
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

        // Optional route argument: the due date to start with, as an epoch day.
        // -1 (31 Dec 1969) stands for "no date given" - nobody sets a task for then.
        const val DUE_DATE_ARG = "dueDate"
        const val NO_DUE_DATE = -1L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotesApp
                TaskEditViewModel(createSavedStateHandle(), app.taskRepository)
            }
        }
    }
}
