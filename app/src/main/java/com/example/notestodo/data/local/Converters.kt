package com.example.notestodo.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

// SQLite has no date type, so Room stores a LocalDate as its "epoch day"
// (days since 1 Jan 1970). Plain numbers also sort and compare correctly in SQL.
class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()
}
