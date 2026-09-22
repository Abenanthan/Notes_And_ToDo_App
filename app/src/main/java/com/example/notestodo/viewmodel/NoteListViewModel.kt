package com.example.notestodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notestodo.NotesApp
import com.example.notestodo.data.local.Note
import com.example.notestodo.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class NoteListViewModel(private val repository: NoteRepository) : ViewModel() {

    // Compose state rather than StateFlow: a text field bound to a Flow can lag
    // behind fast typing and make the cursor jump.
    var query by mutableStateOf("")
        private set

    // Re-runs the search whenever the query changes. Room re-emits on its own
    // whenever the notes table changes. null means the first result hasn't loaded yet.
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>?> = snapshotFlow { query.trim() }
        .flatMapLatest { repository.searchNotes(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotesApp
                NoteListViewModel(app.noteRepository)
            }
        }
    }
}
