package com.example.notestodo.ui.trash

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notestodo.NotesApp
import com.example.notestodo.ui.common.EmptyMessage
import com.example.notestodo.viewmodel.TrashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit,
    viewModel: TrashViewModel = viewModel(factory = TrashViewModel.Factory),
) {
    val notes = viewModel.deletedNotes.collectAsStateWithLifecycle().value
    val tasks = viewModel.deletedTasks.collectAsStateWithLifecycle().value
    var showEmptyDialog by rememberSaveable { mutableStateOf(false) }

    val isLoaded = notes != null && tasks != null
    val isEmpty = notes.orEmpty().isEmpty() && tasks.orEmpty().isEmpty()
    val retentionDays = NotesApp.TRASH_RETENTION.inWholeDays

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trash") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEmpty) {
                        TextButton(onClick = { showEmptyDialog = true }) { Text("Empty") }
                    }
                },
            )
        },
    ) { padding ->
        when {
            !isLoaded -> Unit // first load still in progress
            isEmpty -> EmptyMessage(
                "Trash is empty.\nDeleted notes and tasks wait here for $retentionDays days.",
                Modifier.padding(padding),
            )
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                item(key = "retention_note") {
                    Text(
                        text = "Items are deleted for good after $retentionDays days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    )
                }

                val deletedNotes = notes.orEmpty()
                if (deletedNotes.isNotEmpty()) {
                    item(key = "notes_header") { SectionHeader("Notes") }
                    items(deletedNotes, key = { "note_${it.id}" }) { note ->
                        TrashRow(
                            title = note.title.ifBlank { note.content }.ifBlank { "Empty note" },
                            deletedAt = note.deletedAt,
                            onRestore = { viewModel.restoreNote(note) },
                            onDeleteForever = { viewModel.deleteNoteForever(note) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                val deletedTasks = tasks.orEmpty()
                if (deletedTasks.isNotEmpty()) {
                    item(key = "tasks_header") { SectionHeader("Tasks") }
                    items(deletedTasks, key = { "task_${it.id}" }) { task ->
                        TrashRow(
                            title = task.title,
                            deletedAt = task.deletedAt,
                            onRestore = { viewModel.restoreTask(task) },
                            onDeleteForever = { viewModel.deleteTaskForever(task) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("Empty trash?") },
            text = { Text("Everything in the trash will be deleted permanently.") },
            confirmButton = {
                TextButton(onClick = {
                    showEmptyDialog = false
                    viewModel.emptyTrash()
                }) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun TrashRow(
    title: String,
    deletedAt: Long?,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(title, maxLines = 1) },
        modifier = modifier,
        supportingContent = if (deletedAt != null) {
            { Text("Deleted ${DateUtils.getRelativeTimeSpanString(deletedAt)}") }
        } else null,
        trailingContent = {
            Row {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restore")
                }
                IconButton(onClick = onDeleteForever) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete permanently")
                }
            }
        },
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}
