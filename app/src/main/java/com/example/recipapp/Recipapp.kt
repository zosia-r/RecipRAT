package com.example.recipapp

import android.app.Application
import com.example.recipapp.data.RecipeRepository
import com.example.recipapp.data.database.RecipeDatabase

class Recipapp : Application() {
    val database by lazy { RecipeDatabase.getDatabase(this) }
    val repository by lazy { RecipeRepository(database.recipeDao()) }
}