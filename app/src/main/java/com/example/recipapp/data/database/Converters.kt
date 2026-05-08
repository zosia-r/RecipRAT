package com.example.recipapp.data.database

import androidx.room.TypeConverter

/**
 * Konwertuje List<String> ↔ String do zapisu w SQLite.
 * Używany zarówno dla tagów jak i dla zdjęć/składników w innych encjach.
 */
class Converters {

    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString(separator = "|||")

    @TypeConverter
    fun toList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||")
    }
}