package com.example.recipapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipapp.data.RecipeTag
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

    // ── Wyszukiwanie + filtrowanie po tagu ───────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<RecipeTag?>(null)
    val selectedTag: StateFlow<RecipeTag?> = _selectedTag.asStateFlow()

    // Łączymy query i tag w jeden strumień – reaguje na zmiany obu
    val searchResults: StateFlow<List<RecipeWithDetails>> =
        combine(_searchQuery, _selectedTag) { query, tag -> query to tag }
            .debounce(300)
            .flatMapLatest { (query, tag) ->
                val tagName = tag?.name ?: ""   // nazwa enuma np. "BREAKFAST", "" gdy brak filtra
                repository.searchRecipesWithTag(query, tagName)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun onTagSelected(tag: RecipeTag?) { _selectedTag.value = tag }

    // ── Akcje ────────────────────────────────────────────────────────────────

    fun addRecipe(
        title: String,
        description: String,
        executionDescription: String,
        ingredients: List<String>,
        photoUris: List<Uri>,
        tags: List<RecipeTag> = emptyList()
    ) {
        viewModelScope.launch {
            val recipe = RecipeEntity(
                title                = title,
                description          = description,
                executionDescription = executionDescription,
                tags                 = tags.map { it.name }   // zapisujemy nazwy enumów
            )
            val ingredientEntities = ingredients
                .filter { it.isNotBlank() }
                .map { IngredientEntity(recipeId = 0, name = it, amount = "") }
            val photoEntities = photoUris.map { uri ->
                PhotoEntity(recipeId = 0, uri = copyPhotoToAppStorage(getApplication(), uri))
            }
            repository.insertFullRecipe(recipe, ingredientEntities, photoEntities)
        }
    }

    fun updateRecipe(
        recipe: RecipeEntity,
        ingredients: List<String>,
        newPhotoUris: List<Uri> = emptyList(),
        removedPhotoPaths: List<String> = emptyList(),
        tags: List<RecipeTag> = emptyList()
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            removedPhotoPaths.forEach { path -> File(path).takeIf { it.exists() }?.delete() }
            val newPhotoEntities = newPhotoUris.map { uri ->
                PhotoEntity(recipeId = recipe.id, uri = copyPhotoToAppStorage(context, uri))
            }
            val ingredientEntities = ingredients
                .filter { it.isNotBlank() }
                .map { IngredientEntity(recipeId = recipe.id, name = it, amount = "") }
            repository.updateRecipe(
                recipe.copy(tags = tags.map { it.name }),
                ingredientEntities,
                newPhotoEntities,
                removedPhotoPaths
            )
        }
    }

    fun toggleFavourite(id: Long, current: Boolean) {
        viewModelScope.launch { repository.toggleFavourite(id, !current) }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch { repository.deleteRecipe(recipe) }
    }

    fun getRecipeById(id: Long): Flow<RecipeWithDetails?> = repository.getRecipeById(id)

    private fun copyPhotoToAppStorage(context: Context, uri: Uri): String {
        val dir  = File(context.filesDir, "recipe_photos").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}