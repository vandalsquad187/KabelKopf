package com.docai.kabelkopf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuickActionBar(
    onVoiceNote: () -> Unit,
    onPhoto: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onVoiceNote,
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Sprachnotiz",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sprache", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = onPhoto,
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Foto",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Foto", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = onComplete,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Fertig",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fertig", style = MaterialTheme.typography.labelLarge)
        }
    }
}
