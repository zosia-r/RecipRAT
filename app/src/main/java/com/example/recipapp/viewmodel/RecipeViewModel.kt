package com.example.recipapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipapp.data.entity.IngredientEntity
import com.example.recipapp.data.entity.PhotoEntity
import com.example.recipapp.data.entity.RecipeEntity
import com.example.recipapp.data.relation.RecipeWithDetails
import com.example.recipapp.data.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RecipeViewModel(
    application: Application,
    private val repository: RecipeRepository
) : AndroidViewModel(application) {

    val allRecipes: StateFlow<List<RecipeWithDetails>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favouriteRecipes: StateFlow<List<RecipeWithDetails>> = repository.favouriteRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Wyszukiwanie ─────────────────────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<RecipeWithDetails>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allRecipes
            else repository.searchRecipes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // ── Akcje ────────────────────────────────────────────────────────────────

    fun addRecipe(
        title: String,
        description: String,
        executionDescription: String,
        ingredients: List<String>,
        photoUris: List<Uri>
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

            val photoEntities = photoUris.map { uri ->
                val savedPath = copyPhotoToAppStorage(getApplication(), uri)
                PhotoEntity(recipeId = 0, uri = savedPath)
            }

            repository.insertFullRecipe(recipe, ingredientEntities, photoEntities)
        }
    }

    fun updateRecipe(
        recipe: RecipeEntity,
        ingredients: List<String>,
        newPhotoUris: List<Uri> = emptyList(),
        removedPhotoPaths: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()

            // Usuń pliki zdjęć skasowanych przez użytkownika
            removedPhotoPaths.forEach { path -> File(path).takeIf { it.exists() }?.delete() }

            // Skopiuj nowe zdjęcia
            val newPhotoEntities = newPhotoUris.map { uri ->
                val savedPath = copyPhotoToAppStorage(context, uri)
                PhotoEntity(recipeId = recipe.id, uri = savedPath)
            }

            // Składniki jako encje
            val ingredientEntities = ingredients
                .filter { it.isNotBlank() }
                .map { IngredientEntity(recipeId = recipe.id, name = it, amount = "") }

            repository.updateRecipe(recipe, ingredientEntities, newPhotoEntities, removedPhotoPaths)
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

    private fun copyPhotoToAppStorage(context: Context, uri: Uri): String {
        val dir = File(context.filesDir, "recipe_photos").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}