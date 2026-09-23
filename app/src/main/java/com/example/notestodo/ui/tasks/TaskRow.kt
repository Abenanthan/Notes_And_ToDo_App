package com.example.notestodo.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import com.example.notestodo.data.local.Task
import java.time.LocalDate

// One task line: checkbox, title, and the due date underneath. Shared by the task list
// and the calendar, which hides the date because every task shown there has the same one.
@Composable
fun TaskRow(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDueDate: Boolean = true,
) {
    val dueDate = task.dueDate
    ListItem(
        headlineContent = {
            Text(
                text = task.title,
                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else Color.Unspecified,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
            )
        },
        modifier = modifier.clickable(onClick = onClick),
        supportingContent = if (dueDate != null && showDueDate) {
            {
                // Overdue dates turn red until the task is done.
                val isOverdue = !task.isDone && dueDate.isBefore(LocalDate.now())
                Text(
                    text = formatDueDate(dueDate),
                    color = if (isOverdue) MaterialTheme.colorScheme.error else Color.Unspecified,
                )
            }
        } else null,
        leadingContent = { Checkbox(checked = task.isDone, onCheckedChange = onCheckedChange) },
    )
}
