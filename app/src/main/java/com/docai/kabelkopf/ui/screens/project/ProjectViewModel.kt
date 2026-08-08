package com.docai.kabelkopf.ui.screens.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.ProjectEntity
import com.docai.kabelkopf.data.repository.ProjectRepository
import com.docai.kabelkopf.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectListUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectListUiState())
    val state: StateFlow<ProjectListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            projectRepository.getAllProjects().collect { projects ->
                _state.update { it.copy(projects = projects) }
            }
        }
    }

    fun showAddDialog() { _state.update { it.copy(showAddDialog = true) } }
    fun hideAddDialog() { _state.update { it.copy(showAddDialog = false) } }

    fun createProject(name: String, address: String, contactName: String, contactPhone: String) {
        viewModelScope.launch {
            projectRepository.createProject(name, address, contactName, contactPhone)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            projectRepository.deleteProject(id)
        }
    }
}
