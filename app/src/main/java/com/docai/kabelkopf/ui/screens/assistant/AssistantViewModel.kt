package com.docai.kabelkopf.ui.screens.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.ErrorLogEntity
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.data.repository.*
import com.docai.kabelkopf.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val task: WorkTaskEntity? = null,
    val steps: List<WorkStepEntity> = emptyList(),
    val currentStepIndex: Int = 0,
    val warnings: List<ErrorLogEntity> = emptyList(),
    val isComplete: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorDescription: String = "",
    val errorSeverity: Int = 3
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val stepRepository: StepRepository,
    private val errorLogRepository: ErrorLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AssistantUiState())
    val state: StateFlow<AssistantUiState> = _state.asStateFlow()

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            _state.update { it.copy(task = task) }

            stepRepository.getStepsForTask(taskId).collect { stepList ->
                val sorted = stepList.sortedBy { it.stepOrder }
                _state.update { it.copy(steps = sorted) }
            }

            val warningList = errorLogRepository.getRelevantWarnings(task.category)
            _state.update { it.copy(warnings = warningList) }
        }
    }

    fun toggleStep(stepId: String) {
        val current = _state.value
        val step = current.steps.find { it.id == stepId } ?: return
        val newDone = !step.isDone

        viewModelScope.launch {
            stepRepository.setStepDone(stepId, newDone)

            if (newDone) {
                val nextIndex = current.steps.indexOf(step) + 1
                if (nextIndex < current.steps.size) {
                    _state.update { it.copy(currentStepIndex = nextIndex) }
                }
            }

            val allDone = current.steps.all { s -> if (s.id == stepId) newDone else s.isDone }
            if (newDone && allDone && current.task != null) {
                taskRepository.completeTask(current.task.id)
                _state.update { it.copy(isComplete = true) }
            }
        }
    }

    fun goToStep(index: Int) {
        if (index in _state.value.steps.indices) {
            _state.update { it.copy(currentStepIndex = index) }
        }
    }

    fun showErrorDialog() {
        _state.update { it.copy(showErrorDialog = true) }
    }

    fun hideErrorDialog() {
        _state.update { it.copy(showErrorDialog = false, errorDescription = "") }
    }

    fun updateErrorDescription(desc: String) {
        _state.update { it.copy(errorDescription = desc) }
    }

    fun updateErrorSeverity(severity: Int) {
        _state.update { it.copy(errorSeverity = severity) }
    }

    fun saveError() {
        val s = _state.value
        if (s.errorDescription.isBlank() || s.task == null) return

        viewModelScope.launch {
            errorLogRepository.logError(
                taskCategory = s.task.category,
                description = s.errorDescription,
                severity = s.errorSeverity,
                taskId = s.task.id
            )
            _state.update { it.copy(showErrorDialog = false, errorDescription = "") }
        }
    }
}
