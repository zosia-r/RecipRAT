package com.example.recipapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipapp.data.RecipeRepository
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.entity.IngredientEntity
import com.example.recipapp.data.entity.PhotoEntity
import com.example.recipapp.data.entity.RecipeEntity
import com.example.recipapp.data.relation.RecipeWithDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

// Klasa pomocnicza łącząca dane z pól tekstowych formularza w UI
data class IngredientInput(val name: String, val amount: String, val unit: String)

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

    private val _selectedTags = MutableStateFlow<Set<RecipeTag>>(emptySet())
    val selectedTags: StateFlow<Set<RecipeTag>> = _selectedTags.asStateFlow()

    // Szybkie wyszukiwanie delegowane bezpośrednio do silnika bazy danych SQLite
    val searchResults: StateFlow<List<RecipeWithDetails>> =
        combine(_searchQuery, _selectedTags) { query, tags -> query to tags }
            .debounce(300)
            .flatMapLatest { (query, tags) ->
                if (tags.isEmpty()) {
                    repository.searchRecipes(query)
                } else {
                    // Pobieramy dane wstępnie przefiltrowane w SQL po pierwszym tagu,
                    // a ewentualne pozostałe tagi dociskamy bezpiecznie w pamięci.
                    val primaryTag = tags.first().name
                    repository.searchRecipesWithTag(query, primaryTag).map { list ->
                        list.filter { item ->
                            tags.all { it in item.recipe.tags }
                        }
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun onTagToggled(tag: RecipeTag) {
        _selectedTags.update { current ->
            if (tag in current) current - tag else current + tag
        }
    }

    fun clearTagFilters() { _selectedTags.value = emptySet() }

    // ── Zdjęcia dla PhotoViewera ──────────────────────────────────────────────

    private val _pendingPhotoUris = MutableStateFlow<List<String>>(emptyList())
    val pendingPhotoUris: StateFlow<List<String>> = _pendingPhotoUris.asStateFlow()

    private val _pendingPhotoIndex = MutableStateFlow(0)
    val pendingPhotoIndex: StateFlow<Int> = _pendingPhotoIndex.asStateFlow()

    fun setPhotoViewerData(uris: List<String>, index: Int) {
        _pendingPhotoUris.value = uris
        _pendingPhotoIndex.value = index
    }

    // ── Akcje ────────────────────────────────────────────────────────────────

    fun addRecipe(
        title: String,
        description: String,
        executionDescription: String,
        ingredients: List<IngredientInput>,
        photoUris: List<Uri>,
        tags: List<RecipeTag> = emptyList()
    ) {
        viewModelScope.launch {
            val recipe = RecipeEntity(
                title                = title,
                description          = description,
                executionDescription = executionDescription,
                tags                 = tags
            )
            val ingredientEntities = ingredients
                .filter { it.name.isNotBlank() }
                .map { input ->
                    IngredientEntity(
                        recipeId = 0,
                        name     = input.name,
                        amount   = input.amount.replace(",", ".").toDoubleOrNull(),
                        unit     = input.unit.trim().ifBlank { null }
                    )
                }
            val photoEntities = photoUris.map { uri ->
                PhotoEntity(recipeId = 0, uri = copyPhotoToAppStorage(getApplication(), uri))
            }
            repository.insertFullRecipe(recipe, ingredientEntities, photoEntities)
        }
    }

    fun updateRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientInput>,
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
                .filter { it.name.isNotBlank() }
                .map { input ->
                    IngredientEntity(
                        recipeId = recipe.id,
                        name     = input.name,
                        amount   = input.amount.replace(",", ".").toDoubleOrNull(),
                        unit     = input.unit.trim().ifBlank { null }
                    )
                }
            repository.updateRecipe(
                recipe.copy(tags = tags),
                ingredientEntities,
                newPhotoEntities,
                removedPhotoPaths
            )
        }
    }

    fun toggleFavourite(id: Long, current: Boolean) {
        viewModelScope.launch { repository.toggleFavourite(id, !current) }
    }

    // Pobiera powiązane pliki z dysku i usuwa je, zapobiegając powstawaniu śmieci w pamięci urządzenia
    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            val fullRecipe = repository.getRecipeById(recipe.id).first()
            fullRecipe?.photos?.forEach { photoEntity ->
                File(photoEntity.uri).takeIf { it.exists() }?.delete()
            }
            repository.deleteRecipe(recipe)
        }
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