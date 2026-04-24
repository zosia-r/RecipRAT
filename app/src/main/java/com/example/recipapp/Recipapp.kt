package com.example.recipapp

import android.app.Application
import com.example.recipapp.data.database.RecipeDatabase
import com.example.recipapp.data.RecipeRepository

class Recipapp : Application() {
    val database by lazy { RecipeDatabase.getInstance(this) }
    val repository by lazy { RecipeRepository(database.recipeDao()) }
}