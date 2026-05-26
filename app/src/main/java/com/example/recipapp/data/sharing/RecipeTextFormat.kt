package com.example.recipapp.data.sharing

import com.example.recipapp.data.RecipeTag

// Format:
//   🍴 <tytuł>
//   <pusty wiersz>
//   <opis>                       (opcjonalny)
//   <pusty wiersz>
//   Tags: TAG1, TAG2             (opcjonalne)
//   <pusty wiersz>
//   Ingredients:
//   • składnik 1
//   <pusty wiersz>
//   Preparation:
//   <kroki>

data class ParsedRecipe(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val steps: String,
    val tags: List<RecipeTag>        // ← nowe pole
)

fun buildShareText(
    title: String,
    description: String,
    ingredients: List<String>,
    steps: String,
    tags: List<String> = emptyList()  // ← nowy parametr
): String = buildString {
    appendLine("🍴 $title")
    if (description.isNotBlank()) {
        appendLine()
        appendLine(description)
    }
    if (tags.isNotEmpty()) {               // ← nowa sekcja Tags
        appendLine()
        appendLine("Tags: ${tags.joinToString(", ") { it }}")
    }
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

    // Opis — wiersze między tytułem a pierwszą sekcją (Tags / Ingredients / Preparation)
    val firstSectionIdx = listOf(tagsIdx, ingredientsIdx, preparationIdx)
        .filter { it > titleIdx }
        .minOrNull() ?: lines.size

    val description = lines
        .subList(titleIdx + 1, firstSectionIdx)
        .joinToString("\n")
        .trim()

    // Tagi — wiersz "Tags: TAG1, TAG2, ..."
    val tags: List<RecipeTag> = if (tagsIdx >= 0) {
        val tagsPart = lines[tagsIdx].trim().removePrefix("Tags:").trim()
        tagsPart.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { name ->
                runCatching { RecipeTag.valueOf(name) }.getOrNull()
            }
    } else emptyList()

    // Składniki
    val ingredients: List<String> = if (ingredientsIdx >= 0) {
        val end = if (preparationIdx > ingredientsIdx) preparationIdx else lines.size
        lines.subList(ingredientsIdx + 1, end)
            .map { it.trim().removePrefix("•").trim() }
            .filter { it.isNotBlank() }
    } else emptyList()

    // Kroki
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