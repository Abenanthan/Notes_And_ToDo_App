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
import com.example.notestodo.data.local.Note
import com.example.notestodo.data.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository,
) : ViewModel() {

    // Navigation puts the "noteId" route argument into the SavedStateHandle.
    private val noteId: Long = savedStateHandle[NOTE_ID_ARG] ?: NEW_NOTE_ID
    val isNewNote = noteId == NEW_NOTE_ID

    // The note as it was loaded from the database; stays null for a new note.
    private var existingNote: Note? = null

    var title by mutableStateOf("")
        private set
    var content by mutableStateOf("")
        private set
    var isFavourite by mutableStateOf(false)
        private set
    var colorIndex by mutableStateOf(0)
        private set

    // Becomes true after the save/delete has finished; the screen then navigates back.
    var isFinished by mutableStateOf(false)
        private set
    private var isClosing = false

    init {
        if (!isNewNote) {
            viewModelScope.launch {
                existingNote = repository.getNote(noteId)?.also {
                    title = it.title
                    content = it.content
                    isFavourite = it.isFavourite
                    colorIndex = it.colorIndex
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        title = value
    }

    fun onContentChange(value: String) {
        content = value
    }

    fun onFavouriteToggle() {
        isFavourite = !isFavourite
    }

    fun onColorSelected(index: Int) {
        colorIndex = index
    }

    // Auto-save when leaving the screen, like Google Keep.
    fun saveAndClose() = finish {
        val note = existingNote
        when {
            // Clearing a note completely removes it; a blank new note is never saved.
            title.isBlank() && content.isBlank() -> note?.let { repository.deleteNote(it) }
            note == null -> repository.saveNote(
                Note(title = title, content = content, isFavourite = isFavourite, colorIndex = colorIndex)
            )
            // Only touch updatedAt if something actually changed, so just opening
            // a note doesn't move it to the top of the list.
            note.title != title || note.content != content ||
                note.isFavourite != isFavourite || note.colorIndex != colorIndex -> repository.saveNote(
                note.copy(
                    title = title,
                    content = content,
                    isFavourite = isFavourite,
                    colorIndex = colorIndex,
                    updatedAt = System.currentTimeMillis(),
                )
            )
            else -> Unit // unchanged, nothing to save
        }
    }

    fun delete() = finish {
        existingNote?.let { repository.deleteNote(it) }
    }

    // Runs the database work before signalling the screen to close. The guard stops
    // a double tap on back from saving a new note twice.
    private fun finish(action: suspend () -> Unit) {
        if (isClosing) return
        isClosing = true
        viewModelScope.launch {
            action()
            isFinished = true
        }
    }

    companion object {
        const val NOTE_ID_ARG = "noteId"
        const val NEW_NOTE_ID = -1L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotesApp
                NoteEditViewModel(createSavedStateHandle(), app.noteRepository)
            }
        }
    }
}
