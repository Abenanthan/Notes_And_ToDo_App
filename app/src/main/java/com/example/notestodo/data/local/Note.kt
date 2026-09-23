package com.example.notestodo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    // defaultValue lets AutoMigration fill these in for notes that already exist.
    @ColumnInfo(defaultValue = "0") val isFavourite: Boolean = false,
    @ColumnInfo(defaultValue = "0") val colorIndex: Int = 0, // 0 = the theme's own card colour
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
