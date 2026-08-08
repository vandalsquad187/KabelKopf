package com.docai.kabelkopf.ui.screens.errorhistory

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
import com.docai.kabelkopf.ui.components.BottomNavBar
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.ui.theme.ErrorSeverityHigh
import com.docai.kabelkopf.ui.theme.ErrorSeverityLow
import com.docai.kabelkopf.ui.theme.ErrorSeverityMedium
import com.docai.kabelkopf.ui.theme.GreenSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorHistoryScreen(
    navController: NavController,
    viewModel: ErrorHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fehlerhistorie") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "errorhistory",
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                        "projects" -> navController.navigate(Screen.Projects.route)
                        "new_task" -> navController.navigate(Screen.NewTask.route)
                        "knowledge" -> navController.navigate(Screen.Knowledge.route)
                        "profile" -> navController.navigate(Screen.Profile.route)
                    }
                }
            )
        }
    ) { padding ->
        if (state.errors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = GreenSuccess
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Noch keine Fehler dokumentiert",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Alle dokumentierten Fehler (${state.errors.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(state.errors, key = { it.id }) { error ->
                    ErrorHistoryCard(
                        error = error,
                        causeLabel = { id -> state.causeLabels[id] }
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorHistoryCard(
    error: com.docai.kabelkopf.data.local.entity.ErrorLogEntity,
    causeLabel: (String) -> String?
) {
    val severityColor = when {
        error.severity >= 5 -> ErrorSeverityHigh
        error.severity >= 3 -> ErrorSeverityMedium
        else -> ErrorSeverityLow
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = error.taskCategory,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = severityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = when {
                            error.severity >= 5 -> "Hoch"
                            error.severity >= 3 -> "Mittel"
                            else -> "Niedrig"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error.description,
                style = MaterialTheme.typography.bodyMedium
            )

            val selectedCauses = error.causeIds
                .split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { causeLabel(it) }

            if (selectedCauses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ursachen:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                selectedCauses.forEach { label ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(6.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (error.solution.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lösung: ${error.solution}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenSuccess
                )
            }
        }
    }
}
