package com.docai.kabelkopf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.docai.kabelkopf.service.ImportPreview

@Composable
fun ImportPreviewDialog(
    preview: ImportPreview,
    onDismiss: () -> Unit,
    onImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import-Vorschau") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Backup erkannt",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Erstellt: ${preview.exportDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Version: Kabel-Kopf ${preview.appVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Enthält:", style = MaterialTheme.typography.titleSmall)

                if (preview.knowledgeCount > 0) ImportCountItem("Wissen", preview.knowledgeCount)
                if (preview.errorsCount > 0) ImportCountItem("Fehler", preview.errorsCount)
                if (preview.errorCausesCount > 0) ImportCountItem("Fehler-Ursachen", preview.errorCausesCount)
                if (preview.projectsCount > 0) ImportCountItem("Baustellen", preview.projectsCount)
                if (preview.tasksCount > 0) ImportCountItem("Arbeiten", preview.tasksCount)
                if (preview.stepsCount > 0) ImportCountItem("Arbeitsschritte", preview.stepsCount)
                if (preview.photosCount > 0) ImportCountItem("Fotos", preview.photosCount)
                if (preview.checklistsCount > 0) ImportCountItem("Checklisten", preview.checklistsCount)
                if (preview.checklistItemsCount > 0) ImportCountItem("Checklisten-Einträge", preview.checklistItemsCount)
                if (preview.projectAssetsCount > 0) ImportCountItem("Anlagen/Assets", preview.projectAssetsCount)
                if (preview.skillsCount > 0) ImportCountItem("Skills", preview.skillsCount)
                if (preview.achievementsCount > 0) ImportCountItem("Erfolge", preview.achievementsCount)
                if (preview.templatesCount > 0) ImportCountItem("Vorlagen", preview.templatesCount)

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Die Daten werden hinzugefügt (Merge). Bestehende Daten bleiben erhalten.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onImport
            ) {
                Text("Importieren")
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
private fun ImportCountItem(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("✓ $label", style = MaterialTheme.typography.bodyMedium)
        Text(
            "$count",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
