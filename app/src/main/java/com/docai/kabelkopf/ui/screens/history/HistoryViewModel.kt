package com.docai.kabelkopf.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.WorkTaskEntity
import com.docai.kabelkopf.data.repository.TaskRepository
import com.docai.kabelkopf.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val tasks: List<WorkTaskEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val showOnlyActive: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                _state.update { it.copy(tasks = applyFilters(tasks)) }
            }
        }
        viewModelScope.launch {
            taskRepository.getAllCategories().collect { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                taskRepository.getAllTasks().collect { tasks ->
                    _state.update { it.copy(tasks = applyFilters(tasks)) }
                }
            } else {
                taskRepository.searchTasks(query).collect { tasks ->
                    _state.update { it.copy(tasks = applyFilters(tasks)) }
                }
            }
        }
    }

    fun selectCategory(category: String?) {
        _state.update { it.copy(selectedCategory = if (it.selectedCategory == category) null else category) }
    }

    fun toggleActiveFilter() {
        _state.update { it.copy(showOnlyActive = !it.showOnlyActive) }
    }

    private fun applyFilters(tasks: List<WorkTaskEntity>): List<WorkTaskEntity> {
        val s = _state.value
        var filtered = tasks
        if (s.selectedCategory != null) {
            filtered = filtered.filter { it.category == s.selectedCategory }
        }
        if (s.showOnlyActive) {
            filtered = filtered.filter { !it.isCompleted }
        }
        return filtered
    }
}
