package com.example.notestodo.ui.tasks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notestodo.reminder.canScheduleExactReminders
import com.example.notestodo.reminder.exactAlarmSettingsIntent
import com.example.notestodo.ui.common.plainTextFieldColors
import com.example.notestodo.viewmodel.TaskEditViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    onBack: () -> Unit,
    viewModel: TaskEditViewModel = viewModel(factory = TaskEditViewModel.Factory),
) {
    val context = LocalContext.current
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showExactAlarmDialog by rememberSaveable { mutableStateOf(false) }

    // Android 13+ asks before an app may post notifications. Once that is answered, check
    // whether reminders are also allowed to be punctual.
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (!canScheduleExactReminders(context)) showExactAlarmDialog = true
    }

    // Both prompts only make sense the moment a reminder is actually set.
    fun onReminderSet() {
        val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        when {
            needsNotificationPermission ->
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            !canScheduleExactReminders(context) -> showExactAlarmDialog = true
        }
    }

    // Navigate away only after the ViewModel has finished writing to the database.
    LaunchedEffect(viewModel.isFinished) {
        if (viewModel.isFinished) onBack()
    }
    // The system back button/gesture saves too, not just the arrow in the top bar.
    BackHandler(onBack = viewModel::saveAndClose)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = viewModel::saveAndClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Save and go back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Move task to trash")
                    }
                },
            )
        },
        // safeDrawing includes the keyboard, so the editor shrinks above it instead of hiding behind it.
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        val titleFocus = remember { FocusRequester() }
        val dueDate = viewModel.dueDate
        val dueTime = viewModel.dueTime

        Column(Modifier.fillMaxSize().padding(padding)) {
            TextField(
                value = viewModel.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
                placeholder = { Text("What needs to be done?", style = MaterialTheme.typography.titleLarge) },
                textStyle = MaterialTheme.typography.titleLarge,
                colors = plainTextFieldColors(),
                // The keyboard shows "Done" instead of Enter; pressing it saves the task, for quick adding.
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.saveAndClose() }),
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { showDatePicker = true },
                        label = { Text(if (dueDate == null) "Add due date" else formatDueDate(dueDate)) },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    )
                    if (dueDate != null) {
                        IconButton(onClick = { viewModel.onDueDateChange(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove due date")
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { showTimePicker = true },
                        // A reminder needs a day to fire on.
                        enabled = dueDate != null,
                        label = { Text(if (dueTime == null) "Add reminder" else formatDueTime(dueTime)) },
                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    )
                    if (dueTime != null) {
                        IconButton(onClick = { viewModel.onDueTimeChange(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove reminder")
                        }
                    }
                }
            }
        }

        // Open the keyboard straight away when adding a new task.
        LaunchedEffect(Unit) {
            if (viewModel.isNewTask) titleFocus.requestFocus()
        }
    }

    if (showDatePicker) {
        DueDatePickerDialog(
            initialDate = viewModel.dueDate,
            onDateSelected = {
                viewModel.onDueDateChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialTime = viewModel.dueTime,
            onTimeSelected = {
                viewModel.onDueTimeChange(it)
                showTimePicker = false
                onReminderSet()
            },
            onDismiss = { showTimePicker = false },
        )
    }

    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmDialog = false },
            title = { Text("Allow exact reminders?") },
            text = {
                Text(
                    "Android may hold reminders back by a few minutes unless this app is " +
                        "allowed to set exact alarms."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmDialog = false
                    context.startActivity(exactAlarmSettingsIntent(context))
                }) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmDialog = false }) { Text("Not now") }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Move task to trash?") },
            text = { Text("You can restore it from Settings \u2192 Trash for 30 days.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) { Text("Move to trash") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate?.toPickerMillis())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pickerState.selectedDateMillis?.let { onDateSelected(it.toPickerDate()) } },
                enabled = pickerState.selectedDateMillis != null,
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

// Material3 1.3 has the clock face but no ready-made dialog around it, so it goes in an AlertDialog.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    // A fresh reminder starts at the next full hour rather than at midnight.
    val startFrom = initialTime ?: LocalTime.now().plusHours(1).withMinute(0)
    val pickerState = rememberTimePickerState(
        initialHour = startFrom.hour,
        initialMinute = startFrom.minute,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remind me at") },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(pickerState.hour, pickerState.minute))
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

// Material's DatePicker works in UTC milliseconds. Converting with UTC both ways stops
// the chosen day from shifting by one in time zones behind UTC.
private fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
