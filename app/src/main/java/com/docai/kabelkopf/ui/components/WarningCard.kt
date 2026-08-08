package com.docai.kabelkopf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.docai.kabelkopf.ui.theme.RedWarning
import com.docai.kabelkopf.ui.theme.YellowWarning

@Composable
fun WarningCard(
    title: String,
    description: String,
    severity: Int,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        severity >= 4 -> MaterialTheme.colorScheme.error
        severity >= 3 -> YellowWarning
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        severity >= 4 -> MaterialTheme.colorScheme.onError
        severity >= 3 -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val icon = when {
        severity >= 4 -> Icons.Default.Warning
        severity >= 3 -> Icons.Default.ErrorOutline
        else -> Icons.Default.Info
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}
