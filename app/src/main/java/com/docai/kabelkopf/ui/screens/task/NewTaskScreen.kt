package com.docai.kabelkopf.ui.screens.task

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.docai.kabelkopf.ui.components.WarningCard
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.ui.theme.ErrorSeverityHigh
import com.docai.kabelkopf.ui.theme.ErrorSeverityMedium
import com.docai.kabelkopf.ui.theme.GreenSuccess
import com.docai.kabelkopf.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(
    navController: NavController,
    preSelectedProjectId: String? = null,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val editErrorState by viewModel.editErrorDialog.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.savedTaskId) {
        state.savedTaskId?.let { taskId ->
            val count = state.suggestedKnowledge.size
            if (count > 0) {
                Toast.makeText(context,
                    "$count passende Wissenseinträge gefunden – siehe Wissensdatenbank",
                    Toast.LENGTH_LONG).show()
            }
            navController.navigate(Screen.Assistant.createRoute(taskId)) {
                popUpTo(Screen.Home.route)
            }
        }
    }

    LaunchedEffect(state.isTimerRunning) {
        while (state.isTimerRunning) {
            delay(1000)
            viewModel.tickTimer()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Foto aufgenommen", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, "Bild aus Galerie ausgewählt", Toast.LENGTH_SHORT).show()
        }
    }

    val voicePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.toggleRecording()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neue Arbeit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        @Suppress("DEPRECATION")
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.saveTask(projectId = preSelectedProjectId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    enabled = state.title.isNotBlank() && !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Arbeit starten", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Titel *") },
                placeholder = { Text("z.B. Unterverteilung verdrahtet") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.customerName,
                    onValueChange = { viewModel.updateCustomer(it) },
                    label = { Text("Kunde") },
                    placeholder = { Text("Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.location,
                    onValueChange = { viewModel.updateLocation(it) },
                    label = { Text("Ort") },
                    placeholder = { Text("Baustelle") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            ExposedDropdownMenuBox(
                expanded = state.showCategoryPicker,
                onExpandedChange = { viewModel.toggleCategoryPicker() }
            ) {
                OutlinedTextField(
                    value = state.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategorie") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showCategoryPicker) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = state.showCategoryPicker,
                    onDismissRequest = { viewModel.toggleCategoryPicker() }
                ) {
                    Constants.Categories.all.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                viewModel.updateCategory(cat)
                                viewModel.toggleCategoryPicker()
                                viewModel.loadWarningsForCategory(cat)
                            }
                        )
                    }
                }
            }

            if (state.templateSuggestions.isNotEmpty()) {
                Column {
                    Text(
                        text = "Vorlage verwenden?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.templateSuggestions.forEach { template ->
                            AssistChip(
                                onClick = { viewModel.applyTemplate(template) },
                                label = { Text(template.name, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Beschreibung") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(permission)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Foto aufnehmen",
                        modifier = Modifier.size(28.dp)
                    )
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = "Aus Galerie wählen",
                        modifier = Modifier.size(28.dp)
                    )
                }

                val voiceColor = when {
                    state.isRecording -> MaterialTheme.colorScheme.error
                    state.voiceNotePath != null -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.primary
                }

                OutlinedButton(
                    onClick = {
                        if (!state.isRecording) {
                            val permission = Manifest.permission.RECORD_AUDIO
                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.toggleRecording()
                            } else {
                                voicePermissionLauncher.launch(permission)
                            }
                        } else {
                            viewModel.toggleRecording()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = voiceColor
                    )
                ) {
                    if (state.isRecording) {
                        Icon(
                            Icons.Default.FiberManualRecord,
                            contentDescription = "Aufnahme läuft – Stoppen",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    } else if (state.voiceNotePath != null) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Aufnahme vorhanden",
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Sprachnotiz aufnehmen",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (state.voiceNotePath != null && !state.isRecording) {
                TextButton(onClick = { viewModel.togglePlayback() }) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (state.isPlaying) "Wiedergabe stoppen" else "Aufnahme abspielen")
                }
            }

            TimerSection(
                elapsedSeconds = state.timerElapsedSeconds,
                isRunning = state.isTimerRunning,
                onStart = { viewModel.startTimer() },
                onPause = { viewModel.pauseTimer() },
                onReset = { viewModel.resetTimer() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Arbeitsschritte",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { viewModel.showEditErrorDialog() }) {
                        Icon(Icons.Default.BugReport, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fehler", maxLines = 1, softWrap = false)
                    }
                    FilledTonalButton(onClick = { viewModel.addStep() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Schritt", maxLines = 1, softWrap = false)
                    }
                }
            }

            state.steps.forEachIndexed { index, step ->
                StepInputCard(
                    step = step,
                    stepNumber = index + 1,
                    onUpdate = { desc, warn ->
                        viewModel.updateStep(index, desc, warn)
                    },
                    onRemove = { viewModel.removeStep(index) }
                )
            }

            if (state.steps.isEmpty()) {
                Text(
                    text = "Optional: Füge Arbeitsschritte hinzu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
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
private fun StepInputCard(
    step: com.docai.kabelkopf.ui.screens.task.StepInput,
    stepNumber: Int,
    onUpdate: (String, String?) -> Unit,
    onRemove: () -> Unit
) {
    var description by remember(step.id) { mutableStateOf(step.description) }
    var warning by remember(step.id) { mutableStateOf(step.warning ?: "") }
    var expanded by remember { mutableStateOf(false) }

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
private fun TimerSection(
    elapsedSeconds: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isRunning) {
                    FilledTonalButton(onClick = onStart) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start")
                    }
                } else {
                    FilledTonalButton(onClick = onPause) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pause")
                    }
                }
                if (elapsedSeconds > 0) {
                    OutlinedButton(onClick = onReset) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                    SeverityChip("Niedrig", 1, severity, ErrorSeverityMedium, onSeverityChange)
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
