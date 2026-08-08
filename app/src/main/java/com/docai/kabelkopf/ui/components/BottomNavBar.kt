package com.docai.kabelkopf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == "projects",
            onClick = { onNavigate("projects") },
            icon = { Icon(Icons.Default.Business, contentDescription = "Baustellen") },
            label = { Text("Baustellen") }
        )
        NavigationBarItem(
            selected = currentRoute == "knowledge",
            onClick = { onNavigate("knowledge") },
            icon = { Icon(Icons.Default.Book, contentDescription = "Wissen") },
            label = { Text("Wissen") }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") }
        )
    }
}
