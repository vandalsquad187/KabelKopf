package com.docai.kabelkopf.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.docai.kabelkopf.ui.components.BottomNavBar
import com.docai.kabelkopf.ui.components.ExportSelectionDialog
import com.docai.kabelkopf.ui.components.ImportPreviewDialog
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    val importPreview by viewModel.importPreview.collectAsStateWithLifecycle()
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            val selection = com.docai.kabelkopf.service.ExportSelection()
            viewModel.exportKKb(selection, it)
        }
    }

    val exportSelectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            showExportDialog = true
        }
    }

    var pendingExportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { pendingExportUri = it; showExportDialog = true }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadImportPreview(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.importResult.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    importPreview?.let { preview ->
        ImportPreviewDialog(
            preview = preview,
            onDismiss = { viewModel.dismissImportPreview() },
            onImport = { viewModel.confirmImport() }
        )
    }

    if (showExportDialog) {
        ExportSelectionDialog(
            onDismiss = { showExportDialog = false },
            onExport = { selection ->
                showExportDialog = false
                pendingExportUri?.let { uri ->
                    viewModel.exportKKb(selection, uri)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "profile",
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                        "projects" -> navController.navigate(Screen.Projects.route)
                        "knowledge" -> navController.navigate(Screen.Knowledge.route)
                        "profile" -> {}
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Deine Statistik",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Abgeschlossen",
                        value = "${state.completedTasks}",
                        icon = Icons.Default.CheckCircle,
                        color = GreenSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    @Suppress("DEPRECATION")
                    val trendingUpIcon = Icons.Default.TrendingUp
                    StatCard(
                        title = "Erledigungsrate",
                        value = "${(state.completionRate * 100).toInt()}%",
                        icon = trendingUpIcon,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.Checklists.route) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Checklist, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checklisten")
                }
            }

            item {
                Button(
                    onClick = { navController.navigate(Screen.Skills.route) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Skills & Erfahrung")
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fortschritt", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${state.totalXp} XP · ${state.overallProgress.toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.overallProgress / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        state.skillProgress.take(4).forEach { skill ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    skill.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "Lv.${skill.level} ${skill.progressPercent.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.navigate(Screen.Achievements.route) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Erfolge")
                }
            }

            item {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.ErrorHistory.route) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    @Suppress("DEPRECATION")
                    val listIcon = Icons.Default.List
                    Icon(listIcon, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Alle Fehler anzeigen")
                }
            }

            item {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.Insights.route) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Insights, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Muster & Erkenntnisse")
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = "Backup & Daten",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                OutlinedButton(
                    onClick = { exportFileLauncher.launch("kabelkopf-export.kkb") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Upload, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isExporting) "Exportiere..." else "Backup exportieren")
                }
            }

            item {
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/zip")) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isImporting
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isImporting) "Importiere..." else "Backup importieren")
                }
            }

            item {
                Text(
                    text = "Tipp: Vor einem Geräte-Wechsel oder App-Deinstallation " +
                        "unbedingt ein Backup exportieren. Alle Daten liegen nur lokal " +
                        "auf diesem Gerät.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Text(
                    text = "Über Kabel-Kopf",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:")
                            putExtra(
                                android.content.Intent.EXTRA_EMAIL,
                                arrayOf("feedback@kabelkopf.app")
                            )
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback zu Kabel-Kopf")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Keine E-Mail-App gefunden", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Feedback, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Feedback senden")
                }
            }

            item {
                Text(
                    text = "Version ${com.docai.kabelkopf.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryBar(
    category: String,
    count: Int,
    maxCount: Int
) {
    val fraction = count.toFloat() / maxCount.toFloat()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
