package com.example.recipapp.util

import com.example.recipapp.data.entity.IngredientEntity

// Formats ingredient amount to a string.
fun formatIngredientAmount(amount: Double?): String {
    if (amount == null) return ""
    return if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        amount.toString()
    }
}