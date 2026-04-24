package com.example.recipapp.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.recipapp.data.local.entity.IngredientEntity
import com.example.recipapp.data.local.entity.PhotoEntity
import com.example.recipapp.data.local.entity.RecipeEntity

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<IngredientEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val photos: List<PhotoEntity>
)