package com.example.notestodo.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notestodo.data.local.Task
import com.example.notestodo.ui.common.EmptyMessage
import com.example.notestodo.ui.tasks.TaskRow
import com.example.notestodo.ui.tasks.formatDueDate
import com.example.notestodo.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onTaskClick: (Long) -> Unit,
    onAddTask: (LocalDate) -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory),
) {
    val monthTasks = viewModel.monthTasks.collectAsStateWithLifecycle().value
    val selectedDate = viewModel.selectedDate
    val tasksByDate = remember(monthTasks) { monthTasks.orEmpty().groupBy { it.dueDate } }
    val tasksOnSelectedDay = tasksByDate[selectedDate].orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                actions = {
                    TextButton(onClick = viewModel::showToday) { Text("Today") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTask(selectedDate) }) {
                Icon(Icons.Default.Add, contentDescription = "Add task on this day")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            MonthHeader(
                month = viewModel.displayedMonth,
                onPreviousMonth = viewModel::showPreviousMonth,
                onNextMonth = viewModel::showNextMonth,
            )
            MonthGrid(
                month = viewModel.displayedMonth,
                selectedDate = selectedDate,
                tasksByDate = tasksByDate,
                onDateClick = viewModel::onDateSelected,
            )

            HorizontalDivider(Modifier.padding(top = 8.dp))
            Text(
                text = formatDueDate(selectedDate),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
            )

            if (tasksOnSelectedDay.isEmpty()) {
                EmptyMessage("No tasks on this day.\nTap + to add one.")
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) { // room for the FAB
                    items(tasksOnSelectedDay, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onCheckedChange = { viewModel.onTaskCheckedChange(task, it) },
                            onClick = { onTaskClick(task.id) },
                            modifier = Modifier.animateItem(),
                            // Every task here is due on the selected day, so the date would just repeat.
                            showDueDate = false,
                        )
                    }
                }
            }
        }
    }
}

private val monthFormat = DateTimeFormatter.ofPattern("MMMM yyyy")

@Composable
private fun MonthHeader(month: YearMonth, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Text(
            text = month.format(monthFormat),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate?, List<Task>>,
    onDateClick: (LocalDate) -> Unit,
) {
    // Which day the week starts on depends on the locale (Sunday in India, Monday in the UK).
    val firstDayOfWeek = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }
    val today = remember { LocalDate.now() }
    val daysInMonth = month.lengthOfMonth()
    // How many empty cells go before the 1st, so it lands under the right weekday.
    val leadingBlanks = remember(month, firstDayOfWeek) {
        (month.atDay(1).dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    }
    val weekCount = (leadingBlanks + daysInMonth + 6) / 7

    Column(Modifier.padding(horizontal = 8.dp)) {
        Row {
            repeat(7) { index ->
                Text(
                    text = firstDayOfWeek.plus(index.toLong())
                        .getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        repeat(weekCount) { week ->
            Row {
                repeat(7) { weekday ->
                    val dayOfMonth = week * 7 + weekday - leadingBlanks + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val date = month.atDay(dayOfMonth)
                        val tasks = tasksByDate[date].orEmpty()
                        DayCell(
                            dayOfMonth = dayOfMonth,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            hasTasks = tasks.isNotEmpty(),
                            allTasksDone = tasks.isNotEmpty() && tasks.all { it.isDone },
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayOfMonth: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasTasks: Boolean,
    allTasksDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    // A dot marks days that have tasks, faded once every task on that day is done.
    val dotColor = when {
        !hasTasks -> Color.Transparent
        allTasksDone -> contentColor.copy(alpha = 0.35f)
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}
