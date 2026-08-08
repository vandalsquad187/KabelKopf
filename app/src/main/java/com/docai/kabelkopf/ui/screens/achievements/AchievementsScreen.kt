package com.docai.kabelkopf.ui.screens.achievements

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
fun AchievementsScreen(
    navController: NavController,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Erfolge") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "achievements",
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${state.unlockedCount} / ${state.totalCount} Erfolge",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            LinearProgressIndicator(
                                progress = {
                                    if (state.totalCount > 0) state.unlockedCount.toFloat() / state.totalCount
                                    else 0f
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            val grouped = state.achievements.groupBy { it.tier }
            listOf(3, 2, 1, 0).forEach { tier ->
                val items = grouped[tier].orEmpty()
                if (items.isNotEmpty()) {
                    item {
                        val label = when (tier) {
                            3 -> "Diamant"
                            2 -> "Gold"
                            1 -> "Silber"
                            else -> "Bronze"
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleLarge,
                            color = tierColor(tier)
                        )
                    }
                    items(items) { achievement ->
                        AchievementCard(achievement)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: com.docai.kabelkopf.data.local.entity.AchievementEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                tierColor(achievement.tier).copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (achievement.isUnlocked) Icons.Default.EmojiEvents
                    else Icons.Default.Lock,
                contentDescription = null,
                tint = if (achievement.isUnlocked) tierColor(achievement.tier)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
            if (achievement.isUnlocked) {
                Text(
                    text = achievement.tierLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = tierColor(achievement.tier),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun tierColor(tier: Int): Color = when (tier) {
    3 -> Color(0xFF00BCD4)
    2 -> Color(0xFFFFD700)
    1 -> Color(0xFFC0C0C0)
    else -> Color(0xFFCD7F32)
}
