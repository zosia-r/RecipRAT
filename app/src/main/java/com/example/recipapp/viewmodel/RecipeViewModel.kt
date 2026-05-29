package com.example.recipapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipapp.data.RecipeRepository
import com.example.recipapp.data.entity.IngredientEntity
import com.example.recipapp.data.entity.PhotoEntity
import com.example.recipapp.data.entity.RecipeEntity
import com.example.recipapp.data.entity.RecipeTag
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * ViewModel connects repository and UI.
 */

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RecipeViewModel(
    application: Application,
    private val repository: RecipeRepository
) : AndroidViewModel(application) {

    // ********** Recipe Flows **********
    val allRecipes: StateFlow<List<RecipeWithDetails>> =
        repository.allRecipes
        .stateIn( // converts database Flow to stable StateFlow in RAM
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val favouriteRecipes: StateFlow<List<RecipeWithDetails>?> = repository.favouriteRecipes
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null)

    // ********** Search Flows **********
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<RecipeTag>>(emptySet())
    // Expose read-only tags state to the UI to prevent accidental modifications
    val selectedTags: StateFlow<Set<RecipeTag>> = _selectedTags.asStateFlow()

    // Reactive search pipeline
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<RecipeWithDetails>?> =
        // Merge search queries and tag filters into a single combined data stream
        combine(_searchQuery, _selectedTags) { query, tags -> query to tags }
            .debounce(300)
            .flatMapLatest { (query, tags) ->
                if (tags.isEmpty()) {
                    repository.searchRecipes(query)
                } else {
                    // Optimize performance by filtering the primary tag in SQL and remaining tags in RAM
                    val primaryTag = tags.first().name
                    repository.searchRecipesWithTag(query, primaryTag).map { list ->
                        list.filter { item ->
                            tags.all { it in item.recipe.tags }
                        }
                    }
                }
            }
            // Convert the dynamic pipeline into a lifecycle-aware StateFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun onTagToggled(tag: RecipeTag) {
        _selectedTags.update { current ->
            if (tag in current) current - tag else current + tag
        }
    }

    fun clearTagFilters() { _selectedTags.value = emptySet() }

    // ********** Photo Viewer **********

    private val _pendingPhotoUris = MutableStateFlow<List<String>>(emptyList())
    val pendingPhotoUris: StateFlow<List<String>> = _pendingPhotoUris.asStateFlow()

    private val _pendingPhotoIndex = MutableStateFlow(0)
    val pendingPhotoIndex: StateFlow<Int> = _pendingPhotoIndex.asStateFlow()

    fun setPhotoViewerData(uris: List<String>, index: Int) {
        _pendingPhotoUris.value = uris
        _pendingPhotoIndex.value = index
    }

    // ********** Recipe Actions **********

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

    // Handle photo deletion
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

    // Save photo to app storage
    private fun copyPhotoToAppStorage(context: Context, uri: Uri): String {
        val dir  = File(context.filesDir, "recipe_photos").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}