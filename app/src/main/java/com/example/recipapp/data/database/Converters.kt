package com.example.recipapp.data.database

import androidx.room.TypeConverter
import com.example.recipapp.data.entity.RecipeTag

/**
 * Converts List<String> to String and vice versa for SQLite.
 */
class Converters {

    // List<String>
    @TypeConverter
    fun fromList(list: List<String>): String =
        list.joinToString(separator = "|||")

    @TypeConverter
    fun toList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||")
    }

    // List<RecipeTag>
    @TypeConverter
    fun fromTagList(tags: List<RecipeTag>): String =
        tags.joinToString(separator = "|||") { it.name }

    @TypeConverter
    fun toTagList(value: String): List<RecipeTag> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||").mapNotNull { name ->
            runCatching { RecipeTag.valueOf(name) }.getOrNull()
        }
    }
}