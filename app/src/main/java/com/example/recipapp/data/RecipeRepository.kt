package com.example.recipapp.data

import com.example.recipapp.data.dao.RecipeDao
import com.example.recipapp.data.entity.IngredientEntity
import com.example.recipapp.data.entity.PhotoEntity
import com.example.recipapp.data.entity.RecipeEntity
import com.example.recipapp.data.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val dao: RecipeDao) {

    val allRecipes: Flow<List<RecipeWithDetails>> =
        dao.getAllRecipesWithDetails()
    val favouriteRecipes: Flow<List<RecipeWithDetails>> =
        dao.getFavouriteRecipes()

    fun getRecipeById(id: Long): Flow<RecipeWithDetails?> =
        dao.getRecipeById(id)

    fun searchRecipes(query: String): Flow<List<RecipeWithDetails>> =
        dao.searchRecipes(query)

    fun searchRecipesWithTag(query: String, tag: String): Flow<List<RecipeWithDetails>> =
        dao.searchRecipesWithTag(query, tag)

    suspend fun insertFullRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        photos: List<PhotoEntity>
    ) = dao.insertFullRecipe(recipe, ingredients, photos)

    suspend fun updateRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        newPhotos: List<PhotoEntity>,
        removedPhotoPaths: List<String>
    ) = dao.updateFullRecipe(recipe, ingredients, newPhotos, removedPhotoPaths)

    suspend fun deleteRecipe(recipe: RecipeEntity) = dao.deleteRecipe(recipe)

    suspend fun toggleFavourite(id: Long, isFavourite: Boolean) =
        dao.updateFavourite(id, isFavourite)
}