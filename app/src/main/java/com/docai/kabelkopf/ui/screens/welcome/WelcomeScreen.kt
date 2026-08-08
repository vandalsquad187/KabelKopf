package com.docai.kabelkopf.ui.screens.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.docai.kabelkopf.ui.navigation.Screen
import com.docai.kabelkopf.ui.theme.Orange700

@Composable
fun WelcomeScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = Orange700,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "KabelKopf",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Orange700
            )

            Text(
                text = "Dein Elektro-Assistent",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            WelcomeFeature(
                icon = Icons.Default.WorkHistory,
                title = "Arbeiten dokumentieren",
                description = "Erfasse deine täglichen Aufgaben mit Schritten, Fotos und Sprachnotizen"
            )

            Spacer(modifier = Modifier.height(16.dp))

            WelcomeFeature(
                icon = Icons.Default.BugReport,
                title = "Aus Fehlern lernen",
                description = "Dokumentiere Fehler und erhalte beim nächsten Mal passende Warnungen"
            )

            Spacer(modifier = Modifier.height(16.dp))

            WelcomeFeature(
                icon = Icons.Default.AutoAwesome,
                title = "Skills entwickeln",
                description = "Sammle Erfahrungspunkte, schalte Erfolge frei und verbessere dich"
            )

            Spacer(modifier = Modifier.height(16.dp))

            WelcomeFeature(
                icon = Icons.Default.Business,
                title = "Baustellen verwalten",
                description = "Fasse Arbeiten pro Projekt zusammen und behalte den Überblick"
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange700
                )
            ) {
                Text(
                    text = "Los geht's!",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun WelcomeFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
