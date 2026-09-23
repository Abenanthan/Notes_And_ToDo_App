package com.example.notestodo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueDate: LocalDate? = null, // stored as a day number, see Converters
    val dueTime: LocalTime? = null, // set a time as well and the task gets a reminder
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
