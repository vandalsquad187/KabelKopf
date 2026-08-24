package com.docai.kabelkopf.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.data.repository.*
import com.docai.kabelkopf.service.ExportSelection
import com.docai.kabelkopf.service.ImportPreview
import com.docai.kabelkopf.service.KKbManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val completionRate: Float = 0f,
    val errorCount: Int = 0,
    val recentErrors: List<String> = emptyList(),
    val tasksByCategory: Map<String, Int> = emptyMap(),
    val skillProgress: List<SkillProgress> = emptyList(),
    val totalXp: Int = 0,
    val overallProgress: Float = 0f
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val errorLogRepository: ErrorLogRepository,
    private val skillRepository: SkillRepository,
    private val kkbManager: KKbManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()
    private val _importResult = MutableSharedFlow<String>()
    val importResult: SharedFlow<String> = _importResult.asSharedFlow()
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _importPreview = MutableStateFlow<ImportPreview?>(null)
    val importPreview: StateFlow<ImportPreview?> = _importPreview.asStateFlow()

    private var pendingImportUri: Uri? = null

    init {
        viewModelScope.launch {
            taskRepository.getTaskCount().collect { total ->
                val completed = _state.value.completedTasks
                _state.update {
                    it.copy(
                        totalTasks = total,
                        completionRate = if (total > 0) completed.toFloat() / total else 0f
                    )
                }
            }
        }
        viewModelScope.launch {
            taskRepository.getCompletedTaskCount().collect { completed ->
                val total = _state.value.totalTasks
                _state.update {
                    it.copy(
                        completedTasks = completed,
                        completionRate = if (total > 0) completed.toFloat() / total else 0f
                    )
                }
            }
        }
        viewModelScope.launch {
            errorLogRepository.getAllErrors().collect { errors ->
                _state.update {
                    it.copy(
                        errorCount = errors.size,
                        recentErrors = errors.take(5).map { e -> e.description }
                    )
                }
            }
        }
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                val byCategory = tasks.groupBy { it.category }
                    .mapValues { it.value.size }
                _state.update { it.copy(tasksByCategory = byCategory) }
            }
        }
        viewModelScope.launch {
            skillRepository.getAllSkills().collect { skills ->
                val progress = skills.map { s ->
                    val percent = if (s.nextLevelXp > 0)
                        (s.currentXp.toFloat() / s.nextLevelXp * 100).coerceAtMost(100f)
                    else 100f
                    SkillProgress(
                        name = s.name,
                        currentXp = s.currentXp,
                        nextLevelXp = s.nextLevelXp,
                        level = s.level,
                        maxLevel = s.maxLevel,
                        progressPercent = percent
                    )
                }
                val totalXp = skills.sumOf { it.currentXp }
                val maxPossible = skills.sumOf { if (it.nextLevelXp > 0) it.nextLevelXp else it.currentXp }
                val overall = if (maxPossible > 0) (totalXp.toFloat() / maxPossible * 100).coerceAtMost(100f) else 0f
                _state.update {
                    it.copy(skillProgress = progress, totalXp = totalXp, overallProgress = overall)
                }
            }
        }
    }

    fun exportKKb(selection: ExportSelection, outputUri: Uri) {
        if (_isExporting.value) return
        viewModelScope.launch {
            _isExporting.value = true
            val success = kkbManager.exportKKb(selection, outputUri)
            _importResult.emit(if (success) "Backup exportiert!" else "Export fehlgeschlagen")
            _isExporting.value = false
        }
    }

    fun loadImportPreview(uri: Uri) {
        viewModelScope.launch {
            val preview = kkbManager.readPreview(uri)
            if (preview != null) {
                pendingImportUri = uri
                _importPreview.value = preview
            } else {
                _importResult.emit("Backup-Datei konnte nicht gelesen werden")
            }
        }
    }

    fun confirmImport() {
        val uri = pendingImportUri ?: return
        if (_isImporting.value) return
        viewModelScope.launch {
            _isImporting.value = true
            val success = kkbManager.importKKb(uri)
            _importResult.emit(if (success) "Backup erfolgreich importiert!" else "Import fehlgeschlagen")
            _importPreview.value = null
            pendingImportUri = null
            _isImporting.value = false
        }
    }

    fun dismissImportPreview() {
        _importPreview.value = null
        pendingImportUri = null
    }
}
