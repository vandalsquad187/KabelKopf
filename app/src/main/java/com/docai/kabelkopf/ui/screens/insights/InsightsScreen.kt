package com.docai.kabelkopf.ui.screens.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
fun InsightsScreen(
    navController: NavController,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Muster & Erkenntnisse") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "insights",
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
        val insights = state.insights

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (insights == null || insights.totalErrors == 0) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Noch keine Daten für Musteranalyse",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dokumentiere Fehler, um Muster zu erkennen!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { VerbesserungsvorschlagCard(insights.suggestion) }

                item { Text("Fehler nach Kategorie", style = MaterialTheme.typography.titleLarge) }
                items(insights.categoryInsights) { cat ->
                    CategoryInsightCard(cat)
                }

                if (insights.topCausesOverall.isNotEmpty()) {
                    item { Text("Häufigste Ursachen", style = MaterialTheme.typography.titleLarge) }
                    items(insights.topCausesOverall.take(5)) { cause ->
                        CauseCard(cause)
                    }
                }

                if (insights.lessonsLearned.isNotEmpty()) {
                    item { Text("Gelernte Lektionen", style = MaterialTheme.typography.titleLarge) }
                    items(insights.lessonsLearned) { error ->
                        LessonCard(error)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun VerbesserungsvorschlagCard(suggestion: String) {
    if (suggestion.isBlank()) return
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun CategoryInsightCard(
    cat: com.docai.kabelkopf.data.repository.CategoryInsight
) {
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
                    text = cat.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Badge(
                    containerColor = if (cat.totalErrors > 3) ErrorSeverityHigh
                        else ErrorSeverityLow
                ) {
                    Text("${cat.totalErrors} Fehler")
                }
            }

            if (cat.topCauses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Häufigste Ursachen:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                cat.topCauses.forEach { cause ->
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(6.dp),
                            tint = ErrorSeverityMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${cause.label} (${cause.count}x)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (cat.hasSolution) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = GreenSuccess
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lösungen dokumentiert",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun CauseCard(cause: com.docai.kabelkopf.data.repository.CauseFrequency) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = ErrorSeverityMedium,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = cause.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Badge { Text("${cause.count}x") }
        }
    }
}

@Composable
private fun LessonCard(
    error: com.docai.kabelkopf.data.local.entity.ErrorLogEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GreenSuccess.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GreenSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = error.taskCategory,
                    style = MaterialTheme.typography.labelLarge,
                    color = GreenSuccess
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "→ ${error.solution}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
