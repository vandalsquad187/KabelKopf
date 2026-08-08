package com.docai.kabelkopf.ui.screens.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.ChecklistEntity
import com.docai.kabelkopf.data.local.entity.ChecklistItemEntity
import com.docai.kabelkopf.data.repository.ChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChecklistUiState(
    val checklists: List<ChecklistEntity> = emptyList(),
    val selectedChecklist: ChecklistEntity? = null,
    val items: List<ChecklistItemEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChecklistUiState())
    val state: StateFlow<ChecklistUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            checklistRepository.getAllChecklists().collect { list ->
                _state.update { it.copy(checklists = list) }
            }
        }
    }

    fun selectChecklist(checklist: ChecklistEntity) {
        _state.update { it.copy(selectedChecklist = checklist) }
        viewModelScope.launch {
            checklistRepository.getItemsForChecklist(checklist.id).collect { items ->
                _state.update { it.copy(items = items) }
            }
        }
    }

    fun backToList() {
        _state.update { it.copy(selectedChecklist = null, items = emptyList()) }
    }

    fun showAddDialog() {
        _state.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _state.update { it.copy(showAddDialog = false) }
    }

    fun createChecklist(name: String, category: String, items: List<String>) {
        viewModelScope.launch {
            checklistRepository.createChecklist(name, category, items)
            hideAddDialog()
        }
    }

    fun toggleItem(itemId: String, isChecked: Boolean) {
        viewModelScope.launch {
            checklistRepository.toggleItem(itemId, isChecked)
        }
    }

    fun deleteChecklist(id: String) {
        viewModelScope.launch {
            checklistRepository.deleteChecklist(id)
            backToList()
        }
    }
}
