package com.docai.kabelkopf.ui.screens.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.ui.components.BottomNavBar
import com.docai.kabelkopf.ui.components.EmptyStateView
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.ui.theme.GreenSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verlauf") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "projects",
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                        "projects" -> navController.navigate(Screen.Projects.route) { popUpTo(Screen.Projects.route) { inclusive = true } }
                        "knowledge" -> navController.navigate(Screen.Knowledge.route)
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
                placeholder = { Text("Arbeiten durchsuchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.showOnlyActive,
                    onClick = { viewModel.toggleActiveFilter() },
                    label = { Text("Aktiv") },
                    leadingIcon = if (state.showOnlyActive) {
                        { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                state.categories.forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            if (state.tasks.isEmpty()) {
                EmptyStateView(
                    title = "Keine Einträge",
                    message = "Noch keine Arbeiten dokumentiert",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.tasks) { task ->
                        HistoryTaskCard(
                            task = task,
                            onClick = {
                                navController.navigate(Screen.TaskDetail.createRoute(task.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTaskCard(
    task: WorkTaskEntity,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = task.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = task.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (task.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Erledigt",
                    tint = GreenSuccess
                )
            }
        }
    }
}
