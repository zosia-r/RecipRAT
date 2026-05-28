package com.example.recipapp.timer

/**
 * Timer state for a single recipe.
 */
sealed class TimerState {

    data class Running(
        val recipeId: Long,
        val recipeTitle: String,
        val totalSec: Int,
        val remainingSec: Int
    ) : TimerState()

    data class Finished(
        val recipeId: Long,
        val recipeTitle: String
    ) : TimerState()
}