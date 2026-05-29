package com.example.recipapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.recipapp.util.formatIngredientAmount

@Entity(
    tableName = "ingredients",
    foreignKeys = [ForeignKey(
        entity = RecipeEntity::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("recipeId")]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    val name: String,
    val amount: Double?,
    val unit: String?
)


// Extension function - formats ingredient details to a string.
fun IngredientEntity.getDisplayDetails(scale: Float = 1f): String {
    val baseAmount = this.amount
    val scaledAmount = if (baseAmount != null) baseAmount * scale else null

    val amountText = formatIngredientAmount(scaledAmount)
    val unitText = this.unit ?: ""
    return "$amountText $unitText".trim()
}