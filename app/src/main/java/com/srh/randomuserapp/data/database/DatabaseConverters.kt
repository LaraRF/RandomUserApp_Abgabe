package com.srh.randomuserapp.data.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room database.
 * Handles conversion between complex types and primitive types for database storage.
 */
class DatabaseConverters {

    /**
     * Converts Date object to Long timestamp for database storage
     * @param date Date object to convert
     * @return Long timestamp or null
     */
    @TypeConverter
    fun fromDateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts Long timestamp to Date object
     * @param timestamp Long timestamp from database
     * @return Date object or null
     */
    @TypeConverter
    fun fromTimestampToDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    /**
     * Converts List<String> to comma-separated string for database storage
     * @param list List of strings to convert
     * @return Comma-separated string or null
     */
    @TypeConverter
    fun fromStringListToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    /**
     * Converts comma-separated string to List<String>
     * @param data Comma-separated string from database
     * @return List of strings or empty list
     */
    @TypeConverter
    fun fromStringToStringList(data: String?): List<String> {
        return if (data.isNullOrEmpty()) {
            emptyList()
        } else {
            data.split(",").map { it.trim() }
        }
    }
}