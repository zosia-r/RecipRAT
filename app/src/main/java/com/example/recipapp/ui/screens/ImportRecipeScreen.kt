package com.example.recipapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.data.sharing.parseRecipeText
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.MintCream
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportRecipeScreen(
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit
) {
    // ── Krok 1: wklejanie tekstu ──────────────────────────────────────────────
    var rawText      by remember { mutableStateOf("") }
    var parseError   by remember { mutableStateOf(false) }
    var parsed       by remember { mutableStateOf(false) }

    // ── Krok 2: edycja po sparsowaniu ────────────────────────────────────────
    var title                by remember { mutableStateOf("") }
    var description          by remember { mutableStateOf("") }
    var executionDescription by remember { mutableStateOf("") }
    var ingredients          by remember { mutableStateOf(listOf("")) }
    var selectedTags         by remember { mutableStateOf<List<RecipeTag>>(emptyList()) }
    var titleError           by remember { mutableStateOf(false) }

    fun applyParsed() {
        val result = parseRecipeText(rawText)
        if (result == null) {
            parseError = true
            return
        }
        title                = result.title
        description          = result.description
        executionDescription = result.steps
        ingredients          = result.ingredients.ifEmpty { listOf("") }
        selectedTags         = emptyList()
        parseError           = false
        parsed               = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (parsed) "Edit imported recipe" else "Import Recipe",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (parsed) {
                            // cofnij do kroku wklejania
                            parsed = false
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (parsed) {
                        IconButton(onClick = {
                            if (title.isBlank()) { titleError = true; return@IconButton }
                            viewModel.addRecipe(
                                title                = title.trim(),
                                description          = description.trim(),
                                executionDescription = executionDescription.trim(),
                                ingredients          = ingredients,
                                photoUris            = emptyList(),
                                tags                 = selectedTags
                            )
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = DeepTeal)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.background,
                    titleContentColor          = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            if (!parsed) {
                // ── KROK 1: wklej tekst ───────────────────────────────────────
                Text(
                    "Paste a recipe shared from this app:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value         = rawText,
                    onValueChange = { rawText = it; parseError = false },
                    label         = { Text("Recipe text") },
                    isError       = parseError,
                    supportingText = {
                        if (parseError) Text("Could not parse recipe — make sure the text starts with 🍴 and follows the export format.")
                    },
                    modifier  = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = recipeTextFieldColors()
                )

                Button(
                    onClick  = { applyParsed() },
                    enabled  = rawText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTeal,
                        contentColor   = MintCream
                    )
                ) {
                    Text("Parse recipe", style = MaterialTheme.typography.labelLarge)
                }

            } else {
                // ── KROK 2: edytuj sparsowane dane ───────────────────────────
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it; titleError = false },
                    label         = { Text("Title *") },
                    isError       = titleError,
                    supportingText = { if (titleError) Text("Title is required") },
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle  = MaterialTheme.typography.titleLarge,
                    shape      = RoundedCornerShape(16.dp),
                    colors     = recipeTextFieldColors()
                )

                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Description") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                    shape         = RoundedCornerShape(16.dp),
                    colors        = recipeTextFieldColors()
                )

                SectionHeader(title = "Tags")
                TagSelector(
                    selectedTags = selectedTags,
                    onTagToggle  = { tag ->
                        selectedTags = if (tag in selectedTags)
                            selectedTags - tag else selectedTags + tag
                    }
                )

                SectionHeader(title = "Ingredients")
                ingredients.forEachIndexed { index, ingredient ->
                    Row(
                        verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value         = ingredient,
                            onValueChange = { newVal ->
                                ingredients = ingredients.toMutableList().also { it[index] = newVal }
                            },
                            label      = { Text("Ingredient ${index + 1}") },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            shape      = RoundedCornerShape(14.dp),
                            colors     = recipeTextFieldColors()
                        )
                        if (ingredients.size > 1) {
                            IconButton(onClick = {
                                ingredients = ingredients.toMutableList().also { it.removeAt(index) }
                            }) {
                                Icon(
                                    androidx.compose.material.icons.Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                TextButton(
                    onClick  = { ingredients = ingredients + "" },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = null,
                        tint = DeepTeal
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Add ingredient", color = DeepTeal, style = MaterialTheme.typography.labelLarge)
                }

                SectionHeader(title = "How to make it")
                OutlinedTextField(
                    value         = executionDescription,
                    onValueChange = { executionDescription = it },
                    label         = { Text("Step by step instructions") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 5,
                    shape         = RoundedCornerShape(16.dp),
                    colors        = recipeTextFieldColors()
                )

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) { titleError = true; return@Button }
                        viewModel.addRecipe(
                            title                = title.trim(),
                            description          = description.trim(),
                            executionDescription = executionDescription.trim(),
                            ingredients          = ingredients,
                            photoUris            = emptyList(),
                            tags                 = selectedTags
                        )
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepTeal,
                        contentColor   = MintCream
                    )
                ) {
                    Text("Save Recipe", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}