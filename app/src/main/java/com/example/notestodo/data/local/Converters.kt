package com.example.notestodo.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

// SQLite has no date type, so Room stores a LocalDate as its "epoch day"
// (days since 1 Jan 1970). Plain numbers also sort and compare correctly in SQL.
class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    // A LocalTime is stored the same way: as minutes since midnight.
    @TypeConverter
    fun fromMinuteOfDay(value: Int?): LocalTime? = value?.let { LocalTime.of(it / 60, it % 60) }

    @TypeConverter
    fun toMinuteOfDay(time: LocalTime?): Int? = time?.let { it.hour * 60 + it.minute }
}
