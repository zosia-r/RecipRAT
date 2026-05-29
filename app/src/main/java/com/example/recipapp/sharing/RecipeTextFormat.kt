package com.example.recipapp.sharing

import com.example.recipapp.data.entity.RecipeTag
import com.example.recipapp.viewmodel.IngredientInput

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
Tags: 🍳 Breakfast, 🍬 Sweet       (optional)
<empty line>
Ingredients:
• Mąka (250 g)
• Sól (szczypta)
• Jajka (4 szt)
• Oliwa
<empty line>
Preparation:
<steps>
 **/

fun buildShareText(
    title: String,
    description: String,
    ingredients: List<IngredientInput>,
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
        ingredients.forEach { ing ->
            if (ing.amount.isNotBlank() || ing.unit.isNotBlank()) {
                val details = "${ing.amount} ${ing.unit}".trim()
                appendLine("• ${ing.name} ($details)")
            } else {
                appendLine("• ${ing.name}")
            }
        }
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

    val ingredients: List<IngredientInput> = if (ingredientsIdx >= 0) {
        val end = if (preparationIdx > ingredientsIdx) preparationIdx else lines.size
        lines.subList(ingredientsIdx + 1, end)
            .map { it.trim().removePrefix("•").trim() }
            .filter { it.isNotBlank() }
            .map { line ->
                val lastCloseParenthesis = line.lastIndexOf(')')
                if (lastCloseParenthesis == line.length - 1) {
                    val lastOpenParenthesis = line.lastIndexOf('(')
                    if (lastOpenParenthesis != -1 && lastOpenParenthesis < lastCloseParenthesis) {
                        val name = line.substring(0, lastOpenParenthesis).trim()
                        val details = line.substring(lastOpenParenthesis + 1, lastCloseParenthesis).trim()

                        val detailsParts = details.split(Regex("""\s+"""), 2)
                        val amount = detailsParts.getOrNull(0) ?: ""
                        val unit = detailsParts.getOrNull(1) ?: ""

                        if (amount.all { it.isDigit() || it == '.' || it == ',' }) {
                            IngredientInput(name = name, amount = amount, unit = unit)
                        } else {
                            IngredientInput(name = name, amount = "", unit = details)
                        }
                    } else {
                        IngredientInput(name = line, amount = "", unit = "")
                    }
                } else {
                    IngredientInput(name = line, amount = "", unit = "")
                }
            }
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