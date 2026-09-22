package com.example.notestodo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueDate: LocalDate? = null, // stored as a day number, see Converters
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
