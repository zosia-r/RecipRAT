package com.example.recipapp.data.sharing


// ── Format tekstu przepisu (eksport / import) ─────────────────────────────────
//
// Format:
//   🍴 <tytuł>
//   <pusty wiersz>
//   <opis>                       (opcjonalny)
//   <pusty wiersz>
//   Ingredients:
//   • składnik 1
//   • składnik 2
//   <pusty wiersz>
//   Preparation:
//   <kroki>
//
// ─────────────────────────────────────────────────────────────────────────────

data class ParsedRecipe(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val steps: String
)

fun buildShareText(
    title: String,
    description: String,
    ingredients: List<String>,
    steps: String
): String = buildString {
    appendLine("🍴 $title")
    if (description.isNotBlank()) {
        appendLine()
        appendLine(description)
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

/**
 * Parsuje tekst w formacie [buildShareText] i zwraca [ParsedRecipe],
 * lub null jeśli tekst nie zawiera wymaganego tytułu (wiersz zaczynający się od 🍴).
 */
fun parseRecipeText(text: String): ParsedRecipe? {
    val lines = text.lines()

    // Tytuł — pierwszy niepusty wiersz zaczynający się od 🍴
    val titleLine = lines.firstOrNull { it.trimStart().startsWith("🍴") }
        ?: return null
    val title = titleLine.trimStart().removePrefix("🍴").trim()
    if (title.isBlank()) return null

    // Indeksy sekcji
    val ingredientsIdx = lines.indexOfFirst { it.trim() == "Ingredients:" }
    val preparationIdx = lines.indexOfFirst { it.trim() == "Preparation:" }
    val titleIdx       = lines.indexOf(titleLine)

    // Opis — wiersze między tytułem a sekcją Ingredients (lub Preparation, lub końcem)
    val descEnd = when {
        ingredientsIdx > titleIdx -> ingredientsIdx
        preparationIdx > titleIdx -> preparationIdx
        else                      -> lines.size
    }
    val description = lines
        .subList(titleIdx + 1, descEnd)
        .joinToString("\n")
        .trim()

    // Składniki — wiersze z "•" między Ingredients: a Preparation: (lub końcem)
    val ingredients: List<String> = if (ingredientsIdx >= 0) {
        val end = if (preparationIdx > ingredientsIdx) preparationIdx else lines.size
        lines.subList(ingredientsIdx + 1, end)
            .map { it.trim().removePrefix("•").trim() }
            .filter { it.isNotBlank() }
    } else emptyList()

    // Kroki — wszystko po "Preparation:"
    val steps: String = if (preparationIdx >= 0) {
        lines.subList(preparationIdx + 1, lines.size)
            .joinToString("\n")
            .trim()
    } else ""

    return ParsedRecipe(
        title       = title,
        description = description,
        ingredients = ingredients,
        steps       = steps
    )
}