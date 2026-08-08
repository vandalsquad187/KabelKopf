package com.docai.kabelkopf.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.data.repository.*
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
    private val backupManager: com.docai.kabelkopf.service.BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()
    private val _importResult = MutableSharedFlow<String>()
    val importResult: SharedFlow<String> = _importResult.asSharedFlow()
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

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

    fun exportBackup() {
        if (_isExporting.value) return
        viewModelScope.launch {
            _isExporting.value = true
            val file = backupManager.exportBackup()
            if (file != null) {
                backupManager.shareBackup(file)
            }
            _isExporting.value = false
        }
    }

    fun importBackup(uri: Uri) {
        if (_isImporting.value) return
        viewModelScope.launch {
            _isImporting.value = true
            val success = backupManager.importBackup(uri)
            _importResult.emit(if (success) "Backup erfolgreich importiert!" else "Import fehlgeschlagen")
            _isImporting.value = false
        }
    }
}
