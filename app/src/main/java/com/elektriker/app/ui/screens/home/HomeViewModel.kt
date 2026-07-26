package com.elektriker.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elektriker.app.data.local.entity.WorkTaskEntity
import com.elektriker.app.data.repository.ErrorLogRepository
import com.elektriker.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentTasks: List<WorkTaskEntity> = emptyList(),
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val errorCountsByTask: Map<String, Int> = emptyMap()
) {
    val totalErrorCount: Int get() = errorCountsByTask.values.sum()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val errorLogRepository: ErrorLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getRecentTasks().collect { tasks ->
                val errorCounts = mutableMapOf<String, Int>()
                tasks.forEach { task ->
                    errorCounts[task.id] = errorLogRepository.getErrorCountByTask(task.id)
                }
                _state.update { it.copy(recentTasks = tasks, errorCountsByTask = errorCounts) }
            }
        }
        viewModelScope.launch {
            taskRepository.getTaskCount().collect { count ->
                _state.update { it.copy(totalTasks = count) }
            }
        }
        viewModelScope.launch {
            taskRepository.getCompletedTaskCount().collect { count ->
                _state.update { it.copy(completedTasks = count) }
            }
        }
    }
}
