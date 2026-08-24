package com.docai.kabelkopf.ui.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.KnowledgeBaseEntity
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.data.local.entity.WorkStepEntity
import com.docai.kabelkopf.data.local.entity.WorkflowTemplateEntity
import com.docai.kabelkopf.data.repository.*
import com.docai.kabelkopf.service.VoiceRecorder
import com.docai.kabelkopf.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NewTaskUiState(
    val title: String = "",
    val category: String = Constants.Categories.UV,
    val description: String = "",
    val location: String = "",
    val customerName: String = "",
    val steps: List<StepInput> = emptyList(),
    val isSaving: Boolean = false,
    val savedTaskId: String? = null,
    val voiceNotePath: String? = null,
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val photoPaths: List<String> = emptyList(),
    val showCategoryPicker: Boolean = false,
    val timerElapsedSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val suggestedKnowledge: List<KnowledgeBaseEntity> = emptyList(),
    val templateSuggestions: List<WorkflowTemplateEntity> = emptyList()
)

data class StepInput(
    val id: String = UUID.randomUUID().toString(),
    val description: String = "",
    val warning: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val stepRepository: StepRepository,
    private val errorLogRepository: ErrorLogRepository,
    private val templateRepository: TemplateRepository,
    private val voiceRecorder: VoiceRecorder,
    private val errorCauseRepository: ErrorCauseRepository,
    private val skillRepository: SkillRepository,
    private val knowledgeRepository: KnowledgeRepository,
    private val gamificationManager: com.docai.kabelkopf.service.GamificationManager,
    private val pdfExportManager: com.docai.kabelkopf.service.PdfExportManager
) : ViewModel() {

    private val _state = MutableStateFlow(NewTaskUiState())
    val state: StateFlow<NewTaskUiState> = _state.asStateFlow()

    private val _task = MutableStateFlow<WorkTaskEntity?>(null)
    val task: StateFlow<WorkTaskEntity?> = _task.asStateFlow()

    private val _steps = MutableStateFlow<List<WorkStepEntity>>(emptyList())
    val steps: StateFlow<List<WorkStepEntity>> = _steps.asStateFlow()

    private val _warnings = MutableStateFlow<List<com.docai.kabelkopf.data.local.entity.ErrorLogEntity>>(emptyList())
    val warnings: StateFlow<List<com.docai.kabelkopf.data.local.entity.ErrorLogEntity>> = _warnings.asStateFlow()

    fun updateTitle(value: String) { _state.update { it.copy(title = value) } }
    fun updateCategory(value: String) {
        _state.update { it.copy(category = value) }
        viewModelScope.launch {
            templateRepository.getTemplatesByCategory(value).collect { templates ->
                _state.update { it.copy(templateSuggestions = templates) }
            }
        }
    }
    fun updateDescription(value: String) { _state.update { it.copy(description = value) } }
    fun updateLocation(value: String) { _state.update { it.copy(location = value) } }
    fun updateCustomer(value: String) { _state.update { it.copy(customerName = value) } }
    fun toggleCategoryPicker() { _state.update { it.copy(showCategoryPicker = !it.showCategoryPicker) } }

    fun applyTemplate(template: WorkflowTemplateEntity) {
        val steps = template.stepsJson.split("\n---\n").filter { it.isNotBlank() }
        _state.update { current ->
            val existingSteps = current.steps.toMutableList()
            steps.forEach { stepText ->
                existingSteps.add(StepInput(description = stepText))
            }
            current.copy(steps = existingSteps)
        }
    }

    fun addStep() {
        _state.update {
            it.copy(steps = it.steps + StepInput())
        }
    }

    fun updateStep(index: Int, description: String, warning: String?) {
        _state.update {
            val updated = it.steps.toMutableList()
            if (index < updated.size) {
                updated[index] = updated[index].copy(description = description, warning = warning)
            }
            it.copy(steps = updated)
        }
    }

    fun removeStep(index: Int) {
        _state.update {
            val updated = it.steps.toMutableList()
            if (index < updated.size) updated.removeAt(index)
            it.copy(steps = updated)
        }
    }

    fun toggleRecording() {
        val current = _state.value
        if (current.isRecording) {
            val file = voiceRecorder.stopRecording()
            _state.update {
                it.copy(isRecording = false, voiceNotePath = file?.absolutePath ?: it.voiceNotePath)
            }
        } else {
            voiceRecorder.startRecording()
            _state.update { it.copy(isRecording = true, isPlaying = false) }
        }
    }

    fun togglePlayback() {
        val path = _state.value.voiceNotePath ?: return
        if (_state.value.isPlaying) {
            voiceRecorder.stopPlayback()
            _state.update { it.copy(isPlaying = false) }
        } else {
            voiceRecorder.startPlayback(path) {
                _state.update { it.copy(isPlaying = false) }
            }
            _state.update { it.copy(isPlaying = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecorder.cleanup()
    }

    fun addPhotoPath(path: String) {
        _state.update { it.copy(photoPaths = it.photoPaths + path) }
    }

    fun startTimer() {
        _state.update { it.copy(isTimerRunning = true) }
    }

    fun pauseTimer() {
        _state.update { it.copy(isTimerRunning = false) }
    }

    fun resetTimer() {
        _state.update { it.copy(isTimerRunning = false, timerElapsedSeconds = 0) }
    }

    fun tickTimer() {
        _state.update { it.copy(timerElapsedSeconds = it.timerElapsedSeconds + 1) }
    }

    fun saveTask(projectId: String? = null) {
        viewModelScope.launch {
            val s = _state.value
            if (s.title.isBlank()) return@launch

            _state.update { it.copy(isSaving = true) }

            val task = taskRepository.createTask(
                title = s.title,
                category = s.category,
                description = s.description,
                location = s.location,
                customerName = s.customerName,
                projectId = projectId,
                durationSeconds = _state.value.timerElapsedSeconds
            )

            s.steps.forEachIndexed { index, stepInput ->
                if (stepInput.description.isNotBlank()) {
                    stepRepository.addStep(
                        taskId = task.id,
                        description = stepInput.description,
                        order = index,
                        warning = stepInput.warning
                    )
                }
            }

            earnXpForCategory(s.category, 10)

            gamificationManager.checkOnTaskCreated(s.category)
            gamificationManager.checkOnStreak()

            val keywords = s.title.split(" ") + s.description.split(" ") + s.category.split(" ")
            val knowledge = knowledgeRepository.findRelevantKnowledge(s.category, keywords)
            _state.update { it.copy(suggestedKnowledge = knowledge) }

            _state.update {
                it.copy(isSaving = false, savedTaskId = task.id, isTimerRunning = false)
            }
        }
    }

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            val t = taskRepository.getTaskById(taskId)
            _task.value = t
            if (t != null) {
                viewModelScope.launch {
                    stepRepository.getStepsForTask(taskId).collect { stepList ->
                        _steps.value = stepList
                    }
                }
                viewModelScope.launch {
                    val warningList = errorLogRepository.getRelevantWarnings(t.category)
                    _warnings.value = warningList
                }
            }
        }
    }

    fun loadWarningsForCategory(category: String) {
        viewModelScope.launch {
            val warnings = errorLogRepository.getRelevantWarnings(category)
            _warnings.value = warnings
        }
    }

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    fun startEdit() {
        val t = _task.value ?: return
        _state.update {
            NewTaskUiState(
                title = t.title,
                category = t.category,
                description = t.description,
                location = t.location,
                customerName = t.customerName,
                steps = _steps.value.map { StepInput(id = it.id, description = it.description, warning = it.warning) }
            )
        }
        _isEditing.value = true
    }

    fun cancelEdit() {
        _isEditing.value = false
        _state.update { NewTaskUiState() }
    }

    fun confirmEdit() {
        viewModelScope.launch {
            val s = _state.value
            val t = _task.value ?: return@launch

            val updated = t.copy(
                title = s.title,
                category = s.category,
                description = s.description,
                location = s.location,
                customerName = s.customerName
            )
            taskRepository.updateTask(updated)

            stepRepository.deleteStepsForTask(t.id)
            s.steps.forEachIndexed { index, stepInput ->
                if (stepInput.description.isNotBlank()) {
                    stepRepository.addStep(
                        taskId = t.id,
                        description = stepInput.description,
                        order = index,
                        warning = stepInput.warning
                    )
                }
            }

            _isEditing.value = false
            loadTask(t.id)
        }
    }

    fun toggleStepDone(stepId: String, isDone: Boolean) {
        viewModelScope.launch {
            stepRepository.setStepDone(stepId, isDone)
            updateTaskCompletion()
        }
    }

    private suspend fun updateTaskCompletion() {
        val t = _task.value ?: return
        val allSteps = stepRepository.getStepsForTaskOnce(t.id)
        val allDone = allSteps.isNotEmpty() && allSteps.all { it.isDone }
        if (allDone != t.isCompleted) {
            taskRepository.updateTask(t.copy(isCompleted = allDone))
            if (allDone) {
                earnXpForCategory(t.category, 50)
                gamificationManager.checkOnTaskCompleted(t.id)
                gamificationManager.checkOnSkillChange()
            }
            loadTask(t.id)
        }
    }

    private suspend fun earnXpForCategory(category: String, xp: Int) {
        val skills = skillRepository.getSkillsForCategoryOnce(category)
        skills.forEach { skill ->
            skillRepository.addXp(skill.id, xp)
        }
    }

    fun exportPdf() {
        val t = _task.value ?: return
        viewModelScope.launch {
            val steps = stepRepository.getStepsForTaskOnce(t.id)
            val file = pdfExportManager.exportTaskPdf(t, steps)
            if (file != null) {
                pdfExportManager.sharePdf(file)
            }
        }
    }

    fun saveRating(rating: Int) {
        viewModelScope.launch {
            val t = _task.value ?: return@launch
            taskRepository.updateTask(t.copy(rating = rating))
            _task.update { it?.copy(rating = rating) }
            earnXpForCategory(t.category, rating * 10)
            gamificationManager.checkOnRatingGiven(rating)
            gamificationManager.checkOnSkillChange()
        }
    }

    private val _editErrorDialog = MutableStateFlow(EditErrorState())
    val editErrorDialog: StateFlow<EditErrorState> = _editErrorDialog.asStateFlow()

    fun showEditErrorDialog() {
        val category = _task.value?.category ?: _state.value.category
        _editErrorDialog.update { it.copy(showDialog = true) }
        viewModelScope.launch {
            errorCauseRepository.getCausesForCategory(category).collect { causes ->
                _editErrorDialog.update { it.copy(availableCauses = causes) }
            }
        }
    }

    fun hideEditErrorDialog() {
        _editErrorDialog.update { EditErrorState() }
    }

    fun updateEditErrorDescription(desc: String) {
        _editErrorDialog.update { it.copy(description = desc) }
    }

    fun updateEditErrorSeverity(severity: Int) {
        _editErrorDialog.update { it.copy(severity = severity) }
    }

    fun updateEditErrorSolution(solution: String) {
        _editErrorDialog.update { it.copy(solution = solution) }
    }

    fun toggleEditErrorCause(causeId: String) {
        _editErrorDialog.update { state ->
            val selected = state.selectedCauseIds.toMutableSet()
            if (selected.contains(causeId)) selected.remove(causeId)
            else selected.add(causeId)
            state.copy(selectedCauseIds = selected)
        }
    }

    fun saveEditError() {
        val s = _editErrorDialog.value
        if (s.description.isBlank()) return
        val category = _task.value?.category ?: _state.value.category

        viewModelScope.launch {
            errorLogRepository.logError(
                taskCategory = category,
                description = s.description,
                severity = s.severity,
                taskId = _task.value?.id,
                causeIds = s.selectedCauseIds.toList(),
                solution = s.solution
            )
            if (s.solution.isNotBlank()) {
                earnXpForCategory(category, 30)
            }
            _editErrorDialog.update { EditErrorState() }
            loadWarningsForCategory(category)
            gamificationManager.checkOnErrorLogged(
                errorsWithSolutionCount = errorLogRepository.getErrorsWithSolutionCount()
            )
        }
    }
}

data class EditErrorState(
    val showDialog: Boolean = false,
    val description: String = "",
    val severity: Int = 3,
    val solution: String = "",
    val availableCauses: List<com.docai.kabelkopf.data.local.entity.ErrorCauseEntity> = emptyList(),
    val selectedCauseIds: Set<String> = emptySet()
)
