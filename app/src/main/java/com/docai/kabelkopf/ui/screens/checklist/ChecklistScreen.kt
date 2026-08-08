package com.docai.kabelkopf.ui.screens.checklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.docai.kabelkopf.data.local.entity.ChecklistEntity
import com.docai.kabelkopf.data.local.entity.ChecklistItemEntity
import com.docai.kabelkopf.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    navController: NavController,
    viewModel: ChecklistViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.selectedChecklist != null) state.selectedChecklist!!.name else "Checklisten")
                },
                navigationIcon = {
                    if (state.selectedChecklist != null) {
                        IconButton(onClick = { viewModel.backToList() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                },
                actions = {
                    if (state.selectedChecklist != null) {
                        IconButton(onClick = {
                            state.selectedChecklist?.let { viewModel.deleteChecklist(it.id) }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Löschen")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (state.selectedChecklist == null) {
                FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "Neue Checkliste")
                }
            }
        }
    ) { padding ->
        if (state.selectedChecklist != null) {
            ChecklistDetailView(
                items = state.items,
                onToggle = { id, checked -> viewModel.toggleItem(id, checked) },
                modifier = Modifier.padding(padding)
            )
        } else {
            ChecklistListView(
                checklists = state.checklists,
                onSelect = { viewModel.selectChecklist(it) },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (state.showAddDialog) {
        AddChecklistDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { name, category, items -> viewModel.createChecklist(name, category, items) }
        )
    }
}

@Composable
private fun ChecklistListView(
    checklists: List<ChecklistEntity>,
    onSelect: (ChecklistEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (checklists.isEmpty()) {
        EmptyStateView(
            title = "Keine Checklisten",
            message = "Erstelle deine erste Checkliste für wiederkehrende Arbeiten",
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(checklists) { checklist ->
                Card(
                    onClick = { onSelect(checklist) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(checklist.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                checklist.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistDetailView(
    items: List<ChecklistItemEntity>,
    onToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isChecked)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(item.id, !item.isChecked) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { onToggle(item.id, it) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.isChecked)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChecklistDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, items: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Allgemein") }
    var itemsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neue Checkliste") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategorie") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = itemsText,
                    onValueChange = { itemsText = it },
                    label = { Text("Punkte (pro Zeile)") },
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val items = itemsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(name, category, items)
                },
                enabled = name.isNotBlank() && itemsText.isNotBlank()
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
