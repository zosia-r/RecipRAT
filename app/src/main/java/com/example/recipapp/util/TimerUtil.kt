package com.example.recipapp.util

// Extension function for Int - convert to time string in format "mm:ss"
fun Int.toTimeString(): String =
    "%02d:%02d".format(
        this / 60,
        this % 60
    )