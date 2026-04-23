package com.example.recipapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.recipapp.data.local.dao.RecipeDao
import com.example.recipapp.data.local.entity.IngredientEntity
import com.example.recipapp.data.local.entity.PhotoEntity
import com.example.recipapp.data.local.entity.RecipeEntity

@Database(
    entities = [RecipeEntity::class, IngredientEntity::class, PhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getInstance(context: Context): RecipeDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                ).build().also { INSTANCE = it }
            }
    }
}