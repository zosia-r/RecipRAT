package com.example.recipapp.sharing

import com.example.recipapp.data.entity.RecipeTag
import com.example.recipapp.viewmodel.IngredientInput

data class ParsedRecipe(
    val title: String,
    val description: String,
    val ingredients: List<IngredientInput>,
    val steps: String,
    val tags: List<RecipeTag>
)
