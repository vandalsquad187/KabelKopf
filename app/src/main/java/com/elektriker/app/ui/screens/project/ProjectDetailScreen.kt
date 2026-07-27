package com.elektriker.app.ui.screens.project

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.elektriker.app.data.local.entity.ProjectAssetEntity
import com.elektriker.app.ui.components.EmptyStateView
import com.elektriker.app.ui.navigation.Screen
import com.elektriker.app.ui.theme.GreenSuccess
import com.elektriker.app.util.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    navController: NavController,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.project?.name ?: "Projekt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { viewModel.toggleMenu() }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mehr")
                        }
                        DropdownMenu(
                            expanded = state.showMenu,
                            onDismissRequest = { viewModel.dismissMenu() }
                        ) {
                            DropdownMenuItem(
                                text = { Text("UV hinzufügen") },
                                onClick = { viewModel.showAddUvDialog() },
                                leadingIcon = {
                                    Icon(Icons.Default.ElectricalServices, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Unterlage hinzufügen") },
                                onClick = { viewModel.showAddDocumentDialog() },
                                leadingIcon = {
                                    Icon(Icons.Default.Description, contentDescription = null)
                                }
                            )
                        }
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.NewTaskWithProject.createRoute(projectId))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Neue Arbeit")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.project?.let { project ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (project.address.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = project.address,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            if (project.contactName.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = project.contactName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${state.totalTasks}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Arbeiten",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = GreenSuccess.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${state.completedTasks}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = GreenSuccess
                                )
                                Text(
                                    text = "erledigt",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GreenSuccess
                                )
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${state.assets.size}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Anlagen",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                if (state.assets.isNotEmpty()) {
                    item {
                        Text(
                            text = "Anlagen",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(state.assets) { asset ->
                        AssetCard(
                            asset = asset,
                            onClick = { viewModel.selectAsset(asset) }
                        )
                    }
                }

                item {
                    Text(
                        text = "Arbeiten",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (state.tasks.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "Noch keine Arbeiten",
                            message = "Tippe auf + oder ⋮, um die erste Arbeit oder Anlage zu erfassen"
                        )
                    }
                } else {
                    items(state.tasks) { task ->
                        TaskCard(
                            title = task.title,
                            category = task.category,
                            isCompleted = task.isCompleted,
                            onClick = {
                                navController.navigate(Screen.TaskDetail.createRoute(task.id))
                            }
                        )
                    }
                }
            }
        }
    }

    if (state.showAddUvDialog) {
        AddUvDialog(
            editingAsset = state.editingAsset,
            onDismiss = { viewModel.hideAddUvDialog() },
            onSave = { title, location, fuseType, circuitInfo, uvType, description, notes, photoPaths, docPaths ->
                viewModel.saveUv(title, location, fuseType, circuitInfo, uvType, description, notes, photoPaths, docPaths)
            }
        )
    }

    if (state.showAddDocumentDialog) {
        AddDocumentDialog(
            editingAsset = state.editingAsset,
            onDismiss = { viewModel.hideAddDocumentDialog() },
            onSave = { title, notes, photoPaths, docPaths ->
                viewModel.saveDocument(title, notes, photoPaths, docPaths)
            }
        )
    }

    state.selectedAsset?.let { asset ->
        AssetDetailDialog(
            asset = asset,
            onDismiss = { viewModel.clearSelectedAsset() },
            onDelete = { viewModel.deleteAsset(asset.id) },
            onEdit = { viewModel.startEditAsset(asset) }
        )
    }
}

@Composable
private fun AssetCard(
    asset: ProjectAssetEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (asset.type == "uv") Icons.Default.ElectricalServices else Icons.Default.Description
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (asset.location.isNotBlank()) {
                    Text(
                        text = asset.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val detail = buildString {
                    if (asset.fuseType.isNotBlank()) append("${asset.fuseType} · ")
                    if (asset.uvType.isNotBlank()) append(asset.uvType)
                }
                if (detail.isNotBlank()) {
                    Text(
                        text = detail.trimEnd(' ', '·'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUvDialog(
    editingAsset: ProjectAssetEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, location: String, fuseType: String, circuitInfo: String, uvType: String, description: String, notes: String, photoPaths: List<String>, docPaths: List<String>) -> Unit
) {
    val context = LocalContext.current
    val isEditing = editingAsset != null

    var title by remember { mutableStateOf(editingAsset?.title ?: "") }
    var location by remember { mutableStateOf(editingAsset?.location ?: "") }
    var fuseType by remember { mutableStateOf(editingAsset?.fuseType ?: "") }
    var fuseExpanded by remember { mutableStateOf(false) }
    var circuitInfo by remember { mutableStateOf(editingAsset?.circuitInfo ?: "") }
    var uvType by remember { mutableStateOf(editingAsset?.uvType ?: "") }
    var uvTypeExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(editingAsset?.description ?: "") }
    var notes by remember { mutableStateOf(editingAsset?.notes ?: "") }

    val photoUris = remember {
        mutableStateListOf<String>().apply {
            if (editingAsset != null) {
                addAll(editingAsset.photoPaths.split(",").filter { it.isNotBlank() })
            }
        }
    }
    val docUris = remember {
        mutableStateListOf<String>().apply {
            if (editingAsset != null) {
                addAll(editingAsset.docPaths.split(",").filter { it.isNotBlank() })
            }
        }
    }

    val fuseOptions = listOf("B13A", "B16A", "B20A", "B25A", "B32A", "C16A", "C20A", "C25A", "C32A", "C63A")
    val uvTypeOptions = listOf("Unterverteilung", "Zählerschrank", "AP-Feld", "Sicherungskasten", "Verteiler")

    var _tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && _tempPhotoUri != null) {
            val internalPath = FileUtils.copyUriToFile(context, _tempPhotoUri!!, FileUtils.createAssetPhotoFile(context))
            photoUris.add(internalPath)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val internalPath = FileUtils.copyUriToFile(context, it, FileUtils.createAssetPhotoFile(context))
            photoUris.add(internalPath)
        }
    }

    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val ext = it.toString().substringAfterLast(".", "pdf")
            val internalPath = FileUtils.copyUriToFile(context, it, FileUtils.createAssetDocFile(context, ext))
            docUris.add(internalPath)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "UV bearbeiten" else "UV hinzufügen") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel *") },
                    placeholder = { Text("z.B. UV-01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Standort") },
                    placeholder = { Text("z.B. Halle D, Raum 101") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = fuseExpanded,
                        onExpandedChange = { fuseExpanded = !fuseExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = fuseType,
                            onValueChange = { fuseType = it },
                            readOnly = false,
                            label = { Text("Sicherung") },
                            placeholder = { Text("z.B. B16A") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuseExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = fuseExpanded,
                            onDismissRequest = { fuseExpanded = false }
                        ) {
                            fuseOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        fuseType = option
                                        fuseExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = uvTypeExpanded,
                        onExpandedChange = { uvTypeExpanded = !uvTypeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uvType,
                            onValueChange = { uvType = it },
                            readOnly = false,
                            label = { Text("Typ") },
                            placeholder = { Text("UV-Typ") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uvTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = uvTypeExpanded,
                            onDismissRequest = { uvTypeExpanded = false }
                        ) {
                            uvTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        uvType = option
                                        uvTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = circuitInfo,
                    onValueChange = { circuitInfo = it },
                    label = { Text("Stromkreis") },
                    placeholder = { Text("z.B. Beleuchtung, Steckdosen Nord") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Legende / Beschreibung") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )

                Text("Fotos", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoUris.toList()) { uri ->
                        Box(modifier = Modifier.size(80.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val photoFile = File.createTempFile("uv_photo_", ".jpg", context.cacheDir)
                                    _tempPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                    cameraLauncher.launch(_tempPhotoUri!!)
                                },
                                modifier = Modifier.height(80.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                    Text("Kamera", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.height(80.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                    Text("Galerie", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Text("Schaltpläne / Dokumente", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(docUris.toList()) { uri ->
                        Card(modifier = Modifier.height(80.dp).width(120.dp)) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uri.substringAfterLast("/").take(20),
                                    style = MaterialTheme.typography.labelSmall,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = { docLauncher.launch("*/*") },
                            modifier = Modifier.height(80.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                Text("Dokument", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(title, location, fuseType, circuitInfo, uvType, description, notes, photoUris.toList(), docUris.toList())
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (isEditing) "Aktualisieren" else "Speichern")
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
private fun AddDocumentDialog(
    editingAsset: ProjectAssetEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, notes: String, photoPaths: List<String>, docPaths: List<String>) -> Unit
) {
    val context = LocalContext.current
    val isEditing = editingAsset != null

    var title by remember { mutableStateOf(editingAsset?.title ?: "") }
    var notes by remember { mutableStateOf(editingAsset?.notes ?: "") }

    val photoUris = remember {
        mutableStateListOf<String>().apply {
            if (editingAsset != null) {
                addAll(editingAsset.photoPaths.split(",").filter { it.isNotBlank() })
            }
        }
    }
    val docUris = remember {
        mutableStateListOf<String>().apply {
            if (editingAsset != null) {
                addAll(editingAsset.docPaths.split(",").filter { it.isNotBlank() })
            }
        }
    }

    var _tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && _tempPhotoUri != null) {
            val internalPath = FileUtils.copyUriToFile(context, _tempPhotoUri!!, FileUtils.createAssetPhotoFile(context))
            photoUris.add(internalPath)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val internalPath = FileUtils.copyUriToFile(context, it, FileUtils.createAssetPhotoFile(context))
            photoUris.add(internalPath)
        }
    }

    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val ext = it.toString().substringAfterLast(".", "pdf")
            val internalPath = FileUtils.copyUriToFile(context, it, FileUtils.createAssetDocFile(context, ext))
            docUris.add(internalPath)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Unterlage bearbeiten" else "Unterlage hinzufügen") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel *") },
                    placeholder = { Text("z.B. Schaltplan Halle D") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Dateien", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoUris.toList()) { uri ->
                        Box(modifier = Modifier.size(80.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    items(docUris.toList()) { uri ->
                        Card(modifier = Modifier.height(80.dp).width(120.dp)) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uri.substringAfterLast("/").take(20),
                                    style = MaterialTheme.typography.labelSmall,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val photoFile = File.createTempFile("doc_photo_", ".jpg", context.cacheDir)
                                        _tempPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                        cameraLauncher.launch(_tempPhotoUri!!)
                                    },
                                    modifier = Modifier.height(70.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("Foto", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier.height(70.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("Galerie", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                OutlinedButton(
                                    onClick = { docLauncher.launch("application/pdf") },
                                    modifier = Modifier.height(70.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("PDF", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, notes, photoUris.toList(), docUris.toList()) },
                enabled = title.isNotBlank()
            ) {
                Text(if (isEditing) "Aktualisieren" else "Speichern")
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
private fun AssetDetailDialog(
    asset: ProjectAssetEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    var showFullscreenPhoto by remember { mutableStateOf<String?>(null) }

    fun openInGallery(uri: String) {
        try {
            val file = File(uri)
            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Konnte Foto nicht öffnen", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(asset.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (asset.type == "uv") {
                    if (asset.location.isNotBlank()) {
                        DetailRow("Standort", asset.location)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (asset.fuseType.isNotBlank()) {
                            DetailRow("Sicherung", asset.fuseType, modifier = Modifier.weight(1f))
                        }
                        if (asset.uvType.isNotBlank()) {
                            DetailRow("Typ", asset.uvType, modifier = Modifier.weight(1f))
                        }
                    }
                    if (asset.circuitInfo.isNotBlank()) {
                        DetailRow("Stromkreis", asset.circuitInfo)
                    }
                    if (asset.description.isNotBlank()) {
                        DetailRow("Legende", asset.description)
                    }
                }

                val photos = asset.photoPaths.split(",").filter { it.isNotBlank() }
                if (photos.isNotEmpty()) {
                    Text("Fotos (tippen zum Vergrößern)", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(photos) { uri ->
                            Card(
                                onClick = { showFullscreenPhoto = uri },
                                modifier = Modifier.size(120.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                val docs = asset.docPaths.split(",").filter { it.isNotBlank() }
                if (docs.isNotEmpty()) {
                    Text("Dokumente", style = MaterialTheme.typography.labelLarge)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        docs.forEach { uri ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(uri), "*/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Konnte Datei nicht öffnen", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uri.substringAfterLast("/").take(30),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (asset.notes.isNotBlank()) {
                    DetailRow("Notizen", asset.notes)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bearbeiten")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Löschen")
                }
            }
        }
    )

    showFullscreenPhoto?.let { uri ->
        AlertDialog(
            onDismissRequest = { showFullscreenPhoto = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Foto", modifier = Modifier.weight(1f))
                    IconButton(onClick = { showFullscreenPhoto = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Schließen")
                    }
                }
            },
            text = {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { showFullscreenPhoto?.let { openInGallery(it) } }) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("In Galerie öffnen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFullscreenPhoto = null }) {
                    Text("Schließen")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TaskCard(
    title: String,
    category: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Erledigt",
                    tint = GreenSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
