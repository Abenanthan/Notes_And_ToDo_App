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
import com.example.notestodo.ui.notes.markdownToPlainText
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
    // The body as loaded from the database; the editor seeds itself from this and owns
    // the live text from then on, handing it back when the note is saved.
    var content by mutableStateOf("")
        private set

    // The editor can only load the note once this is true.
    var isLoaded by mutableStateOf(false)
        private set

    // Shown in the line under the title; a new note counts as written now.
    var updatedAt by mutableStateOf(System.currentTimeMillis())
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
        // A new note has nothing to fetch, so the editor can start straight away.
        isLoaded = isNewNote
        if (!isNewNote) {
            viewModelScope.launch {
                existingNote = repository.getNote(noteId)?.also {
                    title = it.title
                    content = it.content
                    isFavourite = it.isFavourite
                    colorIndex = it.colorIndex
                    updatedAt = it.updatedAt
                }
                isLoaded = true
            }
        }
    }

    fun onTitleChange(value: String) {
        title = value
    }

    // The editor reports the text it actually produced after loading. Markdown can come
    // back slightly reformatted, and that alone should not count as an edit.
    fun onEditorLoaded(markdown: String) {
        content = markdown
    }

    fun onFavouriteToggle() {
        isFavourite = !isFavourite
    }

    fun onColorSelected(index: Int) {
        colorIndex = index
    }

    // Auto-save when leaving the screen, like Google Keep. The body comes from the
    // editor, which holds the live text.
    fun saveAndClose(body: String) = finish {
        val note = existingNote
        // Markdown leftovers such as a stray "- " should not count as content.
        val plainBody = markdownToPlainText(body)
        when {
            // Clearing a note completely removes it; a blank new note is never saved.
            // An emptied note has nothing worth restoring, so it skips the trash.
            title.isBlank() && plainBody.isBlank() -> note?.let { repository.deleteNoteForever(it) }
            note == null -> repository.saveNote(
                Note(title = title, content = body, isFavourite = isFavourite, colorIndex = colorIndex)
            )
            // Only touch updatedAt if something actually changed, so just opening
            // a note doesn't move it to the top of the list.
            note.title != title || body != content ||
                note.isFavourite != isFavourite || note.colorIndex != colorIndex -> repository.saveNote(
                note.copy(
                    title = title,
                    content = body,
                    isFavourite = isFavourite,
                    colorIndex = colorIndex,
                    updatedAt = System.currentTimeMillis(),
                )
            )
            else -> Unit // unchanged, nothing to save
        }
    }

    fun delete() = finish {
        existingNote?.let { repository.moveNoteToTrash(it) }
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
