package com.example.recipapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.recipapp.data.entity.IngredientEntity
import com.example.recipapp.data.entity.PhotoEntity
import com.example.recipapp.data.entity.RecipeEntity
import com.example.recipapp.data.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    // ********** CREATE **********
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Transaction
    suspend fun insertFullRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        photos: List<PhotoEntity>
    ) {
        val recipeId = insertRecipe(recipe)
        // Link ingredients to the newly created recipe by assigning its generated ID to each ingredient
        insertIngredients(ingredients.map { it.copy(recipeId = recipeId) })
        insertPhotos(photos.map { it.copy(recipeId = recipeId) })
    }

    // ********** READ **********
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipesWithDetails(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE isFavourite = 1 ORDER BY title ASC")
    fun getFavouriteRecipes(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: Long): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchRecipes(query: String): Flow<List<RecipeWithDetails>>

    // Room handles the internal multi-table queries automatically to populate RecipeWithDetails
    @Transaction
    @Query("""
        SELECT * FROM recipes 
        WHERE title LIKE '%' || :query || '%'
        AND (:tag = '' OR '|||' || tags || '|||' LIKE '%|||' || :tag || '|||%')
        ORDER BY title ASC
    """)
    fun searchRecipesWithTag(query: String, tag: String): Flow<List<RecipeWithDetails>>


    // ********** UPDATE **********
    @Query("UPDATE recipes SET isFavourite = :isFavourite WHERE id = :id")
    suspend fun updateFavourite(id: Long, isFavourite: Boolean)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Transaction
    suspend fun updateFullRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        newPhotos: List<PhotoEntity>,
        removedPhotoPaths: List<String>
    ) {
        updateRecipe(recipe)
        deleteIngredientsByRecipe(recipe.id)
        insertIngredients(ingredients.map { it.copy(recipeId = recipe.id) })
        if (removedPhotoPaths.isNotEmpty()) deletePhotosByPaths(removedPhotoPaths)
        if (newPhotos.isNotEmpty()) insertPhotos(newPhotos.map { it.copy(recipeId = recipe.id) })
    }

    // ********** DELETE **********
    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipe(recipeId: Long)

    @Query("DELETE FROM photos WHERE uri IN (:paths)")
    suspend fun deletePhotosByPaths(paths: List<String>)
}