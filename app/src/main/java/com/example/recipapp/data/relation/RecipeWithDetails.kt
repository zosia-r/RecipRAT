package com.example.recipapp.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.recipapp.data.entity.IngredientEntity
import com.example.recipapp.data.entity.PhotoEntity
import com.example.recipapp.data.entity.RecipeEntity

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity, // wbuduj wszystkie pola z RecipeEntity
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<IngredientEntity>, // mówi roomowi jak połączyć tabele
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val photos: List<PhotoEntity>
)