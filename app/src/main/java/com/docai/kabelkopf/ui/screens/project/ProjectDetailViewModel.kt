package com.docai.kabelkopf.ui.screens.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.ProjectAssetEntity
import com.docai.kabelkopf.data.local.entity.ProjectEntity
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.data.repository.ProjectAssetRepository
import com.docai.kabelkopf.data.repository.ProjectRepository
import com.docai.kabelkopf.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectDetailUiState(
    val project: ProjectEntity? = null,
    val tasks: List<WorkTaskEntity> = emptyList(),
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val assets: List<ProjectAssetEntity> = emptyList(),
    val showMenu: Boolean = false,
    val showAddUvDialog: Boolean = false,
    val showAddDocumentDialog: Boolean = false,
    val selectedAsset: ProjectAssetEntity? = null,
    val editingAsset: ProjectAssetEntity? = null
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val projectAssetRepository: ProjectAssetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectDetailUiState())
    val state: StateFlow<ProjectDetailUiState> = _state.asStateFlow()

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val project = projectRepository.getProjectById(projectId)
            _state.update { it.copy(project = project) }
        }
        viewModelScope.launch {
            taskRepository.getTasksByProjectId(projectId).collect { tasks ->
                _state.update {
                    it.copy(
                        tasks = tasks,
                        totalTasks = tasks.size,
                        completedTasks = tasks.count { t -> t.isCompleted }
                    )
                }
            }
        }
        viewModelScope.launch {
            projectAssetRepository.getAssetsByProjectId(projectId).collect { assets ->
                _state.update { it.copy(assets = assets) }
            }
        }
    }

    fun toggleMenu() { _state.update { it.copy(showMenu = !it.showMenu) } }
    fun dismissMenu() { _state.update { it.copy(showMenu = false) } }

    fun showAddUvDialog() { _state.update { it.copy(showAddUvDialog = true, showMenu = false) } }
    fun hideAddUvDialog() { _state.update { it.copy(showAddUvDialog = false, editingAsset = null) } }

    fun showAddDocumentDialog() { _state.update { it.copy(showAddDocumentDialog = true, showMenu = false) } }
    fun hideAddDocumentDialog() { _state.update { it.copy(showAddDocumentDialog = false, editingAsset = null) } }

    fun startEditAsset(asset: ProjectAssetEntity) {
        _state.update {
            it.copy(
                selectedAsset = null,
                editingAsset = asset,
                showAddUvDialog = asset.type == "uv",
                showAddDocumentDialog = asset.type == "document"
            )
        }
    }

    fun selectAsset(asset: ProjectAssetEntity) { _state.update { it.copy(selectedAsset = asset) } }
    fun clearSelectedAsset() { _state.update { it.copy(selectedAsset = null) } }

    fun saveUv(
        title: String,
        location: String,
        fuseType: String,
        circuitInfo: String,
        uvType: String,
        description: String,
        notes: String,
        photoPaths: List<String>,
        docPaths: List<String>
    ) {
        val projectId = _state.value.project?.id ?: return
        val existing = _state.value.editingAsset
        viewModelScope.launch {
            if (existing != null) {
                val updated = existing.copy(
                    title = title,
                    location = location,
                    fuseType = fuseType,
                    circuitInfo = circuitInfo,
                    uvType = uvType,
                    description = description,
                    notes = notes,
                    photoPaths = photoPaths.joinToString(","),
                    docPaths = docPaths.joinToString(",")
                )
                projectAssetRepository.updateAsset(updated)
            } else {
                projectAssetRepository.createAsset(
                    projectId = projectId,
                    type = "uv",
                    title = title,
                    location = location,
                    fuseType = fuseType,
                    circuitInfo = circuitInfo,
                    uvType = uvType,
                    description = description,
                    notes = notes,
                    photoPaths = photoPaths.joinToString(","),
                    docPaths = docPaths.joinToString(",")
                )
            }
            _state.update { it.copy(showAddUvDialog = false, editingAsset = null) }
        }
    }

    fun saveDocument(
        title: String,
        notes: String,
        photoPaths: List<String>,
        docPaths: List<String>
    ) {
        val projectId = _state.value.project?.id ?: return
        val existing = _state.value.editingAsset
        viewModelScope.launch {
            if (existing != null) {
                val updated = existing.copy(
                    title = title,
                    notes = notes,
                    photoPaths = photoPaths.joinToString(","),
                    docPaths = docPaths.joinToString(",")
                )
                projectAssetRepository.updateAsset(updated)
            } else {
                projectAssetRepository.createAsset(
                    projectId = projectId,
                    type = "document",
                    title = title,
                    notes = notes,
                    photoPaths = photoPaths.joinToString(","),
                    docPaths = docPaths.joinToString(",")
                )
            }
            _state.update { it.copy(showAddDocumentDialog = false, editingAsset = null) }
        }
    }

    fun deleteAsset(id: String) {
        viewModelScope.launch {
            projectAssetRepository.deleteAsset(id)
            _state.update { it.copy(selectedAsset = null) }
        }
    }
}
