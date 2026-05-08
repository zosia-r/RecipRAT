package com.example.recipapp.timer

/**
 * Stan timera dla jednego przepisu.
 */
sealed class TimerState {

    /** Timer odlicza */
    data class Running(
        val recipeId: Long,
        val recipeTitle: String,
        val totalSec: Int,
        val remainingSec: Int
    ) : TimerState()

    /** Timer skończył – alarm dzwoni */
    data class Finished(
        val recipeId: Long,
        val recipeTitle: String
    ) : TimerState()
}