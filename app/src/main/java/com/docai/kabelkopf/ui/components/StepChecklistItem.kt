package com.docai.kabelkopf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StepChecklistItem(
    stepNumber: Int,
    description: String,
    isDone: Boolean,
    warning: String?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isDone -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        warning != null -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = "$stepNumber.",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDone)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDone)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                if (warning != null && !isDone) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ $warning",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
