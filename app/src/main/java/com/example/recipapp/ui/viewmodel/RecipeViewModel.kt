package com.example.recipapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipapp.data.local.entity.IngredientEntity
import com.example.recipapp.data.local.entity.PhotoEntity
import com.example.recipapp.data.local.entity.RecipeEntity
import com.example.recipapp.data.local.relation.RecipeWithDetails
import com.example.recipapp.data.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    val allRecipes: StateFlow<List<RecipeWithDetails>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favouriteRecipes: StateFlow<List<RecipeWithDetails>> = repository.favouriteRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecipe(
        title: String,
        description: String,
        executionDescription: String,
        ingredients: List<String>,
        photoUris: List<String>
    ) {
        viewModelScope.launch {
            val recipe = RecipeEntity(
                title = title,
                description = description,
                executionDescription = executionDescription
            )
            val ingredientEntities = ingredients
                .filter { it.isNotBlank() }
                .map { IngredientEntity(recipeId = 0, name = it, amount = "") }
            val photoEntities = photoUris
                .map { PhotoEntity(recipeId = 0, uri = it) }

            repository.insertFullRecipe(recipe, ingredientEntities, photoEntities)
        }
    }

    fun toggleFavourite(id: Long, current: Boolean) {
        viewModelScope.launch {
            repository.toggleFavourite(id, !current)
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }

    fun getRecipeById(id: Long): Flow<RecipeWithDetails?> =
        repository.getRecipeById(id)
}