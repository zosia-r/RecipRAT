package com.example.recipapp.util

import com.example.recipapp.data.entity.IngredientEntity

/**
 * Ładnie formatuje liczbę Double, usuwając końcówkę .0 dla liczb całkowitych.
 * Np. 2.0 -> "2", 1.5 -> "1.5", null -> ""
 */
fun formatIngredientAmount(amount: Double?): String {
    if (amount == null) return ""
    return if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        amount.toString()
    }
}

/**
 * Funkcja rozszerzająca, która generuje pełny, gotowy tekst miary i jednostki dla składnika.
 * Np. "250 g", "0.5 łyżeczki", lub "" (jeśli brak miary)
 */
fun IngredientEntity.getDisplayDetails(): String {
    val amountText = formatIngredientAmount(this.amount)
    val unitText = this.unit ?: ""
    return "$amountText $unitText".trim()
}