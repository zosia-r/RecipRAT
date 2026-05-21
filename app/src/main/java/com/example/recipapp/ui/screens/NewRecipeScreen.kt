package com.example.recipapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.ui.theme.CoffeeBean
import com.example.recipapp.ui.theme.DeepTeal
import com.example.recipapp.ui.theme.DeepTealLight
import com.example.recipapp.ui.theme.MintCream
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecipeScreen(
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit
) {
    var title                by remember { mutableStateOf("") }
    var description          by remember { mutableStateOf("") }
    var executionDescription by remember { mutableStateOf("") }
    var ingredients          by remember { mutableStateOf(listOf("")) }
    var photoUris            by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedTags         by remember { mutableStateOf<List<RecipeTag>>(emptyList()) }
    var titleError           by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> photoUris = photoUris + uris }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Recipe",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
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

            // ── Tytuł ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value       = title,
                onValueChange = { title = it; titleError = false },
                label       = { Text("Title *") },
                isError     = titleError,
                supportingText = { if (titleError) Text("Title is required") },
                modifier    = Modifier.fillMaxWidth(),
                singleLine  = true,
                textStyle   = MaterialTheme.typography.titleLarge,
                shape       = RoundedCornerShape(16.dp),
                colors      = recipeTextFieldColors()
            )

            // ── Opis ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Description") },
                modifier      = Modifier.fillMaxWidth(),
                minLines      = 2,
                shape         = RoundedCornerShape(16.dp),
                colors        = recipeTextFieldColors()
            )

            // ── Tagi ─────────────────────────────────────────────────────────
            SectionHeader(title = "Tags")
            TagSelector(
                selectedTags = selectedTags,
                onTagToggle  = { tag ->
                    selectedTags = if (tag in selectedTags)
                        selectedTags - tag else selectedTags + tag
                }
            )

            // ── Składniki ────────────────────────────────────────────────────
            SectionHeader(title = "Ingredients")
            ingredients.forEachIndexed { index, ingredient ->
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value         = ingredient,
                        onValueChange = { newVal ->
                            ingredients = ingredients.toMutableList().also { it[index] = newVal }
                        },
                        label    = { Text("Ingredient ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape    = RoundedCornerShape(14.dp),
                        colors   = recipeTextFieldColors()
                    )
                    if (ingredients.size > 1) {
                        IconButton(onClick = {
                            ingredients = ingredients.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            TextButton(
                onClick  = { ingredients = ingredients + "" },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DeepTeal)
                Spacer(Modifier.width(4.dp))
                Text("Add ingredient", color = DeepTeal, style = MaterialTheme.typography.labelLarge)
            }

            // ── Wykonanie ────────────────────────────────────────────────────
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

            // ── Zdjęcia ──────────────────────────────────────────────────────
            SectionHeader(title = "Photos (${photoUris.size})")
            OutlinedButton(
                onClick  = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                border   = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DeepTeal)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add photos", style = MaterialTheme.typography.labelLarge)
            }

            photoUris.forEach { uri ->
                PhotoRow(
                    label   = uri.lastPathSegment ?: uri.toString(),
                    onRemove = { photoUris = photoUris - uri }
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Zapisz ───────────────────────────────────────────────────────
            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    viewModel.addRecipe(
                        title                = title.trim(),
                        description          = description.trim(),
                        executionDescription = executionDescription.trim(),
                        ingredients          = ingredients,
                        photoUris            = photoUris,
                        tags                 = selectedTags
                    )
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 0.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepTeal,
                    contentColor   = MintCream
                )
            ) {
                Text("Save Recipe", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Shared helpers ─────────────────────────────────────────────────────────────

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

@Composable
fun recipeTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = DeepTeal,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor    = DeepTeal,
    cursorColor          = DeepTeal,
    focusedContainerColor   = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)