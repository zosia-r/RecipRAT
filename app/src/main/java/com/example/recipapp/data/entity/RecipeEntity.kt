package com.example.recipapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.recipapp.data.database.Converters

@Entity(tableName = "recipes")
@TypeConverters(Converters::class)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val executionDescription: String,
    val isFavourite: Boolean = false,
    val tags: List<String> = emptyList()   // przechowujemy nazwy enumów np. ["BREAKFAST", "SWEET"]
)