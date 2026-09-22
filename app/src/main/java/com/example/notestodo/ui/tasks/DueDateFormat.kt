package com.example.notestodo.ui.tasks

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val sameYearFormat = DateTimeFormatter.ofPattern("EEE, d MMM")
private val otherYearFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

// "Today", "Tomorrow", "Yesterday", otherwise e.g. "Mon, 29 Sep" (with the year if it isn't this year).
fun formatDueDate(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(if (date.year == today.year) sameYearFormat else otherYearFormat)
}
