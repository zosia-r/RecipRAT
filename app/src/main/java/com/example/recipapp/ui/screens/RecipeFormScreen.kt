package com.example.recipapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.entity.RecipeTag
import com.example.recipapp.sharing.parseRecipeText
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.MintCream
import com.example.recipapp.util.formatIngredientAmount
import com.example.recipapp.viewmodel.IngredientInput
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFormScreen(
    recipeId: Long?,
    isImport: Boolean = false,
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit
) {
    val isEditMode = recipeId != null

    // Naprawione: Bezpieczne pobieranie stanu z ViewModelu. Jeśli to nie jest tryb edycji,
    // używamy słowa kluczowego remember, aby poprawnie zainicjalizować pusty stan bez alokacji w locie.
    val recipeWithDetails by if (isEditMode) {
        viewModel.getRecipeById(recipeId!!).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var rawText     by rememberSaveable { mutableStateOf("") }
    var parseError  by rememberSaveable { mutableStateOf(false) }
    var isParsed    by rememberSaveable { mutableStateOf(false) }

    // ── Pancerne Savery stanów złożonych ──────────────────────────────────────
    val ingredientsSaver = listSaver<MutableState<List<IngredientInput>>, String>(
        save = { state ->
            val flat = mutableListOf<String>()
            state.value.forEach { flat.addAll(listOf(it.name, it.amount, it.unit)) }
            flat
        },
        restore = { flatList ->
            mutableStateOf(flatList.chunked(3).map { IngredientInput(it[0], it[1], it[2]) })
        }
    )

    val photoUrisSaver = listSaver<MutableState<List<Uri>>, String>(
        save = { state -> state.value.map { it.toString() } },
        restore = { flatList -> mutableStateOf(flatList.map { Uri.parse(it) }) }
    )

    val existingPhotoPathsSaver = listSaver<MutableState<List<String>>, String>(
        save = { state -> state.value },
        restore = { flatList -> mutableStateOf(flatList) }
    )

    val selectedTagsSaver = listSaver<MutableState<List<RecipeTag>>, String>(
        save = { state -> state.value.map { it.name } },
        restore = { flatList -> mutableStateOf(flatList.map { RecipeTag.valueOf(it) }) }
    )

    // ── Główne stany pól formularza ──────────────────────────────────────────
    var title                by rememberSaveable { mutableStateOf("") }
    var description          by rememberSaveable { mutableStateOf("") }
    var executionDescription by rememberSaveable { mutableStateOf("") }
    var titleError           by rememberSaveable { mutableStateOf(false) }

    var ingredients       by rememberSaveable(saver = ingredientsSaver) { mutableStateOf(listOf(IngredientInput("", "", ""))) }
    var existingPhotoPaths by rememberSaveable(saver = existingPhotoPathsSaver) { mutableStateOf(emptyList()) }
    var newPhotoUris      by rememberSaveable(saver = photoUrisSaver) { mutableStateOf(emptyList()) }
    var selectedTags      by rememberSaveable(saver = selectedTagsSaver) { mutableStateOf(emptyList()) }

    var isDataLoaded by rememberSaveable { mutableStateOf(false) }

    val shouldShowFormView = !isImport || isParsed

    if (isEditMode && recipeWithDetails == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DeepTeal)
        }
        return
    }

    LaunchedEffect(recipeWithDetails) {
        val details = recipeWithDetails
        if (isEditMode && details != null && !isDataLoaded) {
            val r = details.recipe
            title = r.title
            description = r.description
            executionDescription = r.executionDescription ?: ""
            selectedTags = r.tags
            existingPhotoPaths = details.photos.map { it.uri }
            ingredients = details.ingredients.map { entity ->
                IngredientInput(
                    name = entity.name,
                    amount = formatIngredientAmount(entity.amount),
                    unit = entity.unit ?: ""
                )
            }.ifEmpty { listOf(IngredientInput("", "", "")) }
            isDataLoaded = true
        }
    }

    val applyParsedText = {
        val result = parseRecipeText(rawText)
        if (result == null) {
            parseError = true
        } else {
            title                = result.title
            description          = result.description
            executionDescription = result.steps
            ingredients          = result.ingredients.ifEmpty { listOf(IngredientInput("", "", "")) }
            selectedTags         = result.tags
            parseError           = false
            isParsed             = true
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> newPhotoUris = newPhotoUris + uris }

    val onSaveAction = {
        if (title.isBlank()) {
            titleError = true
        } else {
            if (isEditMode) {
                val details = recipeWithDetails
                if (details != null) {
                    val removedPaths = details.photos.map { it.uri }.filter { it !in existingPhotoPaths }
                    viewModel.updateRecipe(
                        recipe = details.recipe.copy(
                            title                = title.trim(),
                            description          = description.trim(),
                            executionDescription = executionDescription.trim()
                        ),
                        ingredients       = ingredients,
                        newPhotoUris      = newPhotoUris,
                        removedPhotoPaths = removedPaths,
                        tags              = selectedTags
                    )
                }
            } else {
                viewModel.addRecipe(
                    title                = title.trim(),
                    description          = description.trim(),
                    executionDescription = executionDescription.trim(),
                    ingredients          = ingredients,
                    photoUris            = newPhotoUris,
                    tags                 = selectedTags
                )
            }
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = when {
                        isEditMode -> "Edit Recipe"
                        isImport && !isParsed -> "Import Recipe"
                        isImport && isParsed -> "Edit imported recipe"
                        else -> "New Recipe"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (isImport && isParsed) {
                        isParsed = false
                    } else {
                        onNavigateBack()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (shouldShowFormView) {
                    IconButton(onClick = { onSaveAction() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = DeepTeal)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor    = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            if (!shouldShowFormView) {
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
                    onClick  = { applyParsedText() },
                    enabled  = rawText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = MintCream)
                ) {
                    Text("Parse recipe", style = MaterialTheme.typography.labelLarge)
                }

            } else {
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it; titleError = false },
                    label         = { Text("Title *") },
                    isError       = titleError,
                    supportingText = { if (titleError) Text("Title is required") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    textStyle     = MaterialTheme.typography.titleLarge,
                    shape         = RoundedCornerShape(16.dp),
                    colors        = recipeTextFieldColors()
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
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value         = ingredient.name,
                            onValueChange = { newVal ->
                                ingredients = ingredients.toMutableList().also {
                                    it[index] = it[index].copy(name = newVal)
                                }
                            },
                            label    = { Text("Name") },
                            modifier = Modifier.weight(1.8f),
                            singleLine = true,
                            shape    = RoundedCornerShape(14.dp),
                            colors   = recipeTextFieldColors()
                        )

                        OutlinedTextField(
                            value         = ingredient.amount,
                            onValueChange = { newVal ->
                                if (newVal.all { it.isDigit() || it == '.' || it == ',' }) {
                                    ingredients = ingredients.toMutableList().also {
                                        it[index] = it[index].copy(amount = newVal)
                                    }
                                }
                            },
                            label    = { Text("Amt") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.9f),
                            singleLine = true,
                            shape    = RoundedCornerShape(14.dp),
                            colors   = recipeTextFieldColors()
                        )

                        OutlinedTextField(
                            value         = ingredient.unit,
                            onValueChange = { newVal ->
                                ingredients = ingredients.toMutableList().also {
                                    it[index] = it[index].copy(unit = newVal)
                                }
                            },
                            label    = { Text("Unit") },
                            modifier = Modifier.weight(0.9f),
                            singleLine = true,
                            shape    = RoundedCornerShape(14.dp),
                            colors   = recipeTextFieldColors()
                        )

                        if (ingredients.size > 1) {
                            IconButton(
                                onClick = {
                                    ingredients = ingredients.toMutableList().also { it.removeAt(index) }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                TextButton(
                    onClick  = { ingredients = ingredients + IngredientInput("", "", "") },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = DeepTeal)
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

                val totalPhotos = existingPhotoPaths.size + newPhotoUris.size
                SectionHeader(title = "Photos ($totalPhotos)")

                if (existingPhotoPaths.isNotEmpty()) {
                    Text("Current photos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    existingPhotoPaths.forEach { path ->
                        PhotoRow(
                            label    = path.substringAfterLast("/"),
                            onRemove = { existingPhotoPaths = existingPhotoPaths - path }
                        )
                    }
                }

                if (newPhotoUris.isNotEmpty()) {
                    Text("New photos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    newPhotoUris.forEach { uri ->
                        PhotoRow(
                            label    = uri.lastPathSegment ?: uri.toString(),
                            onRemove = { newPhotoUris = newPhotoUris - uri }
                        )
                    }
                }

                OutlinedButton(
                    onClick  = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                    border   = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(DeepTeal))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add photos", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}


@Composable
fun SectionHeader(title: String) {
    Text(
        text  = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun PhotoRow(label: String, onRemove: () -> Unit) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove photo",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helpers
@Composable
fun recipeTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = DeepTeal,
    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
    focusedLabelColor       = DeepTeal,
    cursorColor             = DeepTeal,
    focusedContainerColor   = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)