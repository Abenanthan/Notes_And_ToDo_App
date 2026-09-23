package com.example.notestodo.ui.tasks

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notestodo.ui.common.EmptyMessage
import com.example.notestodo.ui.common.SettingsAction
import com.example.notestodo.viewmodel.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onTaskClick: (Long) -> Unit,
    onAddTask: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: TaskListViewModel = viewModel(factory = TaskListViewModel.Factory),
) {
    val tasks = viewModel.tasks.collectAsStateWithLifecycle().value
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = { SettingsAction(onOpenSettings) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        when {
            tasks == null -> Unit // first load still in progress
            tasks.isEmpty() -> EmptyMessage("No tasks yet.\nTap + to add one.", Modifier.padding(padding))
            else -> {
                // The DAO already puts unfinished tasks first, so partition keeps each group's order.
                val (pending, completed) = remember(tasks) { tasks.partition { !it.isDone } }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 88.dp), // room for the FAB
                ) {
                    items(pending, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onCheckedChange = { viewModel.onTaskCheckedChange(task, it) },
                            onClick = { onTaskClick(task.id) },
                            // Slides the row to its new place when it is ticked or unticked.
                            modifier = Modifier.animateItem(),
                        )
                    }
                    if (completed.isNotEmpty()) {
                        item(key = "completed_header") {
                            CompletedHeader(
                                count = completed.size,
                                onClear = { showClearDialog = true },
                                modifier = Modifier.animateItem(),
                            )
                        }
                        items(completed, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onCheckedChange = { viewModel.onTaskCheckedChange(task, it) },
                                onClick = { onTaskClick(task.id) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Delete completed tasks?") },
            text = { Text("All completed tasks will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    viewModel.clearCompleted()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun CompletedHeader(count: Int, onClear: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Completed ($count)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onClear) { Text("Clear") }
    }
}
