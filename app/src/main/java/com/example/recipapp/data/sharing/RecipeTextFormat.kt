package com.example.recipapp.data.sharing

import com.example.recipapp.data.RecipeTag

/**
 * Module for parsing and building share text.
 * Allows recipe export and import.
 */


/**
 Format:
   🍴 <title>
   <empty line>
   <description>          (optional)
   <empty line>
   Tags: 🍳 Breakfast, 🍬 Sweet       (opcjonalne)
   <empty line>
   Ingredients:
   • ingredient 1
   <empty line>
   Preparation:
   <steps>
 **/

data class ParsedRecipe(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val steps: String,
    val tags: List<RecipeTag>
)

fun buildShareText(
    title: String,
    description: String,
    ingredients: List<String>,
    steps: String,
    tags: List<RecipeTag> = emptyList()
): String = buildString {
    appendLine("🍴 $title")
    if (description.isNotBlank()) {
        appendLine()
        appendLine(description)
    }
    if (tags.isNotEmpty()) {
        appendLine()
        appendLine("Tags: ${tags.joinToString(", ") { it.label }}")    }
    if (ingredients.isNotEmpty()) {
        appendLine()
        appendLine("Ingredients:")
        ingredients.forEach { appendLine("• $it") }
    }
    if (steps.isNotBlank()) {
        appendLine()
        appendLine("Preparation:")
        appendLine(steps)
    }
}

fun parseRecipeText(text: String): ParsedRecipe? {
    val lines = text.lines()

    val titleLine = lines.firstOrNull { it.trimStart().startsWith("🍴") }
        ?: return null
    val title = titleLine.trimStart().removePrefix("🍴").trim()
    if (title.isBlank()) return null

    val titleIdx       = lines.indexOf(titleLine)
    val tagsIdx        = lines.indexOfFirst { it.trim().startsWith("Tags:") }
    val ingredientsIdx = lines.indexOfFirst { it.trim() == "Ingredients:" }
    val preparationIdx = lines.indexOfFirst { it.trim() == "Preparation:" }

    val firstSectionIdx = listOf(tagsIdx, ingredientsIdx, preparationIdx)
        .filter { it > titleIdx }
        .minOrNull() ?: lines.size

    val description = lines
        .subList(titleIdx + 1, firstSectionIdx)
        .joinToString("\n")
        .trim()

    val tags: List<RecipeTag> = if (tagsIdx >= 0) {
        val tagsPart = lines[tagsIdx].trim().removePrefix("Tags:").trim()
        tagsPart.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { textLabel ->
                RecipeTag.entries.find { it.label.equals(textLabel, ignoreCase = true) }
            }
    } else emptyList()

    val ingredients: List<String> = if (ingredientsIdx >= 0) {
        val end = if (preparationIdx > ingredientsIdx) preparationIdx else lines.size
        lines.subList(ingredientsIdx + 1, end)
            .map { it.trim().removePrefix("•").trim() }
            .filter { it.isNotBlank() }
    } else emptyList()

    val steps: String = if (preparationIdx >= 0) {
        lines.subList(preparationIdx + 1, lines.size)
            .joinToString("\n")
            .trim()
    } else ""

    return ParsedRecipe(
        title       = title,
        description = description,
        ingredients = ingredients,
        steps       = steps,
        tags        = tags
    )
}