package com.example.recipapp.data.database

import androidx.room.TypeConverter
import com.example.recipapp.data.entity.RecipeTag

class Converters {

    // List<String> -> String
    @TypeConverter
    fun fromList(list: List<String>): String =
        list.joinToString(separator = "|||")

    // String -> List<String>
    @TypeConverter
    fun toList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||")
    }

    // List<RecipeTag> -> String
    @TypeConverter
    fun fromTagList(tags: List<RecipeTag>): String =
        tags.joinToString(separator = "|||") { it.name }

    // String -> List<RecipeTag>
    @TypeConverter
    fun toTagList(value: String): List<RecipeTag> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||").mapNotNull { name ->
            // safe cast to RecipeTag (like try/catch)
            runCatching { RecipeTag.valueOf(name) }.getOrNull()
        }
    }
}