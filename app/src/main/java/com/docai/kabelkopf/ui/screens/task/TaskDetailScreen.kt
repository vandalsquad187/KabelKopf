package com.docai.kabelkopf.ui.screens.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.ui.components.BottomNavBar
import com.docai.kabelkopf.ui.components.StepChecklistItem
import com.docai.kabelkopf.ui.components.WarningCard
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.ui.theme.ErrorSeverityHigh
import com.docai.kabelkopf.ui.theme.ErrorSeverityLow
import com.docai.kabelkopf.ui.theme.ErrorSeverityMedium
import com.docai.kabelkopf.ui.theme.GreenSuccess
import com.docai.kabelkopf.util.Constants
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val task by viewModel.task.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val warnings by viewModel.warnings.collectAsStateWithLifecycle()
    val isEditing by viewModel.isEditing.collectAsStateWithLifecycle()
    val editState by viewModel.state.collectAsStateWithLifecycle()
    val editErrorState by viewModel.editErrorDialog.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Bearbeiten" else (task?.title ?: "Laden..."))
                },
                navigationIcon = {
                    @Suppress("DEPRECATION")
                    val backIcon = if (isEditing) Icons.Default.Close else Icons.Default.ArrowBack
                    IconButton(onClick = {
                        if (isEditing) viewModel.cancelEdit()
                        else navController.popBackStack()
                    }) {
                        Icon(
                            backIcon,
                            contentDescription = if (isEditing) "Abbrechen" else "Zurück"
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = { viewModel.confirmEdit() }) {
                            Text("Speichern")
                        }
                    } else {
                        IconButton(onClick = { viewModel.startEdit() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isEditing) {
                Surface(
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.confirmEdit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        enabled = editState.title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Speichern", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } else {
                BottomNavBar(
                    currentRoute = "home",
                    onNavigate = { route ->
                        when (route) {
                            "home" -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                            "projects" -> navController.navigate(Screen.Projects.route)
                            "knowledge" -> navController.navigate(Screen.Knowledge.route)
                            "profile" -> navController.navigate(Screen.Profile.route)
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (isEditing) {
            EditTaskContent(
                editState = editState,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            val showRating = androidx.compose.runtime.remember(task) {
                task?.isCompleted == true
            }
            ReadOnlyTaskContent(
                task = task,
                steps = steps,
                warnings = warnings,
                taskId = taskId,
                onToggleStep = { stepId, done -> viewModel.toggleStepDone(stepId, done) },
                onNavigateAssistant = {
                    navController.navigate(Screen.Assistant.createRoute(taskId))
                },
                onExportPdf = { viewModel.exportPdf() },
                onRatingChange = { rating ->
                    viewModel.saveRating(rating)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }

    if (editErrorState.showDialog) {
        EditErrorInputDialog(
            description = editErrorState.description,
            severity = editErrorState.severity,
            solution = editErrorState.solution,
            availableCauses = editErrorState.availableCauses,
            selectedCauseIds = editErrorState.selectedCauseIds,
            onDescriptionChange = { viewModel.updateEditErrorDescription(it) },
            onSeverityChange = { viewModel.updateEditErrorSeverity(it) },
            onSolutionChange = { viewModel.updateEditErrorSolution(it) },
            onCauseToggle = { viewModel.toggleEditErrorCause(it) },
            onSave = { viewModel.saveEditError() },
            onDismiss = { viewModel.hideEditErrorDialog() }
        )
    }
}

@Composable
private fun ReadOnlyTaskContent(
    task: WorkTaskEntity?,
    steps: List<WorkStepEntity>,
    warnings: List<com.docai.kabelkopf.data.local.entity.ErrorLogEntity>,
    taskId: String,
    onToggleStep: (String, Boolean) -> Unit,
    onNavigateAssistant: () -> Unit,
    onExportPdf: () -> Unit,
    onRatingChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        task?.let { t ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = t.category,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (t.isCompleted) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Erledigt",
                                    tint = GreenSuccess
                                )
                            }
                        }
                        if (t.location.isNotBlank()) {
                            Text(
                                text = t.location,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (t.durationSeconds > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDuration(t.durationSeconds),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (t.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = t.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (t.isCompleted && onRatingChange != null) {
                    StarRatingRow(
                        currentRating = t.rating,
                        onRatingChange = onRatingChange
                    )
                }
            }

            if (warnings.isNotEmpty()) {
                item {
                    Text(
                        text = "Erfahrungs-Warnungen",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                items(warnings) { warning ->
                    WarningCard(
                        title = "Fehler bei ${warning.taskCategory}",
                        description = warning.description,
                        severity = warning.severity
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Schritte",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = onExportPdf) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF")
                        }
                        if (steps.isNotEmpty()) {
                            FilledTonalButton(onClick = onNavigateAssistant) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Assistent")
                            }
                        }
                    }
                }
            }

            if (steps.isNotEmpty()) {
                items(steps) { step ->
                    StepChecklistItem(
                        stepNumber = step.stepOrder + 1,
                        description = step.description,
                        isDone = step.isDone,
                        warning = step.warning,
                        onToggle = { onToggleStep(step.id, !step.isDone) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTaskContent(
    editState: NewTaskUiState,
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = editState.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Titel *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = editState.customerName,
                onValueChange = { viewModel.updateCustomer(it) },
                label = { Text("Kunde") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = editState.location,
                onValueChange = { viewModel.updateLocation(it) },
                label = { Text("Ort") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        ExposedDropdownMenuBox(
            expanded = editState.showCategoryPicker,
            onExpandedChange = { viewModel.toggleCategoryPicker() }
        ) {
            OutlinedTextField(
                value = editState.category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Kategorie") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editState.showCategoryPicker) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
            )
            ExposedDropdownMenu(
                expanded = editState.showCategoryPicker,
                onDismissRequest = { viewModel.toggleCategoryPicker() }
            ) {
                Constants.Categories.all.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            viewModel.updateCategory(cat)
                            viewModel.toggleCategoryPicker()
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = editState.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text("Beschreibung") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Arbeitsschritte",
                style = MaterialTheme.typography.titleLarge
            )
            FilledTonalButton(onClick = { viewModel.addStep() }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schritt")
            }
        }

        editState.steps.forEachIndexed { index, step ->
            EditStepCard(
                step = step,
                stepNumber = index + 1,
                onUpdate = { desc, warn ->
                    viewModel.updateStep(index, desc, warn)
                },
                onRemove = { viewModel.removeStep(index) }
            )
        }

        if (editState.steps.isEmpty()) {
            Text(
                text = "Keine Arbeitsschritte vorhanden",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        OutlinedButton(
            onClick = { viewModel.showEditErrorDialog() },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.BugReport, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fehler dokumentieren")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EditStepCard(
    step: StepInput,
    stepNumber: Int,
    onUpdate: (String, String?) -> Unit,
    onRemove: () -> Unit
) {
    var description by remember(step.id) { mutableStateOf(step.description) }
    var warning by remember(step.id) { mutableStateOf(step.warning ?: "") }
    var expanded by remember { mutableStateOf(step.warning != null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$stepNumber.",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        onUpdate(it, warning.ifBlank { null })
                    },
                    placeholder = { Text("Schritt beschreiben") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Entfernen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            TextButton(onClick = { expanded = !expanded }) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (expanded) "Warnung ausblenden" else "Warnung hinzufügen")
            }
            if (expanded) {
                OutlinedTextField(
                    value = warning,
                    onValueChange = {
                        warning = it
                        onUpdate(description, it.ifBlank { null })
                    },
                    label = { Text("Sicherheitswarnung") },
                    placeholder = { Text("z.B. Spannung prüfen!") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ErrorSeverityMedium,
                        cursorColor = ErrorSeverityMedium
                    )
                )
            }
        }
    }
}

@Composable
private fun EditErrorInputDialog(
    description: String,
    severity: Int,
    solution: String,
    availableCauses: List<com.docai.kabelkopf.data.local.entity.ErrorCauseEntity>,
    selectedCauseIds: Set<String>,
    onDescriptionChange: (String) -> Unit,
    onSeverityChange: (Int) -> Unit,
    onSolutionChange: (String) -> Unit,
    onCauseToggle: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fehler dokumentieren") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                if (availableCauses.isNotEmpty()) {
                    Text(
                        text = "Mögliche Ursachen",
                        style = MaterialTheme.typography.labelLarge
                    )
                    availableCauses.forEach { cause ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = selectedCauseIds.contains(cause.id),
                                onCheckedChange = { onCauseToggle(cause.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = cause.label, style = MaterialTheme.typography.bodyMedium)
                                if (cause.description.isNotBlank()) {
                                    Text(
                                        text = cause.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = solution,
                    onValueChange = onSolutionChange,
                    label = { Text("Lösung / Was hast du daraus gelernt?") },
                    placeholder = { Text("z.B. Vor dem Abklemmen immer Spannung messen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )

                Text(
                    text = "Schweregrad",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeverityChip("Niedrig", 1, severity, ErrorSeverityLow, onSeverityChange)
                    SeverityChip("Mittel", 3, severity, ErrorSeverityMedium, onSeverityChange)
                    SeverityChip("Hoch", 5, severity, ErrorSeverityHigh, onSeverityChange)
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
private fun StarRatingRow(
    currentRating: Int,
    onRatingChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Arbeit bewerten",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (star in 1..5) {
                    IconButton(onClick = { onRatingChange(star) }) {
                        Icon(
                            imageVector = if (star <= currentRating) Icons.Default.Star
                                else Icons.Default.StarOutline,
                            contentDescription = "$star Stern${if (star > 1) "e" else ""}",
                            tint = if (star <= currentRating) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                if (currentRating > 0) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = when {
                            currentRating <= 2 -> "Mangelhaft"
                            currentRating == 3 -> "Befriedigend"
                            currentRating == 4 -> "Gut"
                            else -> "Sehr gut"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityChip(
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

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}min" else "${minutes} min"
}
