package com.docai.kabelkopf.ui.screens.knowledge

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.docai.kabelkopf.data.local.entity.KnowledgeBaseEntity
import com.docai.kabelkopf.ui.components.BottomNavBar
import com.docai.kabelkopf.ui.components.EmptyStateView
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen(
    navController: NavController,
    viewModel: KnowledgeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.showCategoryView) Text("Wissensdatenbank")
                    else Text(state.currentCategory ?: "")
                },
                navigationIcon = {
                    if (!state.showCategoryView) {
                        IconButton(onClick = { viewModel.backToCategories() }) {
                            @Suppress("DEPRECATION")
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neues Wissen")
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "knowledge",
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                        "projects" -> navController.navigate(Screen.Projects.route)
                        "knowledge" -> {}
                        "profile" -> navController.navigate(Screen.Profile.route)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Wissen durchsuchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            if (state.showCategoryView) {
                CategoryTilesView(
                    entriesByCategory = state.entriesByCategory,
                    onCategoryClick = { viewModel.selectCategoryForView(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                CategoryEntriesView(
                    entries = state.currentCategoryEntries,
                    searchQuery = state.searchQuery,
                    onEntryClick = { viewModel.openEntry(it) },
                    onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showAddDialog) {
        AddKnowledgeDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, content, tags, category ->
                viewModel.createEntry(title, content, tags, category)
            }
        )
    }

    state.editingEntry?.let { entry ->
        KnowledgeDetailDialog(
            entry = entry,
            categories = state.categories,
            onUpdate = { id, title, content, tags, category ->
                viewModel.updateEntry(id, title, content, tags, category)
            },
            onDelete = { viewModel.deleteEntry(entry.id) },
            onDismiss = { viewModel.closeEntry() }
        )
    }
}

@Composable
private fun CategoryTilesView(
    entriesByCategory: Map<String, Int>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = Constants.Categories.all
    if (entriesByCategory.isEmpty()) {
        EmptyStateView(
            title = "Wissensdatenbank leer",
            message = "Neues Wissen entsteht automatisch aus deinen Arbeiten",
            modifier = modifier
        )
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(categories) { cat ->
            val count = entriesByCategory[cat] ?: 0
            CategoryTile(
                name = cat,
                count = count,
                onClick = { if (count > 0) onCategoryClick(cat) }
            )
        }
    }
}

@Composable
private fun CategoryTile(
    name: String,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (count > 0)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineLarge,
                color = if (count > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = if (count > 0)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryEntriesView(
    entries: List<KnowledgeBaseEntity>,
    searchQuery: String,
    onEntryClick: (KnowledgeBaseEntity) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = entries.filter { entry ->
        searchQuery.isBlank() || entry.title.contains(searchQuery, ignoreCase = true) ||
                entry.content.contains(searchQuery, ignoreCase = true) ||
                entry.tags.contains(searchQuery, ignoreCase = true)
    }

    if (filtered.isEmpty()) {
        EmptyStateView(
            title = if (searchQuery.isNotBlank()) "Keine Treffer" else "Keine Einträge",
            message = if (searchQuery.isNotBlank()) "Anderen Suchbegriff probieren"
            else "Noch kein Wissen in dieser Kategorie",
            modifier = modifier
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            items(filtered) { entry ->
                KnowledgeCard(
                    entry = entry,
                    onClick = { onEntryClick(entry) },
                    onToggleFavorite = { onToggleFavorite(entry.id, !entry.isFavorite) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KnowledgeDetailDialog(
    entry: KnowledgeBaseEntity,
    categories: List<String>,
    onUpdate: (id: String, title: String, content: String, tags: String, category: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(entry.title) }
    var content by remember { mutableStateOf(entry.content) }
    var tags by remember { mutableStateOf(entry.tags) }
    var category by remember { mutableStateOf(entry.category) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    Text("Eintrag bearbeiten", modifier = Modifier.weight(1f))
                } else {
                    Text(entry.title, modifier = Modifier.weight(1f))
                }
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(
                        if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Bearbeiten beenden" else "Bearbeiten"
                    )
                }
            }
        },
        text = {
            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titel *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Inhalt *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (kommagetrennt)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategorie") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            Constants.Categories.all.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (entry.tags.isNotBlank()) {
                        Text(
                            text = "Tags: ${entry.tags}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Kategorie: ${entry.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                TextButton(
                    onClick = {
                        onUpdate(entry.id, title, content, tags, category)
                    },
                    enabled = title.isNotBlank() && content.isNotBlank()
                ) {
                    Text("Speichern")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Schließen")
                }
            }
        },
        dismissButton = {
            if (isEditing) {
                TextButton(onClick = { isEditing = false }) {
                    Text("Abbrechen")
                }
            } else {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Löschen")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddKnowledgeDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, tags: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Constants.Categories.UV) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neues Wissen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Inhalt *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (kommagetrennt)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Constants.Categories.all.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(title, content, tags, category)
                    onDismiss()
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
private fun KnowledgeCard(
    entry: KnowledgeBaseEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (entry.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Favorit",
                        tint = if (entry.isFavorite)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (entry.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.tags,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
