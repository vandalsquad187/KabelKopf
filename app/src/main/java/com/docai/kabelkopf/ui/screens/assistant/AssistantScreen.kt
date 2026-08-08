package com.docai.kabelkopf.ui.screens.assistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import com.docai.kabelkopf.ui.components.StepChecklistItem
import com.docai.kabelkopf.ui.components.WarningCard
import com.docai.kabelkopf.ui.theme.GreenSuccess
import com.docai.kabelkopf.ui.theme.ErrorSeverityHigh
import com.docai.kabelkopf.ui.theme.ErrorSeverityMedium
import com.docai.kabelkopf.ui.theme.ErrorSeverityLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    taskId: String,
    navController: NavController,
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    if (state.showErrorDialog) {
        ErrorInputDialog(
            description = state.errorDescription,
            severity = state.errorSeverity,
            onDescriptionChange = { viewModel.updateErrorDescription(it) },
            onSeverityChange = { viewModel.updateErrorSeverity(it) },
            onSave = { viewModel.saveError() },
            onDismiss = { viewModel.hideErrorDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.task?.title ?: "Assistent",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showErrorDialog() }) {
                        Icon(Icons.Default.BugReport, contentDescription = "Fehler melden")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isComplete -> {
                CompleteScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onFinish = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            state.task == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (state.warnings.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    text = "Warnungen aus Fehlerhistorie",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            itemsIndexed(state.warnings) { _, warning ->
                                WarningCard(
                                    title = "⚠️ ${warning.description}",
                                    description = "Schwere: ${warning.severity}/5 · ${warning.taskCategory}",
                                    severity = warning.severity,
                                    modifier = Modifier.heightIn(min = 60.dp)
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Schritt ${state.currentStepIndex + 1} von ${state.steps.size}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        itemsIndexed(state.steps) { index, step ->
                            StepChecklistItem(
                                stepNumber = index + 1,
                                description = step.description,
                                isDone = step.isDone,
                                warning = step.warning,
                                onToggle = { viewModel.toggleStep(step.id) },
                                modifier = if (index == state.currentStepIndex && !step.isDone)
                                    Modifier.padding(vertical = 4.dp)
                                else
                                    Modifier
                            )
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (state.currentStepIndex > 0) {
                                    OutlinedButton(
                                        onClick = { viewModel.goToStep(state.currentStepIndex - 1) }
                                    ) {
                                        Icon(Icons.Default.SkipPrevious, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Vorheriger")
                                    }
                                }
                                if (state.currentStepIndex < state.steps.size - 1) {
                                    Button(
                                        onClick = { viewModel.goToStep(state.currentStepIndex + 1) }
                                    ) {
                                        Text("Nächster")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.SkipNext, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompleteScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GreenSuccess,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Arbeit abgeschlossen!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Alle Schritte erledigt. Die Arbeit wurde als abgeschlossen markiert.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        FilledTonalButton(
            onClick = onFinish,
            modifier = Modifier.height(56.dp).width(200.dp)
        ) {
            Text("Fertig", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ErrorInputDialog(
    description: String,
    severity: Int,
    onDescriptionChange: (String) -> Unit,
    onSeverityChange: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fehler dokumentieren") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Was ist passiert? Diese Information hilft beim nächsten Mal.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Fehlerbeschreibung") },
                    placeholder = { Text("z.B. N und PE vertauscht") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Schweregrad",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeverityButton("Niedrig", 1, severity, ErrorSeverityLow, onSeverityChange)
                    SeverityButton("Mittel", 3, severity, ErrorSeverityMedium, onSeverityChange)
                    SeverityButton("Hoch", 5, severity, ErrorSeverityHigh, onSeverityChange)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = description.isNotBlank()
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
private fun SeverityButton(
    label: String,
    value: Int,
    current: Int,
    color: androidx.compose.ui.graphics.Color,
    onClick: (Int) -> Unit
) {
    FilterChip(
        selected = current == value,
        onClick = { onClick(value) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.3f)
        )
    )
}
