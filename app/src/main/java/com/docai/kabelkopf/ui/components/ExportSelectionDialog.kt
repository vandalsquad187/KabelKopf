package com.docai.kabelkopf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.docai.kabelkopf.service.ExportSelection

@Composable
fun ExportSelectionDialog(
    onDismiss: () -> Unit,
    onExport: (ExportSelection) -> Unit
) {
    var selection by remember { mutableStateOf(ExportSelection()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export auswählen") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Was möchtest du exportieren?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExportCheckbox("Wissen", selection.knowledge) { selection = selection.copy(knowledge = it) }
                ExportCheckbox("Fehler", selection.errors) { selection = selection.copy(errors = it) }
                ExportCheckbox("Fehler-Ursachen", selection.errorCauses) { selection = selection.copy(errorCauses = it) }
                ExportCheckbox("Baustellen", selection.projects) { selection = selection.copy(projects = it) }
                ExportCheckbox("Arbeiten", selection.tasks) { selection = selection.copy(tasks = it) }
                ExportCheckbox("Arbeitsschritte", selection.steps) { selection = selection.copy(steps = it) }
                ExportCheckbox("Fotos", selection.photos) { selection = selection.copy(photos = it) }
                ExportCheckbox("Checklisten", selection.checklists) { selection = selection.copy(checklists = it) }
                ExportCheckbox("Checklisten-Einträge", selection.checklistItems) { selection = selection.copy(checklistItems = it) }
                ExportCheckbox("Anlagen/Assets", selection.projectAssets) { selection = selection.copy(projectAssets = it) }
                ExportCheckbox("Skills", selection.skills) { selection = selection.copy(skills = it) }
                ExportCheckbox("Erfolge", selection.achievements) { selection = selection.copy(achievements = it) }
                ExportCheckbox("Vorlagen", selection.templates) { selection = selection.copy(templates = it) }
            }
        },
        confirmButton = {
            Button(
                onClick = { onExport(selection) }
            ) {
                Text("Exportieren")
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
private fun ExportCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
