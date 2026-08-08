package com.docai.kabelkopf.ui.screens.search

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
import com.docai.kabelkopf.ui.components.EmptyStateView
import com.docai.kabelkopf.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            text = it
                            viewModel.updateQuery(it)
                        },
                        placeholder = { Text("Suchen in Arbeiten, Wissen, Fehlern...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (text.isNotBlank()) {
                                IconButton(onClick = {
                                    text = ""
                                    viewModel.clearSearch()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Löschen")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isSearching -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                text.isBlank() -> {
                    EmptyStateView(
                        title = "Globale Suche",
                        message = "Durchsuche Arbeiten, Wissensdatenbank, Fehler und Projekte",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.results.isEmpty() -> {
                    EmptyStateView(
                        title = "Keine Ergebnisse",
                        message = "Für '$text' wurde nichts gefunden",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.results) { result ->
                            SearchResultCard(
                                result = result,
                                onClick = {
                                    when (result.type) {
                                        "Arbeit" -> navController.navigate(Screen.TaskDetail.createRoute(result.id))
                                        "Wissen" -> navController.navigate(Screen.Knowledge.route)
                                        "Projekt" -> navController.navigate(Screen.ProjectDetail.createRoute(result.id))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: com.docai.kabelkopf.data.repository.GlobalSearchResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
            val icon = when (result.type) {
                "Arbeit" -> Icons.Default.Work
                "Wissen" -> Icons.Default.MenuBook
                "Fehler" -> Icons.Default.BugReport
                "Projekt" -> Icons.Default.Business
                else -> Icons.Default.Search
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = result.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
