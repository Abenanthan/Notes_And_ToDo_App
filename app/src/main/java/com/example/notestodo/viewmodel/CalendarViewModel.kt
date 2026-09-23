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
import com.example.notestodo.data.local.Task
import com.example.notestodo.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(private val repository: TaskRepository) : ViewModel() {

    var displayedMonth by mutableStateOf(YearMonth.now())
        private set
    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    // Only the month on screen is loaded, and it reloads when the user pages to another
    // month. The screen groups these by date to draw the dots and the day's task list.
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthTasks: StateFlow<List<Task>?> = snapshotFlow { displayedMonth }
        .flatMapLatest { repository.getTasksBetween(it.atDay(1), it.atEndOfMonth()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onDateSelected(date: LocalDate) {
        selectedDate = date
    }

    fun showPreviousMonth() = showMonth(displayedMonth.minusMonths(1))

    fun showNextMonth() = showMonth(displayedMonth.plusMonths(1))

    fun showToday() {
        displayedMonth = YearMonth.now()
        selectedDate = LocalDate.now()
    }

    fun onTaskCheckedChange(task: Task, isDone: Boolean) {
        viewModelScope.launch { repository.saveTask(task.copy(isDone = isDone)) }
    }

    private fun showMonth(month: YearMonth) {
        displayedMonth = month
        // Keep the selected day inside the month on screen, so the list below the grid
        // always matches a highlighted day.
        val today = LocalDate.now()
        selectedDate = if (YearMonth.from(today) == month) today else month.atDay(1)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotesApp
                CalendarViewModel(app.taskRepository)
            }
        }
    }
}
