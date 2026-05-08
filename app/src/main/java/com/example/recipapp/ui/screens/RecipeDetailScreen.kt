package com.example.recipapp.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.recipapp.data.RecipeTag
import com.example.recipapp.timer.TimerService
import com.example.recipapp.timer.TimerState
import com.example.recipapp.timer.toTimeString
import com.example.recipapp.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onPhotoClick: (initialUri: String, allUris: List<String>) -> Unit
) {
    val context = LocalContext.current
    val recipeWithDetails by viewModel.getRecipeById(recipeId).collectAsState(initial = null)
    val recipe = recipeWithDetails?.recipe

    val checkedIngredients = remember { mutableStateMapOf<Int, Boolean>() }

    val allTimers by TimerService.timers.collectAsState()
    val timerState = allTimers[recipeId]
    var showTimerDialog by remember { mutableStateOf(false) }
    val showAlarmDialog = timerState is TimerState.Finished

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    recipe?.let { r ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedVisibility(
                                visible = timerState is TimerState.Running,
                                enter   = fadeIn(),
                                exit    = fadeOut()
                            ) {
                                if (timerState is TimerState.Running) {
                                    Text(
                                        text  = timerState.remainingSec.toTimeString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { showTimerDialog = true }) {
                                Icon(
                                    imageVector = if (timerState is TimerState.Running)
                                        Icons.Filled.Timer else Icons.Outlined.Timer,
                                    contentDescription = "Timer",
                                    tint = if (timerState is TimerState.Running)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = {
                            val text = buildShareText(
                                title       = r.title,
                                description = r.description,
                                ingredients = recipeWithDetails!!.ingredients.map { it.name },
                                steps       = r.executionDescription
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                                putExtra(Intent.EXTRA_SUBJECT, r.title)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share recipe"))
                        }) { Icon(Icons.Default.Share, contentDescription = "Share") }

                        IconButton(onClick = { onNavigateToEdit(r.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }

                        IconButton(onClick = { viewModel.toggleFavourite(r.id, r.isFavourite) }) {
                            Icon(
                                imageVector = if (r.isFavourite)
                                    Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite",
                                tint = if (r.isFavourite)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (recipeWithDetails == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val photos = recipeWithDetails!!.photos
        val allPhotoUris = photos.map { it.uri }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Zdjęcia ──────────────────────────────────────────────────────
            if (photos.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { photos.size })
                Box {
                    HorizontalPager(
                        state    = pagerState,
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                    ) { page ->
                        AsyncImage(
                            model              = photos[page].uri,
                            contentDescription = "Photo ${page + 1}",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .fillMaxSize()
                                .clickable { onPhotoClick(photos[page].uri, allPhotoUris) }
                        )
                    }
                    if (photos.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            photos.indices.forEach { index ->
                                val selected = pagerState.currentPage == index
                                Surface(
                                    modifier = Modifier.size(if (selected) 8.dp else 6.dp),
                                    shape    = MaterialTheme.shapes.extraSmall,
                                    color    = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                ) {}
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Tagi ─────────────────────────────────────────────────────
                val tags = recipe!!.tags.mapNotNull { name ->
                    runCatching { RecipeTag.valueOf(name) }.getOrNull()
                }
                if (tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label   = { Text(tag.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // ── Opis ─────────────────────────────────────────────────────
                if (recipe.description.isNotBlank()) {
                    SectionCard("Opis") {
                        Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // ── Składniki ─────────────────────────────────────────────────
                val ingredients = recipeWithDetails!!.ingredients
                if (ingredients.isNotEmpty()) {
                    SectionCard("Ingredients") {
                        ingredients.forEachIndexed { index, ingredient ->
                            val isChecked = checkedIngredients[index] == true
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked         = isChecked,
                                    onCheckedChange = { checkedIngredients[index] = it }
                                )
                                Text(
                                    text     = ingredient.name,
                                    style    = MaterialTheme.typography.bodyMedium,
                                    color    = if (isChecked)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (ingredient.amount.isNotBlank()) {
                                    Text(
                                        ingredient.amount,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < ingredients.lastIndex) {
                                HorizontalDivider(Modifier.padding(vertical = 2.dp))
                            }
                        }
                        if (checkedIngredients.values.any { it }) {
                            TextButton(
                                onClick  = { checkedIngredients.clear() },
                                modifier = Modifier.align(Alignment.End)
                            ) { Text("Reset") }
                        }
                    }
                }

                // ── Sposób wykonania ──────────────────────────────────────────
                if (recipe.executionDescription.isNotBlank()) {
                    SectionCard("Execution description") {
                        Text(recipe.executionDescription, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showTimerDialog) {
        TimerSetDialog(
            currentTimer = timerState as? TimerState.Running,
            recipeTitle  = recipe?.title ?: "",
            onStart      = { minutes, seconds ->
                TimerService.startTimer(context, recipeId, recipe?.title ?: "", minutes * 60 + seconds)
                showTimerDialog = false
            },
            onStop    = { TimerService.stopTimer(context, recipeId); showTimerDialog = false },
            onDismiss = { showTimerDialog = false }
        )
    }

    if (showAlarmDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("⏰ Timer finished!") },
            text  = { Text("${recipe?.title} is ready!") },
            confirmButton = {
                Button(onClick = { TimerService.dismissAlarm(context, recipeId) }) {
                    Text("OK, got it!")
                }
            }
        )
    }
}

@Composable
private fun TimerSetDialog(
    currentTimer: TimerState.Running?,
    recipeTitle: String,
    onStart: (minutes: Int, seconds: Int) -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit
) {
    var minutes by remember {
        mutableStateOf(if (currentTimer != null) (currentTimer.remainingSec / 60).toString() else "5")
    }
    var seconds by remember {
        mutableStateOf(if (currentTimer != null) (currentTimer.remainingSec % 60).toString() else "0")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set timer") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentTimer != null) {
                    Text(
                        text  = "Running: ${currentTimer.remainingSec.toTimeString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                }
                Text("Set new timer for $recipeTitle:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value           = minutes,
                        onValueChange   = { if (it.length <= 2) minutes = it.filter { c -> c.isDigit() } },
                        label           = { Text("min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.width(80.dp),
                        singleLine      = true
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(
                        value           = seconds,
                        onValueChange   = { if (it.length <= 2) seconds = it.filter { c -> c.isDigit() } },
                        label           = { Text("sec") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.width(80.dp),
                        singleLine      = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val m = minutes.toIntOrNull() ?: 0
                val s = seconds.toIntOrNull() ?: 0
                if (m > 0 || s > 0) onStart(m, s)
            }) { Text("Start") }
        },
        dismissButton = {
            Row {
                if (currentTimer != null) {
                    TextButton(onClick = onStop) { Text("Stop timer") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

private fun buildShareText(
    title: String, description: String,
    ingredients: List<String>, steps: String
): String = buildString {
    appendLine("🍴 $title")
    if (description.isNotBlank()) { appendLine(); appendLine(description) }
    if (ingredients.isNotEmpty()) {
        appendLine(); appendLine("Ingredients:")
        ingredients.forEach { appendLine("• $it") }
    }
    if (steps.isNotBlank()) { appendLine(); appendLine("Preparation:"); appendLine(steps) }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}