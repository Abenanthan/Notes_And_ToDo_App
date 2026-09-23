package com.example.notestodo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notestodo.NotesApp
import com.example.notestodo.data.local.Note
import com.example.notestodo.data.local.Task
import com.example.notestodo.data.repository.NoteRepository
import com.example.notestodo.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// The one screen that touches both repositories, because the trash holds both kinds of item.
class TrashViewModel(
    private val noteRepository: NoteRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    val deletedNotes: StateFlow<List<Note>?> = noteRepository.getDeletedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val deletedTasks: StateFlow<List<Task>?> = taskRepository.getDeletedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun restoreNote(note: Note) {
        viewModelScope.launch { noteRepository.restoreNote(note) }
    }

    fun deleteNoteForever(note: Note) {
        viewModelScope.launch { noteRepository.deleteNoteForever(note) }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch { taskRepository.restoreTask(task) }
    }

    fun deleteTaskForever(task: Task) {
        viewModelScope.launch { taskRepository.deleteTaskForever(task) }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyNoteTrash()
            taskRepository.emptyTaskTrash()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotesApp
                TrashViewModel(app.noteRepository, app.taskRepository)
            }
        }
    }
}
