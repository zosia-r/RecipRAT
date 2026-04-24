package com.example.recipapp.data

import androidx.room.Transaction
import com.example.recipapp.data.dao.RecipeDao
import com.example.recipapp.data.entity.IngredientEntity
import com.example.recipapp.data.entity.PhotoEntity
import com.example.recipapp.data.entity.RecipeEntity
import com.example.recipapp.data.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val dao: RecipeDao) {

    val allRecipes: Flow<List<RecipeWithDetails>> = dao.getAllRecipesWithDetails()
    val favouriteRecipes: Flow<List<RecipeWithDetails>> = dao.getFavouriteRecipes()

    fun getRecipeById(id: Long): Flow<RecipeWithDetails?> = dao.getRecipeById(id)

    fun searchRecipes(query: String): Flow<List<RecipeWithDetails>> = dao.searchRecipes(query)

    @Transaction
    suspend fun insertFullRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        photos: List<PhotoEntity>
    ) {
        val recipeId = dao.insertRecipe(recipe)
        dao.insertIngredients(ingredients.map { it.copy(recipeId = recipeId) })
        dao.insertPhotos(photos.map { it.copy(recipeId = recipeId) })
    }

    suspend fun updateRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        photos: List<PhotoEntity>
    ) {
        dao.updateRecipe(recipe)
        dao.deleteIngredientsByRecipe(recipe.id)
        dao.deletePhotosByRecipe(recipe.id)
        dao.insertIngredients(ingredients.map { it.copy(recipeId = recipe.id) })
        dao.insertPhotos(photos.map { it.copy(recipeId = recipe.id) })
    }

    suspend fun deleteRecipe(recipe: RecipeEntity) = dao.deleteRecipe(recipe)

    suspend fun toggleFavourite(id: Long, isFavourite: Boolean) =
        dao.updateFavourite(id, isFavourite)
}