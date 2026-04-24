package com.example.recipapp

import android.app.Application
import com.example.recipapp.data.local.RecipeDatabase
import com.example.recipapp.data.repository.RecipeRepository

class Recipapp : Application() {
    val database by lazy { RecipeDatabase.getInstance(this) }
    val repository by lazy { RecipeRepository(database.recipeDao()) }
}