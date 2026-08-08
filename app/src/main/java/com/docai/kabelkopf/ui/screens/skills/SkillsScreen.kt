package com.docai.kabelkopf.ui.screens.skills

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.docai.kabelkopf.ui.components.BottomNavBar
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.ui.theme.GreenSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(
    navController: NavController,
    viewModel: SkillsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skills & Erfahrung") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "skills",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Gesamtlevel ${state.totalLevel}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.skills.size} Skills · Nächstes Level: ${nextLevelProgress(state.skills)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            items(state.skills, key = { it.id }) { skill ->
                SkillCard(skill = skill)
            }
        }
    }
}

@Composable
private fun SkillCard(skill: com.docai.kabelkopf.data.local.entity.SkillEntity) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (skill.description.isNotBlank()) {
                        Text(
                            text = skill.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Lv. ${skill.level}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = levelColor(skill.level)
                    )
                    if (!skill.isMaxLevel) {
                        Text(
                            text = "→ Lv. ${skill.level + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!skill.isMaxLevel) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { skill.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = levelColor(skill.level),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${skill.currentXp} / ${skill.nextLevelXp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Max Level erreicht!",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenSuccess
                    )
                }
            }
        }
    }
}

private fun levelColor(level: Int): Color {
    return when {
        level >= 8 -> Color(0xFF9C27B0)
        level >= 5 -> Color(0xFFFF9800)
        level >= 3 -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }
}

private fun nextLevelProgress(skills: List<com.docai.kabelkopf.data.local.entity.SkillEntity>): Int {
    if (skills.isEmpty()) return 0
    val nonMax = skills.filter { !it.isMaxLevel }
    if (nonMax.isEmpty()) return 100
    return ((nonMax.sumOf { it.currentXp }.toFloat() / nonMax.sumOf { it.nextLevelXp }) * 100).toInt().coerceAtMost(100)
}
